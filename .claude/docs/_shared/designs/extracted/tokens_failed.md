# Token Inventory: stitch_failed.html

## Tailwind Config Overrides

- **colors.background**: `#0A1209`
- **colors.error**: `#FFB4AB`
- **colors.error-container**: `#93000A`
- **colors.on-error**: `#690005`
- **colors.on-error-container**: `#FFDAD6`
- **colors.on-primary**: `#003919`
- **colors.on-primary-container**: `#C8FFD7`
- **colors.on-surface**: `#E2EEDF`
- **colors.on-surface-variant**: `#A5C0A0`
- **colors.outline**: `#5C7A5A`
- **colors.outline-variant**: `#2E4A2C`
- **colors.primary**: `#86E8AB`
- **colors.primary-container**: `#005227`
- **colors.secondary**: `#FFD700`
- **colors.surface**: `#141E12`
- **colors.surface-variant**: `#1E3020`
- **colors.tertiary**: `#4FC3F7`
- **borderRadius.2xl**: `20px`
- **borderRadius.DEFAULT**: `0.5rem`
- **borderRadius.full**: `9999px`
- **borderRadius.lg**: `1rem`
- **borderRadius.md**: `12px`
- **borderRadius.sm**: `6px`
- **borderRadius.xl**: `1.5rem`
- **fontFamily.body**: `Public Sans`
- **fontFamily.display**: `Outfit`
- **fontFamily.headline**: `Outfit`
- **fontFamily.label**: `Public Sans`

## Global Styles

Inline `<style>` rules that apply globally — these affect every matching element regardless of class list.

```css
body {
            font-family: 'Public Sans', sans-serif;
            -webkit-font-smoothing: antialiased;
            -moz-osx-font-smoothing: grayscale;
        }
        .material-symbols-outlined.icon-large {
            font-size: 96px;
            font-variation-settings: 'FILL' 1, 'wght' 400, 'GRAD' 0, 'opsz' 48;
            color: #FFB4AB;
        }
        .stadium-glow {
            background: radial-gradient(circle at 50% 50%, rgba(134, 232, 171, 0.05) 0%, rgba(10, 18, 9, 0) 70%);
        }
        @keyframes pulse-subtle {
            0% { opacity: 0.8; transform: scale(1); }
            50% { opacity: 1; transform: scale(1.05); }
            100% { opacity: 0.8; transform: scale(1); }
        }
        .icon-animate {
            animation: pulse-subtle 3s infinite ease-in-out;
        }
```

## Motion Inventory

Captured animation vocabulary. Bucket each token via the Web-Motion Policy in `.claude/skills/_shared/motion.md`: **KEEP** the 4 non-interaction families (Ambient bg, Loading/Attention loop, Entrance, Value-driven) + honor reduced-motion; **DROP** all touch press (`active:*`, ripple) and pointer/hover (`hover:*`, `group-hover:*`) feedback. Per-element `animate-*` / `transition-*` / `active:` / `hover:` tags are annotated inline in the Elements section below.

### @keyframes (<style> blocks)

- pulse-subtle

### Keyframe magnitudes

Animated value ranges (the delta each animation moves through). Pin these in the blueprint's `## Motion` **Magnitude** column — they are the only source for scale/translate/opacity/offset amounts (duration/easing come from the shorthand above; the implementer must not invent magnitudes).

- **pulse-subtle**: scale 1→1.05; opacity 0.8→1

### JS animation drivers

- timed step (Value/Entrance)

## Elements

Each class is followed by its deterministic token interpretation when one applies (e.g. `mt-4 → margin-top: 16dp`). Classes with no annotation are layout primitives, state variants, or unrecognised — interpret them yourself.

**Element formats:**
- **Visual elements** (any class converts to a visual token, or has inline style) get a full block with one line per class.
- **Layout-only elements** (only structural classes like `flex`, `items-center`, `justify-between`) get a single compact line — they still appear in order so structural mismatches (Row vs Column, arrangement, alignment) remain visible.
- **Classless text children** (e.g. `<span>Label</span>` inside a button) also appear as a one-liner with their text, so sibling DOM order inside a flex container is preserved — compare it against the Compose content lambda order.

- [1] `<html>` `dark`
**[2] `<body>`**

1. `bg-background` → background: background (#0A1209)
2. `text-on-surface` → color: on-surface (#E2EEDF)
3. `min-h-screen` → min-height: 100vh/vw
4. `flex`
5. `flex-col`
6. `items-center`
7. `justify-center`
8. `overflow-hidden`
9. `relative`

### <!-- Atmospheric Background Glow -->

**[3] `<div>`**

1. `fixed` (positioning: fixed — Compose: Box overlay or BottomBar slot)
2. `inset-0` → inset: 0dp
3. `stadium-glow`
4. `pointer-events-none`

### <!-- Main Content Column -->

**[4] `<main>`**

1. `relative`
2. `z-10` (z-index — Compose has no z-index; layering is order-based)
3. `w-full` → width: 100%
4. `max-w-xs`
5. `flex`
6. `flex-col`
7. `items-center`
8. `text-center`
9. `px-6` → horizontal padding: 24dp

### <!-- Main Icon -->

**[5] `<div>`**

1. `mb-8` → bottom margin: 32dp
2. `icon-animate`

- [6] `<span>` `material-symbols-outlined icon-large select-none` — "sports_soccer"
### <!-- Title -->

**[7] `<h1>` — "Oops! Connection lost"**

1. `font-headline`
2. `font-bold` → font-weight: 700 (Bold)
3. `text-headline-sm`
4. `text-on-surface` → color: on-surface (#E2EEDF)
5. `tracking-tight` → letter-spacing: -0.025em (× font-size for sp)
6. `mb-3` → bottom margin: 12dp

### <!-- Subtitle -->

**[8] `<p>` — "Check your connection and try again."**

1. `font-body`
2. `text-body-md`
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `mb-12` → bottom margin: 48dp
5. `leading-relaxed` → line-height: 1.625× font-size

### <!-- High-Contrast Retry Button -->

**[9] `<button>` — "RETRY"**

1. `w-full` → width: 100%
2. `h-14` → height: 56dp
3. `bg-primary` → background: primary (#86E8AB)
4. `text-on-primary` → color: on-primary (#003919)
5. `font-headline`
6. `font-bold` → font-weight: 700 (Bold)
7. `text-base` → font-size: 16sp
8. `rounded-xl` → corner-radius: 24dp
9. `transition-all` (motion: transition hint — Entrance/Value family)
10. `duration-200` (motion: transition hint — Entrance/Value family)
11. `active:scale-95` (motion: interaction — DROP — touch press feedback)
12. `hover:brightness-105` (motion: web-only — DROP — pointer/hover)
13. `shadow-lg` → shadow: ~8dp elevation
14. `shadow-primary/10` → shadow-color: primary (#86E8AB) @ 10%
15. `flex`
16. `items-center`
17. `justify-center`
18. `uppercase`
19. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)

---
**Total elements**: 9 | **Visual**: 7 | **Layout-only**: 2 | **Total classes**: 58 | **Auto-converted**: 24 (41%)

