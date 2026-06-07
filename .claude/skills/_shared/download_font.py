#!/usr/bin/env python3
"""
Google-Fonts downloader for the design-driven font swap (KMP-aware).

Companion to download_assets.py, but for the ONE app-global font family rather
than per-feature drawables — fonts do NOT use the domain/chrome promotion model,
they always land in the design system's font dir. This script resolves a Google
Fonts family to a bundle-ready set of .ttf files in
`core/designsystem/.../composeResources/font/` and emits the exact
`Font(Res.font.…)` lines to paste into XTheme.kt's `XFontFamily()`.

USAGE
-----
python3 download_font.py \\
  --project-root {repo_root} \\
  (--font "Manrope" | --css-url "https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700" | --html .../stitch_success.html) \\
  [--weights regular,medium,bold] \\
  [--source auto|github|css2] \\
  [--manifest .../fonts.json] \\
  [--manifest-only | --dry-run]

INPUT (one of):
  --font      Family name, e.g. "Manrope" / "Open Sans".
  --css-url   A fonts.googleapis.com/css2 URL (family + weights parsed from it).
  --html      A Stitch HTML export; the embedded css2 <link> is parsed out of it.

WEIGHTS
  Default regular,medium,bold (400/500/700) — the three the XTheme type scale uses.
  Accepts names (regular/medium/bold/…) or raw numbers (400/500/700).

SOURCES (precedence in `auto`)
  1. github  — github.com/google/fonts contents API (ofl/ → apache/ → ufl/).
               Prefers full static `{Family}-{Weight}.ttf`; else the full
               variable `{Family}[wght].ttf` (→ FontVariation wiring). Full glyph
               coverage, deterministic raw.githubusercontent.com download.
  2. css2    — fonts.googleapis.com/css2 with a TrueType-forcing UA. gstatic .ttf
               are unicode-range SUBSETS (web partial-loading), so this is the
               fallback only — fine for Latin UIs, not full coverage.

MODES
  --manifest-only : write fonts.json only (no download, no source mutation). Used
                    by /ui-designer (design-only skill).
  default (full)  : download .ttf into the font dir + write fonts.json. Used by
                    /creating-kmp-feature & /modifying-kmp-feature (design-aware).
  --dry-run       : resolve + report, mutate nothing.

On any failure the script prints the exact filenames, target dir, and Font(...)
lines to drop in by hand, then exits non-zero — a font swap is rare and one-time,
so it must surface a manual path rather than silently half-apply.

Dependencies: Python 3 stdlib only.
"""

import argparse
import json
import re
import sys
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import unquote
from urllib.request import Request, urlopen

USER_AGENT = "download-font/1.0"
# Old UA so Google Fonts css2 serves TrueType (no woff2/woff capability advertised).
TTF_USER_AGENT = "Mozilla/5.0 (Windows NT 5.1)"
DOWNLOAD_TIMEOUT_S = 30

DESIGN_SYSTEM_FONT_DIR_REL = (
    Path("core") / "designsystem" / "src" / "commonMain" / "composeResources" / "font"
)
GOOGLE_FONTS_LICENSES = ("ofl", "apache", "ufl")
GITHUB_CONTENTS = "https://api.github.com/repos/google/fonts/contents/{license}/{slug}"

# weight name -> CSS numeric weight
NAME_TO_NUM = {
    "thin": 100, "extralight": 200, "light": 300, "regular": 400, "normal": 400,
    "medium": 500, "semibold": 600, "bold": 700, "extrabold": 800, "black": 900,
}
NUM_TO_NAME = {
    100: "thin", 200: "extralight", 300: "light", 400: "regular", 500: "medium",
    600: "semibold", 700: "bold", 800: "extrabold", 900: "black",
}
# CSS numeric weight -> Compose FontWeight member
NUM_TO_FONTWEIGHT = {
    100: "Thin", 200: "ExtraLight", 300: "Light", 400: "Normal", 500: "Medium",
    600: "SemiBold", 700: "Bold", 800: "ExtraBold", 900: "Black",
}
# google/fonts static filename weight token, e.g. Manrope-SemiBold.ttf
NUM_TO_STATIC_TOKEN = {
    100: "Thin", 200: "ExtraLight", 300: "Light", 400: "Regular", 500: "Medium",
    600: "SemiBold", 700: "Bold", 800: "ExtraBold", 900: "Black",
}


def slug(family: str) -> str:
    """Family name -> resource-safe stem segment: lowercase, non-alnum -> '_'."""
    return re.sub(r"[^a-z0-9]+", "_", family.strip().lower()).strip("_")


def parse_weights(spec: str) -> list:
    out = []
    for tok in (t.strip().lower() for t in spec.split(",") if t.strip()):
        if tok.isdigit():
            n = int(tok)
        elif tok in NAME_TO_NUM:
            n = NAME_TO_NUM[tok]
        else:
            print(f"warning: unknown weight '{tok}' — skipped.", file=sys.stderr)
            continue
        if n not in out:
            out.append(n)
    return out or [400, 500, 700]


def css_url_for(family: str, weights: list) -> str:
    wlist = ";".join(str(w) for w in sorted(weights))
    fam = family.strip().replace(" ", "+")
    return f"https://fonts.googleapis.com/css2?family={fam}:wght@{wlist}&display=swap"


def parse_css_link_from_html(html_path: Path) -> str:
    text = html_path.read_text(encoding="utf-8", errors="ignore")
    # Skip the Material Symbols link; we want a real text font family.
    for m in re.finditer(r'href="([^"]*fonts\.googleapis\.com/css2[^"]*)"', text):
        url = m.group(1).replace("&amp;", "&")
        if "Material+Symbols" in url:
            continue
        return url
    raise SystemExit(
        f"error: no text-font css2 <link> found in {html_path}. "
        "Pass --font or --css-url explicitly."
    )


def family_from_css_url(url: str) -> str:
    m = re.search(r"[?&]family=([^:&]+)", url)
    if not m:
        raise SystemExit(f"error: could not parse family= from css url: {url}")
    return unquote(m.group(1)).replace("+", " ")


def weights_from_css_url(url: str) -> list:
    m = re.search(r"wght@([0-9;]+)", url)
    if not m:
        return []
    nums = []
    for part in m.group(1).split(";"):
        part = part.strip()
        if part.isdigit() and int(part) not in nums:
            nums.append(int(part))
    return nums


def _http_get(url: str, *, ua: str = USER_AGENT) -> bytes:
    req = Request(url, headers={"User-Agent": ua})
    with urlopen(req, timeout=DOWNLOAD_TIMEOUT_S) as resp:
        return resp.read()


# ---------------------------------------------------------------------------
# Route 1: github.com/google/fonts
# ---------------------------------------------------------------------------


def github_list(slug_name: str):
    """Returns (license, [ {name, download_url} ]) for the first matching license dir."""
    for lic in GOOGLE_FONTS_LICENSES:
        url = GITHUB_CONTENTS.format(license=lic, slug=slug_name)
        try:
            data = json.loads(_http_get(url))
        except HTTPError as e:
            if e.code == 404:
                continue
            raise
        files = []
        for entry in data:
            name = entry.get("name", "")
            dl = entry.get("download_url")
            if name.lower().endswith(".ttf") and dl:
                files.append({"name": name, "download_url": dl})
            elif entry.get("type") == "dir" and name == "static":
                # Some families keep static instances in a static/ subdir.
                try:
                    sub = json.loads(_http_get(f"{url}/static"))
                    for s in sub:
                        sn, sd = s.get("name", ""), s.get("download_url")
                        if sn.lower().endswith(".ttf") and sd:
                            files.append({"name": sn, "download_url": sd})
                except (HTTPError, URLError):
                    pass
        if files:
            return lic, files
    return None, []


def github_resolve(family: str, weights: list):
    """Pick the github route. Returns a plan dict or None."""
    lic, files = github_list(slug(family))
    if not files:
        return None
    fam_token = family.replace(" ", "")
    static = {}  # num -> download_url
    variable = None
    for f in files:
        nm = f["name"]
        if "[" in nm:  # variable font, e.g. Manrope[wght].ttf
            variable = f["download_url"]
            continue
        m = re.match(rf"{re.escape(fam_token)}-([A-Za-z]+)\.ttf$", nm)
        if m:
            token = m.group(1)
            for num, t in NUM_TO_STATIC_TOKEN.items():
                if t == token:
                    static[num] = f["download_url"]
    if all(w in static for w in weights):
        return {"route": "github-static", "license": lic,
                "files": {w: static[w] for w in weights}}
    if variable is not None:
        return {"route": "github-variable", "license": lic, "variable_url": variable}
    if static:  # partial static — take what's there, fall back per missing weight
        return {"route": "github-static", "license": lic,
                "files": {w: static[w] for w in weights if w in static}}
    return None


# ---------------------------------------------------------------------------
# Route 2: fonts.googleapis.com/css2 (TrueType subsets)
# ---------------------------------------------------------------------------


def css2_resolve(css_url: str, weights: list):
    """Fetch css2 with a TTF-forcing UA, parse weight->ttf url. Returns plan or None."""
    want = css_url_for(family_from_css_url(css_url), weights)
    try:
        css = _http_get(want, ua=TTF_USER_AGENT).decode("utf-8", errors="ignore")
    except (HTTPError, URLError, TimeoutError) as e:
        print(f"warning: css2 fetch failed: {e}", file=sys.stderr)
        return None
    files = {}
    for block in css.split("@font-face"):
        wm = re.search(r"font-weight:\s*(\d+)", block)
        um = re.search(r"url\((https://[^)]+\.ttf)\)", block)
        if not (wm and um):
            continue
        num = int(wm.group(1))
        if num in weights and num not in files:  # first subset (latin) per weight
            files[num] = um.group(1)
    if not files:
        return None
    return {"route": "css2-static", "files": {w: files[w] for w in weights if w in files}}


# ---------------------------------------------------------------------------
# Wiring emitters
# ---------------------------------------------------------------------------


def font_lines(plan: dict, family: str, weights: list) -> list:
    s = slug(family)
    lines = []
    if plan["route"] in ("github-static", "css2-static"):
        for w in weights:
            if w not in plan["files"]:
                continue
            lines.append(
                f"Font(Res.font.{s}_{NUM_TO_NAME[w]}, FontWeight.{NUM_TO_FONTWEIGHT[w]}),"
            )
    else:  # github-variable
        for w in weights:
            lines.append(
                f"Font(Res.font.{s}_variable, FontWeight.{NUM_TO_FONTWEIGHT[w]}, "
                f"variationSettings = FontVariation.Settings(FontVariation.weight({w}))),"
            )
    return lines


def planned_files(plan: dict, family: str, weights: list) -> dict:
    """Returns {dest_filename: source_url} for downloads."""
    s = slug(family)
    if plan["route"] in ("github-static", "css2-static"):
        return {f"{s}_{NUM_TO_NAME[w]}.ttf": url for w, url in plan["files"].items()}
    return {f"{s}_variable.ttf": plan["variable_url"]}


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def main(argv):
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument("--project-root", required=True, type=Path)
    src = ap.add_mutually_exclusive_group(required=True)
    src.add_argument("--font")
    src.add_argument("--css-url")
    src.add_argument("--html", type=Path)
    ap.add_argument("--weights", default="regular,medium,bold")
    ap.add_argument("--source", choices=["auto", "github", "css2"], default="auto")
    ap.add_argument("--manifest", type=Path, default=None)
    ap.add_argument("--manifest-only", action="store_true")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args(argv)

    project_root = args.project_root.resolve()
    if not project_root.is_dir():
        print(f"error: --project-root not a directory: {project_root}", file=sys.stderr)
        return 2

    # Resolve family, css url, weights from whichever input was given.
    css_url = None
    if args.css_url:
        css_url = args.css_url.replace("&amp;", "&")
        family = family_from_css_url(css_url)
    elif args.html:
        if not args.html.is_file():
            print(f"error: --html not a file: {args.html}", file=sys.stderr)
            return 2
        css_url = parse_css_link_from_html(args.html)
        family = family_from_css_url(css_url)
    else:
        family = args.font

    weights = parse_weights(args.weights)
    if css_url and args.weights == "regular,medium,bold":
        link_weights = weights_from_css_url(css_url)
        # Keep only the standard 3 the theme uses, but constrained to what the link offers.
        if link_weights:
            weights = [w for w in (400, 500, 700) if w in link_weights] or weights
    if not css_url:
        css_url = css_url_for(family, weights)

    s = slug(family)
    font_dir = project_root / DESIGN_SYSTEM_FONT_DIR_REL
    # Default manifest lives beside the HTML (extracted/) when given, else at the
    # project root — NEVER inside font_dir, which must hold only font files or the
    # compose-resources accessor generation picks up a stray .json.
    manifest_path = args.manifest or (
        (args.html.parent / "fonts.json") if args.html else (project_root / f"{s}.fonts.json")
    )

    print(f"font: {family}  (slug={s})  weights={weights}  source={args.source}")

    # Resolve a download plan per the source precedence.
    plan = None
    if args.source in ("auto", "github"):
        try:
            plan = github_resolve(family, weights)
        except (HTTPError, URLError, TimeoutError) as e:
            print(f"warning: github route failed: {e}", file=sys.stderr)
    if plan is None and args.source in ("auto", "css2"):
        plan = css2_resolve(css_url, weights)

    if plan is None:
        _print_manual_fallback(family, weights, font_dir, project_root, css_url)
        return 1

    files_map = planned_files(plan, family, weights)
    wiring = font_lines(plan, family, weights)
    res_accessors = [fn[:-4] for fn in files_map]  # strip .ttf -> Res.font accessor stem

    print(f"\nroute: {plan['route']}")
    for fn, url in files_map.items():
        print(f"  {fn:32s} <- {url}")
    print("\nXFontFamily() wiring (paste into XTheme.kt):")
    print("    FontFamily(")
    for line in wiring:
        print(f"        {line}")
    print("    )")
    if plan["route"] == "github-variable":
        print("  (variable font — ensure these imports in XTheme.kt:)")
        print("    import androidx.compose.ui.text.font.FontVariation")

    # Download (full mode only).
    status = "pending"
    if not args.manifest_only and not args.dry_run:
        font_dir.mkdir(parents=True, exist_ok=True)
        ok = True
        for fn, url in files_map.items():
            dest = font_dir / fn
            if dest.is_file():
                print(f"  [skip-exists] {fn}")
                continue
            try:
                dest.write_bytes(_http_get(url))
                print(f"  [downloaded]  {fn}")
            except (HTTPError, URLError, TimeoutError) as e:
                ok = False
                print(f"  [FAILED]      {fn}: {e}", file=sys.stderr)
        status = "downloaded" if ok else "partial"
        if not ok:
            _print_manual_fallback(family, weights, font_dir, project_root, css_url)
    elif args.dry_run:
        status = "dry-run"

    # Write manifest (skip on dry-run).
    payload = {
        "family": family,
        "slug": s,
        "route": plan["route"],
        "weights": weights,
        "css_url": css_url,
        "font_dir": str(font_dir.relative_to(project_root)),
        "files": list(files_map.keys()),
        "res_accessors": res_accessors,
        "font_family_lines": wiring,
        "download_status": status,
        "generator": "download_font.py",
        "version": 1,
    }
    if not args.dry_run:
        manifest_path.parent.mkdir(parents=True, exist_ok=True)
        manifest_path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
        try:
            print(f"\nmanifest: {manifest_path.relative_to(project_root)}")
        except ValueError:
            print(f"\nmanifest: {manifest_path}")
    else:
        print(f"\n(dry-run) manifest would be: {manifest_path}")

    mode = "manifest-only" if args.manifest_only else ("dry-run" if args.dry_run else "full")
    print(f"\nsummary [{mode}]: {family} via {plan['route']} — {len(files_map)} file(s), status={status}")
    return 0 if status in ("downloaded", "pending", "dry-run") else 1


def _print_manual_fallback(family, weights, font_dir, project_root, css_url):
    s = slug(family)
    try:
        rel = font_dir.relative_to(project_root)
    except ValueError:
        rel = font_dir
    print(
        f"\n--- MANUAL FALLBACK: could not auto-source '{family}'. ---\n"
        f"1. Download the font's .ttf weights (e.g. from {css_url}\n"
        f"   or https://fonts.google.com/specimen/{family.replace(' ', '+')}).\n"
        f"2. Place them in: {rel}/ named:",
        file=sys.stderr,
    )
    for w in weights:
        print(f"     {s}_{NUM_TO_NAME[w]}.ttf  (weight {w})", file=sys.stderr)
    print("3. Wire into XTheme.kt XFontFamily():", file=sys.stderr)
    for w in weights:
        print(
            f"     Font(Res.font.{s}_{NUM_TO_NAME[w]}, FontWeight.{NUM_TO_FONTWEIGHT[w]}),",
            file=sys.stderr,
        )


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
