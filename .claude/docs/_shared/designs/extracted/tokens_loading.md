# Token Inventory: stitch_loading.html

## Tailwind Config Overrides

_(none found)_

## Global Styles

Inline `<style>` rules that apply globally — these affect every matching element regardless of class list.

```css
body {
            background-color: #0d150c;
            margin: 0;
            overflow: hidden;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            width: 100vw;
        }

        .spinner-container {
            position: relative;
            width: 48px;
            height: 48px;
        }

        .spinner {
            width: 100%;
            height: 100%;
            border: 4px solid rgba(134, 232, 171, 0.1);
            border-top: 4px solid #86e8ab;
            border-radius: 50%;
            animation: spin 1s cubic-bezier(0.4, 0, 0.2, 1) infinite;
            filter: drop-shadow(0 0 8px rgba(134, 232, 171, 0.4));
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Material Ripple effect for atmospheric feel */
        .glow-pulse {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 64px;
            height: 64px;
            background: radial-gradient(circle, rgba(134, 232, 171, 0.15) 0%, rgba(13, 21, 12, 0) 70%);
            border-radius: 50%;
            animation: pulse 2s ease-in-out infinite;
            z-index: -1;
        }

        @keyframes pulse {
            0%, 100% { opacity: 0.5; scale: 1; }
            50% { opacity: 1; scale: 1.5; }
        }
body {
      min-height: max(884px, 100dvh);
    }
```

## Motion Inventory

Captured animation vocabulary. Bucket each token via the Web-Motion Policy in `.claude/skills/_shared/motion.md`: **KEEP** the 4 non-interaction families (Ambient bg, Loading/Attention loop, Entrance, Value-driven) + honor reduced-motion; **DROP** all touch press (`active:*`, ripple) and pointer/hover (`hover:*`, `group-hover:*`) feedback. Per-element `animate-*` / `transition-*` / `active:` / `hover:` tags are annotated inline in the Elements section below.

### @keyframes (<style> blocks)

- spin, pulse

### Keyframe magnitudes

Animated value ranges (the delta each animation moves through). Pin these in the blueprint's `## Motion` **Magnitude** column — they are the only source for scale/translate/opacity/offset amounts (duration/easing come from the shorthand above; the implementer must not invent magnitudes).

- **spin**: rotate 0deg→360deg
- **pulse**: opacity 0.5→1

### JS animation drivers

- timed step (Value/Entrance)

## Elements

Each class is followed by its deterministic token interpretation when one applies (e.g. `mt-4 → margin-top: 16dp`). Classes with no annotation are layout primitives, state variants, or unrecognised — interpret them yourself.

**Element formats:**
- **Visual elements** (any class converts to a visual token, or has inline style) get a full block with one line per class.
- **Layout-only elements** (only structural classes like `flex`, `items-center`, `justify-between`) get a single compact line — they still appear in order so structural mismatches (Row vs Column, arrangement, alignment) remain visible.
- **Classless text children** (e.g. `<span>Label</span>` inside a button) also appear as a one-liner with their text, so sibling DOM order inside a flex container is preserved — compare it against the Compose content lambda order.

- [1] `<html>` `dark`
- [2] `<body>` `bg-background`
### <!-- Generic Mobile Loading State 
        - Hierarchy: Suppressing all navigation elements (TopAppBar, BottomNavBar) per user instructions.
        - Intent: Transactional/Focused waiting state. -->

**[3] `<main>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `justify-center`
5. `w-full` → width: 100%
6. `h-full` → height: 100%

- [4] `<div>` `spinner-container`
- [5] `<div>` `glow-pulse`
- [6] `<div>` `spinner`
---
**Total elements**: 6 | **Visual**: 1 | **Layout-only**: 5 | **Total classes**: 11 | **Auto-converted**: 2 (18%)

