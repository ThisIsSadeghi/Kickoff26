# Stitch MCP Setup Guide

How to install and configure the Stitch MCP server for the `/ui-designer` skill when it is not yet available in Claude Code.

This guide is referenced from `phase-init.md` (Init-1) and `phase-0-preflight.md` (Step 0.2). Surface it to the user whenever `mcp__stitch__list_projects` is unavailable.

---

## Prerequisites

| Requirement | Why |
|---|---|
| **Node.js 18 or newer** | The `npx @_davideast/stitch-mcp` CLI requires it |
| **A Google account** | Stitch is a Google Labs product (https://stitch.withgoogle.com) |
| **Claude Code CLI** | The `claude mcp add` command registers the server |

Check Node version with `node -v`. If it is below 18, install a newer version (e.g. via `nvm install 20`) before continuing.

---

## Two Setup Paths

The user picks one. **API Key** is faster and works in any environment. **OAuth** is the full wizard — recommended only if the user already has a Google Cloud project they want to bill against.

Use `AskUserQuestion` to let the user choose:

| Option | When to recommend |
|---|---|
| **API Key (Recommended)** | First-time setup, no existing Google Cloud project, headless environments (WSL/SSH/Docker), users who just want it working |
| **OAuth via init wizard** | User already manages Google Cloud projects and wants Stitch tied to one of them |

---

## Path A — API Key (Recommended)

### Step 1: Get a Stitch API key

Direct the user:

1. Open <https://stitch.withgoogle.com/settings> in a browser.
2. Sign in with the Google account that has Stitch access.
3. Find the **API keys** section, click **Create API key**, copy the value, and store it somewhere safe.

> The key MUST come from Stitch's own settings page. Keys created at <https://aistudio.google.com/apikey> are AI Studio keys and are **rejected by the Stitch API for tool calls** ("API keys are not supported by this API"). Only Stitch-issued keys work.
>
> The API key is sent as the `X-Goog-Api-Key` header on every request. Treat it like a password.

### Step 2: Register the MCP server with Claude Code

Ask the user to run this in their terminal (replace `YOUR_API_KEY`):

```bash
claude mcp add stitch \
  --transport http https://stitch.googleapis.com/mcp \
  --header "X-Goog-Api-Key: YOUR_API_KEY" \
  -s user
```

Flags:
- `--transport http` — direct HTTP connection (no proxy, no Node process)
- `-s user` — save to `$HOME/.claude.json` so all projects can use it. Use `-s project` instead to save to `./.mcp.json` (per-repo)

### Step 3: Restart Claude Code and re-invoke

The user must fully restart Claude Code (quit and reopen) so the new MCP server loads. Then re-invoke `/ui-designer` (with or without a feature name, depending on where they were in the flow).

---

## Path B — OAuth via init wizard

The wizard handles gcloud SDK install, Google Cloud project selection, API enablement, and config generation. It opens a browser for OAuth.

Ask the user to run:

```bash
npx @_davideast/stitch-mcp init
```

The wizard walks through 9 steps: client selection (pick **Claude Code**), authentication mode (pick **OAuth**), gcloud install, Google sign-in, project selection, IAM/API enablement, transport choice, config generation, connection test.

After it finishes the wizard writes the config to `$HOME/.claude.json` (or `./.mcp.json` if the user picked project scope). The user must restart Claude Code and re-invoke `/ui-designer`.

---

## Headless / WSL / SSH / Docker

OAuth's browser flow does not work in these environments. Use Path A (API Key) instead — it works everywhere.

If the user insists on OAuth in a headless environment, they can set `STITCH_API_KEY` and run the init wizard, which downgrades to a non-browser flow:

```bash
export STITCH_API_KEY="your-api-key"
npx @_davideast/stitch-mcp init
```

---

## Verifying the Setup

After the user restarts Claude Code and re-invokes `/ui-designer`, the skill's preflight call to `mcp__stitch__list_projects` should succeed. If it still fails:

| Symptom | Likely cause | Fix |
|---|---|---|
| Tool `mcp__stitch__list_projects` not found | Claude Code was not restarted, or the server entry is in a scope Claude Code does not load | Quit Claude Code fully and reopen. Verify with `claude mcp list` |
| 401 / 403 from Stitch with "API keys are not supported by this API" | Key was created at AI Studio (`aistudio.google.com/apikey`) — Stitch rejects those | Create a Stitch-issued key at <https://stitch.withgoogle.com/settings> and re-run `claude mcp add stitch …` |
| 401 / 403 from Stitch (other) | Invalid or revoked API key | Generate a fresh key at <https://stitch.withgoogle.com/settings> and re-run `claude mcp add stitch …` (it will overwrite the existing entry) |
| `npx` command not found | Node.js not installed or PATH issue | `node -v` to check; install Node 18+ |
| Connection timeout on every call | Corporate firewall blocking `stitch.googleapis.com` | Switch to the proxy variant: `claude mcp add stitch -e STITCH_API_KEY=YOUR_API_KEY -- npx @_davideast/stitch-mcp proxy` |

---

## Listing and Removing the Stitch MCP Entry

For troubleshooting, the user can run:

```bash
claude mcp list                # show all registered MCP servers
claude mcp remove stitch       # remove the stitch entry (then re-add with corrected flags)
```

---

## Quick-Reference: What to Say to the User

When preflight detects that Stitch MCP is missing, present this short version (the long form lives in this file):

```
Stitch MCP is not configured. The /ui-designer skill needs it for all Stitch operations.

Fastest setup (≈2 minutes):
  1. Get a Stitch API key at https://stitch.withgoogle.com/settings (NOT aistudio.google.com — AI Studio keys are rejected by the Stitch API)
  2. Run in your terminal:
       claude mcp add stitch \
         --transport http https://stitch.googleapis.com/mcp \
         --header "X-Goog-Api-Key: YOUR_API_KEY" \
         -s user
  3. Fully restart Claude Code (quit and reopen)
  4. Re-invoke /ui-designer

Full setup guide (OAuth, troubleshooting, alternatives):
  .claude/skills/ui-designer/references/stitch-setup.md

Prerequisite: Node.js 18+ (check with `node -v`)
```
