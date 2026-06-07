#!/usr/bin/env python3
"""
Stitch Asset Downloader (KMP-aware, two asset types, three modes)

One script for both Material Symbols icons and <img> assets. Type-specific
logic (HTML parsing, filename derivation, URL building, KMP cleanup) branches
on --type; shared logic (scope decision, source migration, DesignSystemResources
extension, Kotlin import rewriting) is type-agnostic.

USAGE
-----
python3 download_assets.py --type icons|images \\
  --feature {featurename} \\
  --project-root {repo_root} \\
  --html .../extracted/stitch_success.html \\
  [--html ... ...] \\
  [--manifest .../extracted/{icons|images}.json] \\
  [--manifest-only | --cleanup-only] \\
  [--dry-run]

ASSET TYPES
-----------
--type icons:
  - Parses <span class="material-symbols-{outlined|rounded|sharp}" data-icon="name">
  - Downloads XML vector drawables from google/material-design-icons
  - Applies KMP cleanup pass (strip android:tint, android:autoMirrored;
    translate EVERY @android:color/* ref — any color attribute — to literal
    ARGB hex, since the @android:color namespace crashes non-Android targets)
  - Filename: snake_case from data-icon (e.g. "arrow_back", "bolt_fill")
  - Manifest: icons.json

--type images:
  - Parses <img src="..." class="...">
  - Classifies each image's DELIVERY (see below): "bundled" or "remote".
  - "bundled" (static design asset: hero, decorative background, logo):
    downloads raster from the src URL (Stitch CDN), detects extension from
    Content-Type (PNG / JPEG / WebP), placed like icons (chrome/domain),
    rendered via painterResource. No KMP cleanup needed (raster format).
  - "remote" (dynamic content: avatar, flag, thumbnail, repeated list image):
    NOT downloaded, NOT bundled, no DesignSystemResources entry. Rendered at
    runtime via the design-system AsyncImage(url = <data field>) — the Stitch
    CDN URL is an ephemeral placeholder and must never ship. The manifest
    records a suggested `data_binding` field name + a generic placeholder ref.
  - Filename: {state}_{role}[_idx] from CSS heuristics
  - Manifest: images.json

DELIVERY CLASSIFICATION (images only)
-------------------------------------
Heuristic, per image (see _classify_image_delivery):
  - remote  if role in {avatar, thumbnail}, OR the image repeats >=2x with the
            same CSS class signature (data-bound collection), OR its alt text
            looks like an entity name (low confidence).
  - bundled if role in {background, hero} and not repeated.
  - else    bundled, low confidence (ambiguous — /ui-designer asks the user).
A user override is persisted with `delivery_locked: true` (set by /ui-designer
after the AskUserQuestion confirm); subsequent runs reuse the locked decision
verbatim instead of re-deriving it.

MODES (apply equally to both --type values)
-------------------------------------------
--manifest-only (used by /ui-designer):
  Only writes the {icons|images}.json manifest. NO source mutation, NO
  downloads. Updates other features' manifests on promotion (doc artifacts only).

--cleanup-only (used by /modifying-kmp-feature post-UI-agent):
  Sweeps feature/{f}/.../drawable/ for orphans (files whose ident is not in
  the manifest AND not referenced by any .kt). Safe deletion only.

default (full, used by /creating-kmp-feature and pre-UI-agent /modifying-):
  Downloads, applies KMP cleanup (icons only), extends DesignSystemResources.kt,
  inline-migrates stale references in other features.

SHARED ALGORITHM
----------------
Icons and BUNDLED images share the same usage-based placement (remote-delivery
images bypass it entirely — they are never downloaded or placed; see DELIVERY
CLASSIFICATION above):
  users = {features in any matching manifest declaring this asset} U {current}
  scope = "chrome" if len(users) >= 2 else "domain"
  - chrome → core/designsystem/.../composeResources/drawable/
  - domain → feature/{featurename}/.../composeResources/drawable/

Promotion (domain → chrome) is automatic in full mode and includes:
  - Place file at chrome path
  - Delete stale feature/X/drawable/{ident}.{ext} duplicates
  - Rewrite every .kt under feature/X/src/ from Res.drawable.{ident}
    to DesignSystemResources.drawable.{ident}
  - Extend core/designsystem/.../DesignSystemResources.kt

Dependencies: Python 3 stdlib only.
"""

import argparse
import json
import re
import sys
from dataclasses import dataclass
from html.parser import HTMLParser
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

USER_AGENT = "download-assets/1.0"
DOWNLOAD_TIMEOUT_S = 30

# Module/file conventions (template-standard, fixed across projects)
DESIGN_SYSTEM_MODULE_REL = Path("core") / "designsystem"
FEATURE_MODULE_PARENT_REL = Path("feature")

# Icons-specific
ICON_REPO_BASE = (
    "https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android"
)
ICON_DEFAULT_SIZE_PX = 24
ICON_DEFAULT_STYLE = "outlined"

# Images-specific (raster Content-Type → file extension)
RASTER_CONTENT_TYPE_EXT = {
    "image/png": "png",
    "image/jpeg": "jpg",
    "image/jpg": "jpg",
    "image/webp": "webp",
}


# ---------------------------------------------------------------------------
# Per-asset-type configuration
# ---------------------------------------------------------------------------


@dataclass(frozen=True)
class AssetType:
    name: str
    manifest_filename: str
    manifest_array_key: str
    extensions: tuple  # accepted file suffixes on disk (lowercased, with dot)


ICONS = AssetType(
    name="icons",
    manifest_filename="icons.json",
    manifest_array_key="icons",
    extensions=(".xml",),
)

IMAGES = AssetType(
    name="images",
    manifest_filename="images.json",
    manifest_array_key="images",
    extensions=(".png", ".jpg", ".jpeg", ".webp"),
)

ASSET_TYPES = {"icons": ICONS, "images": IMAGES}


# ---------------------------------------------------------------------------
# Shared path helpers (directory layout is template-standard)
# ---------------------------------------------------------------------------


def chrome_drawable_dir(project_root: Path) -> Path:
    return (
        project_root / DESIGN_SYSTEM_MODULE_REL / "src" / "commonMain"
        / "composeResources" / "drawable"
    )


def feature_drawable_dir(project_root: Path, featurename: str) -> Path:
    return (
        project_root / FEATURE_MODULE_PARENT_REL / featurename / "src" / "commonMain"
        / "composeResources" / "drawable"
    )


def feature_kotlin_root(project_root: Path, featurename: str) -> Path:
    return project_root / FEATURE_MODULE_PARENT_REL / featurename / "src"


def state_from_html_path(path: Path) -> str:
    m = re.search(r"stitch_(?P<state>[a-z]+)\.html$", path.name)
    return m.group("state") if m else path.stem


# ---------------------------------------------------------------------------
# Project-specific package discovery (auto-detected, not hardcoded)
# ---------------------------------------------------------------------------


@dataclass(frozen=True)
class ProjectPackages:
    """All project-specific package names + file paths derived once at startup.

    Derived from `core/designsystem/.../DesignSystemResources.kt`:
      - design_system_package: the `package` declaration in DSR.kt
        e.g. "thisissadeghi.designsystem"
      - design_system_generated_pkg: the import target for the design system's
        generated Res object (e.g. "kmpilot.core.designsystem.generated.resources")
      - project_namespace: the leading segment of the generated package
        (e.g. "kmpilot"); used to construct feature generated packages.
      - design_system_resources_file: absolute path to DSR.kt
    """
    design_system_package: str
    design_system_generated_pkg: str
    project_namespace: str
    design_system_resources_file: Path

    def feature_generated_res_package(self, featurename: str) -> str:
        return f"{self.project_namespace}.feature.{featurename}.generated.resources"


def discover_project_packages(project_root: Path) -> ProjectPackages:
    """Find DesignSystemResources.kt under core/designsystem and read its
    package + generated-resources import to derive all project-specific names.

    The script never falls back to hardcoded defaults — if DSR.kt is missing or
    its imports don't match the compose-resources convention, this fails fast.
    """
    ds_kotlin_root = project_root / DESIGN_SYSTEM_MODULE_REL / "src" / "commonMain" / "kotlin"
    candidates = []
    if ds_kotlin_root.is_dir():
        candidates = [p for p in ds_kotlin_root.rglob("DesignSystemResources.kt")
                      if "/build/" not in str(p)]
    if not candidates:
        # Fallback: scan whole design system module (excluding build/)
        ds_module = project_root / DESIGN_SYSTEM_MODULE_REL
        if ds_module.is_dir():
            candidates = [p for p in ds_module.rglob("DesignSystemResources.kt")
                          if "/build/" not in str(p)]
    if not candidates:
        raise SystemExit(
            f"error: DesignSystemResources.kt not found under {DESIGN_SYSTEM_MODULE_REL}/. "
            "Cannot derive project packages. This script requires the design system "
            "module to exist with a DesignSystemResources.kt file."
        )
    if len(candidates) > 1:
        print(
            f"warning: multiple DesignSystemResources.kt candidates found; using {candidates[0]}",
            file=sys.stderr,
        )
    dsr = candidates[0]
    content = dsr.read_text(encoding="utf-8")

    pkg_match = re.search(r"^package\s+([\w.]+)", content, flags=re.MULTILINE)
    if not pkg_match:
        raise SystemExit(f"error: no `package` declaration in {dsr.relative_to(project_root)}")
    design_system_package = pkg_match.group(1)

    res_import_match = re.search(
        r"^import\s+([\w.]+)\.generated\.resources\.Res\s*$",
        content,
        flags=re.MULTILINE,
    )
    if not res_import_match:
        raise SystemExit(
            f"error: no `import …generated.resources.Res` line in "
            f"{dsr.relative_to(project_root)}. "
            "Cannot derive design system generated resource package. Add a `Res` "
            "import (e.g. from `compose.components.resources` generated accessors) "
            "and retry."
        )
    design_system_generated_pkg = f"{res_import_match.group(1)}.generated.resources"
    project_namespace = design_system_generated_pkg.split(".", 1)[0]

    return ProjectPackages(
        design_system_package=design_system_package,
        design_system_generated_pkg=design_system_generated_pkg,
        project_namespace=project_namespace,
        design_system_resources_file=dsr,
    )


# ---------------------------------------------------------------------------
# Shared: Kotlin import editing
# ---------------------------------------------------------------------------


def _remove_import_line(source: str, fqcn: str) -> str:
    pattern = re.compile(rf"^import {re.escape(fqcn)}\s*\r?\n", flags=re.MULTILINE)
    return pattern.sub("", source)


def _ensure_import_line(source: str, fqcn: str) -> str:
    if re.search(rf"^import {re.escape(fqcn)}\s*$", source, flags=re.MULTILINE):
        return source
    import_lines = list(re.finditer(r"^import\s+([^\s]+)\s*$", source, flags=re.MULTILINE))
    if not import_lines:
        pkg_match = re.search(r"^package\s+[^\n]+\n", source, flags=re.MULTILINE)
        if pkg_match:
            insert_at = pkg_match.end()
            return source[:insert_at] + f"\nimport {fqcn}\n" + source[insert_at:]
        return f"import {fqcn}\n\n" + source
    existing = [(m.start(), m.end(), m.group(1)) for m in import_lines]
    for start, end, name in existing:
        if name > fqcn:
            return source[:start] + f"import {fqcn}\n" + source[start:]
    last_end = existing[-1][1]
    return source[:last_end] + f"\nimport {fqcn}" + source[last_end:]


def rewrite_feature_kotlin(
    kt_root: Path,
    feature: str,
    ident: str,
    *,
    packages: ProjectPackages,
    dry_run: bool,
):
    """Rewrite Res.drawable.{ident} -> DesignSystemResources.drawable.{ident} across a feature."""
    actions = []
    gen_pkg = packages.feature_generated_res_package(feature)
    use_re = re.compile(rf"\bRes\.drawable\.{re.escape(ident)}\b")

    for kt_path in kt_root.rglob("*.kt"):
        try:
            original = kt_path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        if not use_re.search(original):
            continue
        new_text = use_re.sub(f"DesignSystemResources.drawable.{ident}", original)
        new_text = _remove_import_line(new_text, f"{gen_pkg}.{ident}")
        any_other_res = re.search(r"\bRes\.[A-Za-z_][A-Za-z0-9_]*", new_text)
        if not any_other_res:
            new_text = _remove_import_line(new_text, f"{gen_pkg}.Res")
        new_text = _ensure_import_line(
            new_text, f"{packages.design_system_package}.DesignSystemResources"
        )
        if new_text != original:
            if dry_run:
                actions.append((kt_path, "would-rewrite"))
            else:
                kt_path.write_text(new_text, encoding="utf-8")
                actions.append((kt_path, "rewritten"))
    return actions


# ---------------------------------------------------------------------------
# Shared: DesignSystemResources.kt auto-extension
# ---------------------------------------------------------------------------


def extend_design_system_resources(
    project_root: Path,
    chrome_idents: list,
    *,
    packages: ProjectPackages,
    dry_run: bool,
):
    target = packages.design_system_resources_file
    if not target.is_file():
        print(
            f"warning: {target.relative_to(project_root)} not found; "
            "skipping DesignSystemResources auto-extension.",
            file=sys.stderr,
        )
        return ("missing", 0)
    source = target.read_text(encoding="utf-8")
    added = 0
    for ident in chrome_idents:
        if re.search(
            rf"\bval {re.escape(ident)}\s*=\s*Res\.drawable\.{re.escape(ident)}\b", source
        ):
            continue
        source = _add_design_system_drawable_entry(source, ident, packages=packages)
        added += 1
    if added == 0:
        return ("up-to-date", 0)
    if dry_run:
        return ("would-update", added)
    target.write_text(source, encoding="utf-8")
    return ("updated", added)


def _add_design_system_drawable_entry(source: str, ident: str, *, packages: ProjectPackages) -> str:
    source = _ensure_import_line(source, f"{packages.design_system_generated_pkg}.{ident}")
    drawable_block_re = re.compile(
        r"(object\s+drawable\s*\{)([\s\S]*?)(\n\s*\})", flags=re.MULTILINE
    )
    m = drawable_block_re.search(source)
    if not m:
        return source
    header, body, footer = m.group(1), m.group(2), m.group(3)
    new_entry_line = f"        val {ident} = Res.drawable.{ident}"
    val_lines = re.findall(r"^\s*val\s+([A-Za-z_][A-Za-z0-9_]*)\b", body, flags=re.MULTILINE)
    if ident in val_lines:
        return source
    new_body_lines = []
    inserted = False
    for line in body.splitlines(keepends=True):
        m_val = re.match(r"^\s*val\s+([A-Za-z_][A-Za-z0-9_]*)\b", line)
        if m_val and not inserted and m_val.group(1) > ident:
            new_body_lines.append(new_entry_line + "\n")
            inserted = True
        new_body_lines.append(line)
    if not inserted:
        if not body.endswith("\n"):
            new_body_lines.append("\n")
        new_body_lines.append(new_entry_line + "\n")
    return source[: m.start()] + header + "".join(new_body_lines) + footer + source[m.end():]


# ---------------------------------------------------------------------------
# Per-type HTML parsers
# ---------------------------------------------------------------------------


class _IconSpanFinder(HTMLParser):
    """<span class="material-symbols-{style}" data-icon="..." [data-weight="fill"]>."""

    _CLASS_RE = re.compile(r"material-symbols-(outlined|rounded|sharp)")

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.hits = []

    def handle_starttag(self, tag, attrs):
        if tag != "span":
            return
        attr_map = {k: (v or "") for k, v in attrs}
        m = self._CLASS_RE.search(attr_map.get("class", ""))
        if not m:
            return
        name = attr_map.get("data-icon", "").strip()
        if not name:
            return
        self.hits.append({
            "name": name,
            "style": m.group(1),
            "filled": attr_map.get("data-weight", "").strip().lower() == "fill",
        })


class _ImgFinder(HTMLParser):
    """<img src="..."> with ancestor <div class chain and preceding HTML comment."""

    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.hits = []
        self._div_class_stack = []
        self._last_comment = None

    def handle_starttag(self, tag, attrs):
        attr_map = {k: (v or "") for k, v in attrs}
        if tag == "div":
            self._div_class_stack.append(attr_map.get("class", ""))
            return
        if tag == "img":
            src = attr_map.get("src", "").strip()
            if not src:
                return
            self.hits.append({
                "src": src,
                "alt": (attr_map.get("data-alt") or attr_map.get("alt") or "").strip(),
                "img_classes": attr_map.get("class", ""),
                "ancestor_classes": list(self._div_class_stack),
                "preceding_comment": self._last_comment,
            })
            self._last_comment = None

    def handle_endtag(self, tag):
        if tag == "div" and self._div_class_stack:
            self._div_class_stack.pop()

    def handle_startendtag(self, tag, attrs):
        self.handle_starttag(tag, attrs)

    def handle_comment(self, data):
        self._last_comment = data.strip()


def parse_icons_from_html(html_text):
    p = _IconSpanFinder()
    try:
        p.feed(html_text)
    finally:
        p.close()
    return p.hits


def parse_images_from_html(html_text):
    p = _ImgFinder()
    try:
        p.feed(html_text)
    finally:
        p.close()
    return p.hits


# ---------------------------------------------------------------------------
# Per-type ident derivation
# ---------------------------------------------------------------------------


def icon_ident(hit) -> str:
    """data-icon snake_case, plus _fill suffix when data-weight=fill."""
    return f"{hit['name']}_fill" if hit["filled"] else hit["name"]


_IMG_ABSOLUTE_RE = re.compile(r"\b(absolute|fixed)\b")
_IMG_POINTER_NONE_RE = re.compile(r"\bpointer-events-none\b")
_IMG_LOW_OPACITY_RE = re.compile(r"\bopacity-(?:[0-4]?\d)\b")
_IMG_ROUNDED_FULL_RE = re.compile(r"\brounded-full\b")
_IMG_SMALL_W_RE = re.compile(r"\bw-(8|10|12|14|16)\b")
_IMG_ASPECT_SQUARE_RE = re.compile(r"\baspect-square\b")
_IMG_W_FULL_RE = re.compile(r"\bw-full\b")


def _infer_image_role(hit) -> str:
    all_classes = " ".join(hit["ancestor_classes"]) + " " + hit["img_classes"]
    comment = (hit.get("preceding_comment") or "").lower()
    if any(tok in comment for tok in ("decorative", "background", "texture")):
        return "background"
    if "hero" in comment:
        return "hero"
    if "avatar" in comment:
        return "avatar"
    if "thumbnail" in comment or "thumb" in comment:
        return "thumbnail"
    is_absolute = bool(_IMG_ABSOLUTE_RE.search(all_classes))
    if is_absolute and (
        _IMG_POINTER_NONE_RE.search(all_classes) or _IMG_LOW_OPACITY_RE.search(all_classes)
    ):
        return "background"
    if _IMG_ROUNDED_FULL_RE.search(all_classes) and _IMG_SMALL_W_RE.search(all_classes):
        return "avatar"
    if _IMG_ASPECT_SQUARE_RE.search(all_classes) and _IMG_SMALL_W_RE.search(all_classes):
        return "thumbnail"
    if _IMG_W_FULL_RE.search(all_classes) and not is_absolute:
        return "hero"
    return "image"


def image_ident(hit, used: set, *, state: str):
    """{state}_{role}[_idx]; idx disambiguates per-(state,role) collisions within a run."""
    role = _infer_image_role(hit)
    base = f"{state}_{role}"
    if base not in used:
        return base, role
    i = 1
    while f"{base}_{i}" in used:
        i += 1
    return f"{base}_{i}", role


# ---------------------------------------------------------------------------
# Image delivery classification (bundled = drawable; remote = AsyncImage)
# ---------------------------------------------------------------------------

# Generic placeholder used as AsyncImage's loadingResId for every remote image.
# Authored once in the design system (literal-hex vector, no @android:color).
REMOTE_PLACEHOLDER_REF = "DesignSystemResources.drawable.ds_image_placeholder"

# Words that mark an alt as a scene/description (static asset), not an entity name.
_ALT_SCENE_WORDS = {
    "background", "texture", "pattern", "gradient", "abstract", "scene",
    "illustration", "banner", "backdrop", "at", "night", "day", "sunset",
    "sunrise", "blurred", "decorative",
}

# alt keyword -> suggested data-binding field name (singular, runtime URL).
_ALT_BINDING_KEYWORDS = (
    "flag", "logo", "avatar", "photo", "picture", "banner", "cover", "icon",
    "poster", "thumbnail",
)


def _img_signature(hit) -> str:
    """Normalized class signature used to detect repeated (data-bound) images.

    Flags/avatars in a list share an identical <img class="..."> even though
    each has a distinct src URL — collapsing whitespace + lowercasing groups
    them so a >=2 count signals a runtime collection."""
    return re.sub(r"\s+", " ", (hit.get("img_classes") or "").strip()).lower()


def _looks_like_entity_name(alt: str) -> bool:
    """True when alt reads like a short proper noun (e.g. 'Brazil Flag',
    'Korea Republic') rather than a scene description ('stadium at night')."""
    a = (alt or "").strip()
    if not a:
        return False
    words = a.split()
    if len(words) > 3:
        return False
    low_words = a.lower().split()
    if any(w in _ALT_SCENE_WORDS for w in low_words):
        return False
    return any(w[:1].isupper() for w in words)


def _classify_image_delivery(role: str, alt: str, repeat_count: int):
    """Return (delivery, confidence, reason). delivery in {'bundled','remote'}."""
    if role in ("avatar", "thumbnail"):
        return "remote", "high", f"role={role} (runtime content)"
    if repeat_count >= 2:
        return "remote", "high", f"repeated x{repeat_count} (data-bound collection)"
    if role in ("background", "hero"):
        return "bundled", "high", f"role={role} (static design asset)"
    if _looks_like_entity_name(alt):
        return "remote", "low", f"alt looks like an entity name: {alt!r}"
    return "bundled", "low", "ambiguous — defaulted to bundled (confirm in /ui-designer)"


def _suggest_data_binding(role: str, alt: str) -> str:
    """Suggested *UiModel/DTO field name for a remote image's runtime URL.
    A specific alt keyword (e.g. 'Brazil Flag' -> flagUrl) wins over the
    role-derived default (avatar -> avatarUrl)."""
    low = (alt or "").lower()
    for kw in _ALT_BINDING_KEYWORDS:
        if kw in low:
            return f"{kw}Url"
    if role == "avatar":
        return "avatarUrl"
    if role == "thumbnail":
        return "thumbnailUrl"
    return "imageUrl"


def read_locked_deliveries(manifest_path: Path, asset_type: AssetType) -> dict:
    """Return {source_url: {delivery, data_binding}} for prior-manifest entries
    flagged `delivery_locked: true` (a user-confirmed choice from /ui-designer).
    Lets full-mode re-runs honor the user's bundle/remote decision instead of
    re-deriving it from the HTML. Read-only; images only."""
    out: dict = {}
    if asset_type.name != "images" or not manifest_path.is_file():
        return out
    try:
        data = json.loads(manifest_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError):
        return out
    for e in data.get("images", []):
        if not e.get("delivery_locked"):
            continue
        url = e.get("source_url") or e.get("url")
        if url and e.get("delivery") in ("bundled", "remote"):
            out[url] = {"delivery": e["delivery"], "data_binding": e.get("data_binding")}
    return out


# ---------------------------------------------------------------------------
# Per-type URL builder (icons only — images use their src directly)
# ---------------------------------------------------------------------------


def build_icon_url(hit, size_px: int = ICON_DEFAULT_SIZE_PX) -> str:
    style_dir = f"materialsymbols{hit['style']}"
    fill_suffix = "_fill1" if hit["filled"] else ""
    return f"{ICON_REPO_BASE}/{hit['name']}/{style_dir}/{hit['name']}{fill_suffix}_{size_px}px.xml"


# ---------------------------------------------------------------------------
# Per-type KMP cleanup (icons only)
# ---------------------------------------------------------------------------


_XML_TINT_RE = re.compile(r'\s+android:tint="[^"]*"')
_XML_AUTOMIRRORED_RE = re.compile(r'\s+android:autoMirrored="[^"]*"')
# Any "@android:color/<name>" used as an attribute value, in ANY color attribute
# (android:fillColor, android:strokeColor, app:tint, …). The @android:color
# namespace is Android-only and is NOT resolved by the Compose Multiplatform
# resource pipeline — leaving it in crashes at runtime on non-Android targets.
_XML_ANDROID_COLOR_REF_RE = re.compile(r'"@android:color/([A-Za-z0-9_]+)"')

# Literal ARGB hex for the public android.R.color.* platform colors
# (frameworks/base .../res/values/colors.xml). Used to translate any leftover
# "@android:color/<name>" reference to a value the KMP pipeline can parse.
# Unmapped names fall back to opaque black (visible default; icons are tinted
# by XIcon at render time, so the literal value is cosmetic for Material Symbols).
_ANDROID_PLATFORM_COLORS = {
    "white": "#FFFFFFFF",
    "black": "#FF000000",
    "transparent": "#00000000",
    "background_light": "#FFFFFFFF",
    "background_dark": "#FF000000",
    "darker_gray": "#FFAAAAAA",
    "primary_text_dark": "#FFFFFFFF",
    "primary_text_light": "#FF000000",
    "secondary_text_dark": "#FFBEBEBE",
    "secondary_text_light": "#FF323232",
    "holo_blue_light": "#FF33B5E5",
    "holo_blue_dark": "#FF0099CC",
    "holo_green_light": "#FF99CC00",
    "holo_green_dark": "#FF669900",
    "holo_red_light": "#FFFF4444",
    "holo_red_dark": "#FFCC0000",
    "holo_orange_light": "#FFFFBB33",
    "holo_orange_dark": "#FFFF8800",
    "holo_purple": "#FFAA66CC",
}
_ANDROID_COLOR_FALLBACK = "#FF000000"


def _resolve_android_color(match: "re.Match[str]") -> str:
    name = match.group(1)
    return '"' + _ANDROID_PLATFORM_COLORS.get(name, _ANDROID_COLOR_FALLBACK) + '"'


def clean_icon_xml_for_kmp(raw: bytes) -> bytes:
    text = raw.decode("utf-8")
    text = _XML_TINT_RE.sub("", text)
    text = _XML_AUTOMIRRORED_RE.sub("", text)
    # Translate EVERY @android:color/* ref (any color attribute) to literal hex.
    text = _XML_ANDROID_COLOR_REF_RE.sub(_resolve_android_color, text)
    return text.encode("utf-8")


# ---------------------------------------------------------------------------
# Download (per-type cleanup applied internally)
# ---------------------------------------------------------------------------


def fetch_and_save(url: str, dest_no_ext: Path, asset_type: AssetType, *, dry_run: bool):
    """Returns (ok, status, ext). Idempotent skip when any matching file exists."""
    for existing in dest_no_ext.parent.glob(f"{dest_no_ext.name}.*"):
        if existing.suffix.lower() in asset_type.extensions:
            return True, "skip-exists", existing.suffix.lstrip(".")

    if dry_run:
        return True, "dry-run", "pending"

    try:
        req = Request(url, headers={"User-Agent": USER_AGENT})
        with urlopen(req, timeout=DOWNLOAD_TIMEOUT_S) as resp:
            body = resp.read()
            content_type = (resp.getheader("Content-Type") or "").split(";")[0].strip().lower()
    except HTTPError as e:
        return False, f"http-{e.code}", "unknown"
    except URLError as e:
        return False, f"url-error:{e.reason}", "unknown"
    except TimeoutError:
        return False, "timeout", "unknown"

    if asset_type.name == "icons":
        body = clean_icon_xml_for_kmp(body)
        ext = "xml"
    else:
        ext = RASTER_CONTENT_TYPE_EXT.get(content_type)
        if not ext:
            return False, f"unrecognised-content-type:{content_type}", "unknown"

    dest = dest_no_ext.with_suffix(f".{ext}")
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(body)
    return True, "downloaded", ext


# ---------------------------------------------------------------------------
# Cross-feature manifest scan + promotion
# ---------------------------------------------------------------------------


def scan_existing_manifests(project_root: Path, current_feature: str, asset_type: AssetType):
    """Returns {dedup_key -> set(feature_names)}. Skips current feature's own manifest."""
    docs_root = project_root / ".claude" / "docs"
    usage = {}
    if not docs_root.is_dir():
        return usage
    for manifest_path in docs_root.glob(f"*/designs/extracted/{asset_type.manifest_filename}"):
        feature = manifest_path.parts[-4]
        if feature == current_feature:
            continue
        try:
            data = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as e:
            print(f"warning: could not read {manifest_path}: {e}", file=sys.stderr)
            continue
        for entry in data.get(asset_type.manifest_array_key, []):
            if asset_type.name == "icons":
                key = (
                    entry.get("name"),
                    entry.get("style", ICON_DEFAULT_STYLE),
                    bool(entry.get("filled")),
                )
            else:
                # Accept either 'source_url' (new) or 'url' (older format)
                key = entry.get("source_url") or entry.get("url")
            if key and (asset_type.name != "icons" or key[0]):
                usage.setdefault(key, set()).add(feature)
    return usage


def manifest_promote(
    project_root: Path,
    ident: str,
    affected_features: list,
    asset_type: AssetType,
    *,
    dry_run: bool,
):
    """Doc-artifact-only promotion. Updates affected features' manifests in place."""
    actions = []
    for feature in affected_features:
        manifest_path = (
            project_root / ".claude" / "docs" / feature / "designs"
            / "extracted" / asset_type.manifest_filename
        )
        if not manifest_path.is_file():
            continue
        if dry_run:
            actions.append((manifest_path, "would-update-manifest"))
            continue
        data = json.loads(manifest_path.read_text(encoding="utf-8"))
        for entry in data.get(asset_type.manifest_array_key, []):
            if entry.get("drawable_name") != ident:
                continue
            entry["scope"] = "chrome"
            ext = entry.get("extension", "xml" if asset_type.name == "icons" else "unknown")
            if ext == "unknown":
                entry["drawable_path"] = str(
                    (chrome_drawable_dir(project_root) / ident).relative_to(project_root)
                )
            else:
                entry["drawable_path"] = str(
                    (chrome_drawable_dir(project_root) / f"{ident}.{ext}").relative_to(project_root)
                )
            entry["res_reference"] = f"DesignSystemResources.drawable.{ident}"
        manifest_path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
        actions.append((manifest_path, "manifest-updated"))
    return actions


def source_promote(
    project_root: Path,
    ident: str,
    affected_features: list,
    asset_type: AssetType,
    *,
    packages: ProjectPackages,
    dry_run: bool,
):
    """Source-tree promotion: delete stale files + rewrite Kotlin imports."""
    actions = []
    for feature in affected_features:
        feature_dir = feature_drawable_dir(project_root, feature)
        if feature_dir.is_dir():
            for stale in feature_dir.glob(f"{ident}.*"):
                if stale.suffix.lower() not in asset_type.extensions:
                    continue
                if dry_run:
                    actions.append((stale, "would-delete"))
                else:
                    stale.unlink()
                    actions.append((stale, "deleted"))
        kt_root = feature_kotlin_root(project_root, feature)
        if kt_root.is_dir():
            actions.extend(
                rewrite_feature_kotlin(kt_root, feature, ident, packages=packages, dry_run=dry_run)
            )
    return actions


def detect_existing_extension(dest_no_ext: Path, asset_type: AssetType) -> str:
    """If a matching file already exists at dest_no_ext.*, return its extension
    (sans dot). Otherwise 'unknown'. Used by manifest-only mode so that running
    after a prior full-mode materialization preserves the known extension."""
    if dest_no_ext.parent.is_dir():
        for existing in dest_no_ext.parent.glob(f"{dest_no_ext.name}.*"):
            if existing.suffix.lower() in asset_type.extensions:
                return existing.suffix.lstrip(".")
    return "unknown"


# ---------------------------------------------------------------------------
# Orphan cleanup (cleanup-only mode)
# ---------------------------------------------------------------------------


def cleanup_feature_orphans(
    project_root: Path,
    featurename: str,
    asset_type: AssetType,
    *,
    dry_run: bool,
):
    actions = []
    feature_dir = feature_drawable_dir(project_root, featurename)
    if not feature_dir.is_dir():
        return actions

    manifest_path = (
        project_root / ".claude" / "docs" / featurename / "designs"
        / "extracted" / asset_type.manifest_filename
    )
    if not manifest_path.is_file():
        print(
            f"warning: cleanup-only requested for '{featurename}' but no "
            f"{asset_type.manifest_filename} at {manifest_path.relative_to(project_root)} — "
            "nothing to compare against; skipping.",
            file=sys.stderr,
        )
        return actions

    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        print(f"error: {asset_type.manifest_filename} is not valid JSON: {e}", file=sys.stderr)
        return actions

    # Remote-delivery images declare no on-disk drawable, so they must NOT
    # protect a file: flipping an image bundled -> remote leaves its old raster
    # as an orphan to be reclaimed here (subject to the still-referenced guard).
    declared_idents = {
        e.get("drawable_name") for e in manifest.get(asset_type.manifest_array_key, [])
        if e.get("drawable_name") and e.get("delivery") != "remote"
    }
    kt_root = feature_kotlin_root(project_root, featurename)

    for path in sorted(feature_dir.iterdir()):
        if path.suffix.lower() not in asset_type.extensions:
            continue
        ident = path.stem
        if ident in declared_idents:
            continue
        if kt_root.is_dir():
            ref_re = re.compile(rf"\bRes\.drawable\.{re.escape(ident)}\b")
            still_referenced = False
            for kt_path in kt_root.rglob("*.kt"):
                try:
                    if ref_re.search(kt_path.read_text(encoding="utf-8")):
                        still_referenced = True
                        break
                except UnicodeDecodeError:
                    continue
            if still_referenced:
                actions.append((path, "skipped-orphan-still-referenced"))
                continue
        if dry_run:
            actions.append((path, "would-delete-orphan"))
        else:
            path.unlink()
            actions.append((path, "deleted-orphan"))
    return actions


# ---------------------------------------------------------------------------
# Manifest writer
# ---------------------------------------------------------------------------


def write_manifest(path: Path, feature: str, entries: list, asset_type: AssetType):
    payload = {
        "feature": feature,
        "type": asset_type.name,
        asset_type.manifest_array_key: entries,
        "generator": "download_assets.py",
        "version": 1,
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


def main(argv):
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    ap.add_argument(
        "--type", choices=list(ASSET_TYPES.keys()), required=True,
        help="Asset type: 'icons' (Material Symbols XML) or 'images' (raster).",
    )
    ap.add_argument("--feature", required=True)
    ap.add_argument("--project-root", required=True, type=Path)
    ap.add_argument("--html", action="append", required=True, type=Path)
    ap.add_argument("--manifest", type=Path, default=None)
    ap.add_argument("--manifest-only", action="store_true")
    ap.add_argument("--cleanup-only", action="store_true")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args(argv)

    if args.manifest_only and args.cleanup_only:
        print("error: --manifest-only and --cleanup-only are mutually exclusive.", file=sys.stderr)
        return 2

    asset_type = ASSET_TYPES[args.type]
    project_root = args.project_root.resolve()
    if not project_root.is_dir():
        print(f"error: --project-root not a directory: {project_root}", file=sys.stderr)
        return 2

    # Auto-discover project-specific package names from DesignSystemResources.kt.
    # The script never falls back to hardcoded defaults — different projects use
    # different namespaces (e.g. "kmpilot.core.designsystem" vs "myorg.ds").
    packages = discover_project_packages(project_root)

    # cleanup-only short-circuit
    if args.cleanup_only:
        actions = cleanup_feature_orphans(
            project_root, args.feature, asset_type, dry_run=args.dry_run
        )
        if not actions:
            print(
                f"summary [cleanup-only --type {args.type}]: "
                f"no orphans in feature/{args.feature}/drawable/."
            )
        else:
            for path, action in actions:
                print(f"  [{action}] {path.relative_to(project_root)}")
            deleted = sum(1 for _, a in actions if a in ("deleted-orphan", "would-delete-orphan"))
            skipped = sum(1 for _, a in actions if a == "skipped-orphan-still-referenced")
            print(
                f"\nsummary [cleanup-only --type {args.type}]: "
                f"{deleted} orphan(s) deleted, {skipped} preserved."
            )
        return 0

    # Parse + dedup per asset type
    if asset_type.name == "icons":
        current = {}  # (name, style, filled) -> [{state, count}, ...]
        for html_path in args.html:
            if not html_path.is_file():
                print(f"error: --html not a file: {html_path}", file=sys.stderr)
                return 2
            state = state_from_html_path(html_path)
            per_state = {}
            for hit in parse_icons_from_html(html_path.read_text(encoding="utf-8")):
                k = (hit["name"], hit["style"], hit["filled"])
                per_state[k] = per_state.get(k, 0) + 1
            for k, cnt in per_state.items():
                current.setdefault(k, []).append({"state": state, "count": cnt})
    else:  # images
        current = {}  # src -> {hit, primary_state, occurrences}
        for html_path in args.html:
            if not html_path.is_file():
                print(f"error: --html not a file: {html_path}", file=sys.stderr)
                return 2
            state = state_from_html_path(html_path)
            for hit in parse_images_from_html(html_path.read_text(encoding="utf-8")):
                url = hit["src"]
                if url not in current:
                    current[url] = {"hit": hit, "primary_state": state, "occurrences": []}
                current[url]["occurrences"].append({"state": state})

    if not current:
        kind = "Material Symbols spans" if asset_type.name == "icons" else "<img> tags"
        print(f"warning: no {kind} found in any input HTML.", file=sys.stderr)

    cross_feature_usage = scan_existing_manifests(project_root, args.feature, asset_type)

    # Resolve the manifest path up front so we can honor user-locked delivery
    # choices from a prior /ui-designer run (images only; read-only).
    manifest_path = args.manifest or (args.html[0].parent / asset_type.manifest_filename)
    locked_deliveries = read_locked_deliveries(manifest_path, asset_type)

    # Count how many distinct images share each CSS class signature — a >=2
    # group is a data-bound collection (flags/avatars in a list) -> remote.
    repeat_counts: dict = {}
    if asset_type.name == "images":
        for inf in current.values():
            sig = _img_signature(inf["hit"])
            repeat_counts[sig] = repeat_counts.get(sig, 0) + 1

    entries = []
    any_failed = False
    chrome_idents_added = []
    source_migration_actions = []
    manifest_migration_actions = []
    used_idents = set()
    mode_label = "manifest-only" if args.manifest_only else "full"

    for key, info in sorted(current.items()):
        delivery = None  # icons are always bundled; only images carry a delivery
        delivery_conf = delivery_reason = data_binding = None
        locked = None
        if asset_type.name == "icons":
            name, style, filled = key
            hit = {"name": name, "style": style, "filled": filled}
            ident = icon_ident(hit)
            url = build_icon_url(hit)
            type_specific = {"name": name, "style": style, "filled": filled}
            occurrences = info
            if style != ICON_DEFAULT_STYLE:
                print(
                    f"warning: icon '{name}' uses style '{style}' (not '{ICON_DEFAULT_STYLE}'). "
                    "Verify intentional divergence from Stitch's outlined default.",
                    file=sys.stderr,
                )
            label_role = ""
        else:
            url = key
            hit = info["hit"]
            state = info["primary_state"]
            ident, role = image_ident(hit, used_idents, state=state)
            type_specific = {"role": role, "state": state, "alt": hit["alt"]}
            occurrences = info["occurrences"]
            label_role = f" ({role:10s})"
            locked = locked_deliveries.get(url)
            if locked:
                delivery = locked["delivery"]
                delivery_conf = "high"
                delivery_reason = "locked (user-confirmed in /ui-designer)"
                data_binding = locked.get("data_binding")
            else:
                delivery, delivery_conf, delivery_reason = _classify_image_delivery(
                    role, hit["alt"], repeat_counts.get(_img_signature(hit), 1)
                )
            if delivery == "remote" and not data_binding:
                data_binding = _suggest_data_binding(role, hit["alt"])

        used_idents.add(ident)

        # --- Remote images: no download, no scope / promotion / DSR entry. ---
        # Rendered at runtime via AsyncImage(url = <data field>); the Stitch CDN
        # URL is an ephemeral placeholder and is never bundled or shipped.
        if delivery == "remote":
            entries.append({
                **type_specific,
                "delivery": "remote",
                "delivery_confidence": delivery_conf,
                "delivery_reason": delivery_reason,
                "delivery_locked": bool(locked),
                "data_binding": data_binding,
                "compose_hint": "AsyncImage",
                "placeholder_ref": REMOTE_PLACEHOLDER_REF,
                "scope": "remote",
                "drawable_name": ident,
                "extension": "n/a",
                "drawable_path": None,
                "res_reference": None,
                "source_url": url,
                "download_status": "remote",
                "usage_count": 1,
                "users": [args.feature],
                "occurrences": occurrences,
            })
            print(
                f"  [remote] {ident:30s}{label_role} -> AsyncImage(url = {data_binding})  "
                f"[skip-download]  ({delivery_reason})"
            )
            continue

        # --- Bundled (all icons + bundled images): download + place + DSR. ---
        users = set(cross_feature_usage.get(key, set()))
        users.add(args.feature)
        scope = "chrome" if len(users) >= 2 else "domain"

        dest_no_ext = (
            chrome_drawable_dir(project_root) / ident
            if scope == "chrome"
            else feature_drawable_dir(project_root, args.feature) / ident
        )

        if args.manifest_only:
            status = "pending"
            # If a prior full-mode run already materialized this asset, detect
            # the existing extension so the manifest stays accurate; otherwise
            # leave as "unknown" until full mode resolves it.
            ext = detect_existing_extension(dest_no_ext, asset_type)
        else:
            ok, status, ext = fetch_and_save(url, dest_no_ext, asset_type, dry_run=args.dry_run)
            if not ok:
                any_failed = True

        if scope == "chrome":
            stale_features = [f for f in users if f != args.feature]
            if stale_features:
                manifest_migration_actions.extend(
                    manifest_promote(
                        project_root, ident, stale_features, asset_type, dry_run=args.dry_run
                    )
                )
                if not args.manifest_only:
                    source_migration_actions.extend(
                        source_promote(
                            project_root, ident, stale_features, asset_type,
                            packages=packages, dry_run=args.dry_run,
                        )
                    )
            chrome_idents_added.append(ident)

        res_reference = (
            f"DesignSystemResources.drawable.{ident}" if scope == "chrome"
            else f"Res.drawable.{ident}"
        )
        drawable_path_str = (
            str(dest_no_ext.with_suffix(f".{ext}").relative_to(project_root))
            if ext != "unknown"
            else str(dest_no_ext.relative_to(project_root))
        )

        entry = {
            **type_specific,
            "scope": scope,
            "drawable_name": ident,
            "extension": ext,
            "drawable_path": drawable_path_str,
            "res_reference": res_reference,
            "source_url": url,
            "download_status": status,
            "usage_count": len(users),
            "users": sorted(users),
            "occurrences": occurrences,
        }
        if asset_type.name == "images":
            entry.update({
                "delivery": "bundled",
                "delivery_confidence": delivery_conf,
                "delivery_reason": delivery_reason,
                "delivery_locked": bool(locked),
            })
        entries.append(entry)

        print(
            f"  [{scope:6s}] {ident:30s}{label_role} -> {drawable_path_str}  "
            f"[{status}]  users={len(users)}"
        )

    if not args.manifest_only and chrome_idents_added:
        ds_action, count = extend_design_system_resources(
            project_root, chrome_idents_added, packages=packages, dry_run=args.dry_run
        )
        if count > 0 or ds_action == "missing":
            print(f"\nDesignSystemResources.kt: {ds_action} ({count} entries)")

    if manifest_migration_actions:
        print("\nManifest promotions (doc artifacts):")
        for path, action in manifest_migration_actions:
            print(f"  [{action}] {path.relative_to(project_root)}")
    if source_migration_actions:
        print("\nSource migrations:")
        for path, action in source_migration_actions:
            print(f"  [{action}] {path.relative_to(project_root)}")

    if not args.dry_run:
        write_manifest(manifest_path, args.feature, entries, asset_type)
        try:
            print(f"\nmanifest: {manifest_path.relative_to(project_root)}")
        except ValueError:
            print(f"\nmanifest: {manifest_path}")
    else:
        print(f"\n(dry-run) manifest would be: {manifest_path}")

    label = "icon(s)" if asset_type.name == "icons" else "image(s)"
    remote_n = sum(1 for e in entries if e.get("delivery") == "remote")
    remote_suffix = f", {remote_n} remote (AsyncImage)" if remote_n else ""
    print(
        f"\nsummary [{mode_label} --type {args.type}]: {len(entries)} {label} total, "
        f"{sum(1 for e in entries if e['scope'] == 'chrome')} chrome, "
        f"{sum(1 for e in entries if e['scope'] == 'domain')} domain"
        f"{remote_suffix}"
    )
    return 1 if any_failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
