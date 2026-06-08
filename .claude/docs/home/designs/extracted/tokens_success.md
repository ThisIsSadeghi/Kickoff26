# Token Inventory: stitch_success.html

## Tailwind Config Overrides

- **colors.background**: `#0A1209`
- **colors.error**: `#FFB4AB`
- **colors.on-error**: `#690005`
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
- **borderRadius.lg**: `1rem`
- **borderRadius.xl**: `1.5rem`
- **fontFamily.body**: `Outfit`
- **fontFamily.display**: `Outfit`
- **fontFamily.headline**: `Outfit`
- **fontFamily.label**: `Outfit`

## Global Styles

Inline `<style>` rules that apply globally — these affect every matching element regardless of class list.

```css
body {
            font-family: 'Outfit', sans-serif;
            background-color: #0A1209;
            color: #E2EEDF;
            -webkit-tap-highlight-color: transparent;
        }
        .hide-scrollbar::-webkit-scrollbar {
            display: none;
        }
        .hero-gradient {
            background: linear-gradient(180deg, rgba(20, 30, 18, 0) 0%, rgba(10, 18, 9, 1) 100%), linear-gradient(45deg, #003919 0%, #141E12 100%);
        }
        .glass-card {
            background: rgba(20, 30, 18, 0.8);
            backdrop-filter: blur(12px);
        }
        body {
            min-height: max(884px, 100dvh);
        }
```

## Motion Inventory

Captured animation vocabulary. Bucket each token via the Web-Motion Policy in `.claude/skills/_shared/motion.md`: **KEEP** the 4 non-interaction families (Ambient bg, Loading/Attention loop, Entrance, Value-driven) + honor reduced-motion; **DROP** all touch press (`active:*`, ripple) and pointer/hover (`hover:*`, `group-hover:*`) feedback. Per-element `animate-*` / `transition-*` / `active:` / `hover:` tags are annotated inline in the Elements section below.

### JS animation drivers

- timed loop (Value/Ambient)
- count-up number (Value-driven)

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
3. `antialiased`
4. `overflow-x-hidden`
5. `pb-24` → bottom padding: 96dp

### <!-- TopAppBar -->

**[3] `<header>`**

1. `fixed` (positioning: fixed — Compose: Box overlay or BottomBar slot)
2. `top-0` → top: 0dp
3. `left-0` → left: 0dp
4. `right-0` → right: 0dp
5. `z-50` (z-index — Compose has no z-index; layering is order-based)
6. `transition-colors` (motion: transition hint — Entrance/Value family)
7. `duration-300` (motion: transition hint — Entrance/Value family)
8. `px-4` → horizontal padding: 16dp
9. `h-16` → height: 64dp
10. `flex`
11. `items-center`
12. `bg-background/40` → background: background (#0A1209) @ 40%
13. `backdrop-blur-sm`

**[4] `<div>`**

1. `flex`
2. `items-center`
3. `gap-3` → gap: 12dp

**[5] `<h1>` — "KICKOFF 26"**

1. `font-display`
2. `font-black` → font-weight: 900 (Black)
3. `tracking-tighter` → letter-spacing: -0.05em (× font-size for sp)
4. `text-xl` → font-size: 20sp
5. `text-primary` → color: primary (#86E8AB)
6. `uppercase`

**[6] `<main>`**

1. `pt-4` → top padding: 16dp
2. `px-4` → horizontal padding: 16dp
3. `space-y-6` → children spaced 24dp vertically

### <!-- Hero Section: Countdown -->

**[7] `<section>`**

1. `relative`
2. `h-[200px]` → height: 200px
3. `w-full` → width: 100%
4. `rounded-2xl` → corner-radius: 20dp
5. `overflow-hidden`
6. `flex`
7. `flex-col`
8. `justify-end`
9. `p-5` → padding: 20dp
10. `hero-gradient`
11. `border` → border-width: 1dp
12. `border-outline-variant/30` → border-color: outline-variant (#2E4A2C) @ 30%
13. `mt-12` → top margin: 48dp

**[8] `<div>`**

1. `absolute` (positioning: absolute — Compose: Box overlay or BottomBar slot)
2. `inset-0` → inset: 0dp
3. `z-0` (z-index — Compose has no z-index; layering is order-based)
4. `opacity-40` → opacity: 0.4

**[9] `<img>`**

1. `w-full` → width: 100%
2. `h-full` → height: 100%
3. `object-cover`

**[10] `<div>`**

1. `relative`
2. `z-10` (z-index — Compose has no z-index; layering is order-based)
3. `space-y-3` → children spaced 12dp vertically

**[11] `<p>` — "TOURNAMENT STARTS IN"**

1. `text-[10px]` → font-size: 10sp
2. `font-bold` → font-weight: 700 (Bold)
3. `tracking-[0.2em]` → letter-spacing: 0.2em
4. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
5. `uppercase`
6. `text-center`

**[12] `<div>`**

1. `flex`
2. `items-center`
3. `justify-center`
4. `gap-6` → gap: 24dp
5. `text-secondary` → color: secondary (#FFD700)
6. `tabular-nums`

- [13] `<div>` `flex flex-col items-center`
**[14] `<span>` — "4D"**

1. `text-4xl` → font-size: 36sp
2. `font-black` → font-weight: 900 (Black)

- [15] `<div>` `flex flex-col items-center`
**[16] `<span>` — "11H"**

1. `text-4xl` → font-size: 36sp
2. `font-black` → font-weight: 900 (Black)

- [17] `<div>` `flex flex-col items-center`
**[18] `<span>` — "23M"**

1. `text-4xl` → font-size: 36sp
2. `font-black` → font-weight: 900 (Black)

- [19] `<div>` `flex flex-col items-center`
**[20] `<span>` — "52S"**

1. `text-4xl` → font-size: 36sp
2. `font-black` → font-weight: 900 (Black)

**[21] `<div>`**

1. `flex`
2. `items-center`
3. `justify-center`
4. `gap-2` → gap: 8dp
5. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
6. `text-sm` → font-size: 14sp

**[22] `<span>` — "calendar_today"**

1. `material-symbols-outlined`
2. `text-xs` → font-size: 12sp

- [23] `<span>` — "June 11, 2026 · Mexico City"
### <!-- Upcoming Matches -->

**[24] `<section>`**

1. `space-y-3` → children spaced 12dp vertically

- [25] `<div>` `flex justify-between items-end`
**[26] `<h3>` — "Upcoming Matches"**

1. `font-bold` → font-weight: 700 (Bold)
2. `text-lg` → font-size: 18sp
3. `tracking-tight` → letter-spacing: -0.025em (× font-size for sp)

**[27] `<button>` — "View All"**

1. `text-primary` → color: primary (#86E8AB)
2. `text-xs` → font-size: 12sp
3. `font-semibold` → font-weight: 600 (SemiBold)
4. `uppercase`
5. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)

**[28] `<div>`**

1. `flex`
2. `overflow-x-auto`
3. `hide-scrollbar`
4. `gap-4` → gap: 16dp
5. `-mx-4` → horizontal margin: -16dp
6. `px-4` → horizontal padding: 16dp
7. `pb-2` → bottom padding: 8dp

### <!-- Card 1: LIVE -->

**[29] `<div>`**

1. `min-w-[280px]` → min-width: 280px
2. `bg-surface` → background: surface (#141E12)
3. `rounded-xl` → corner-radius: 24dp
4. `p-4` → padding: 16dp
5. `border` → border-width: 1dp
6. `border-outline-variant/50` → border-color: outline-variant (#2E4A2C) @ 50%
7. `relative`
8. `overflow-hidden`
9. `group`
10. `active:scale-[0.98]` (motion: interaction — DROP — touch press feedback)
11. `transition-transform` (motion: transition hint — Entrance/Value family)

**[30] `<div>`**

1. `flex`
2. `justify-between`
3. `items-center`
4. `mb-4` → bottom margin: 16dp

**[31] `<span>` — "GROUP A"**

1. `px-2` → horizontal padding: 8dp
2. `py-0.5` → vertical padding: 2dp
3. `bg-surface-variant` → background: surface-variant (#1E3020)
4. `text-primary` → color: primary (#86E8AB)
5. `text-[10px]` → font-size: 10sp
6. `font-bold` → font-weight: 700 (Bold)
7. `rounded-sm` → corner-radius: 2dp
8. `uppercase`
9. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)

**[32] `<div>`**

1. `flex`
2. `items-center`
3. `gap-1.5` → gap: 6dp
4. `px-2` → horizontal padding: 8dp
5. `py-0.5` → vertical padding: 2dp
6. `bg-tertiary/10` → background: tertiary (#4FC3F7) @ 10%
7. `rounded-full` → corner-radius: CircleShape

**[33] `<span>`**

1. `w-1.5` → width: 6dp
2. `h-1.5` → height: 6dp
3. `bg-tertiary` → background: tertiary (#4FC3F7)
4. `rounded-full` → corner-radius: CircleShape
5. `animate-pulse` (motion: looping 'pulse' — Loading/Attention loop)

**[34] `<span>` — "● LIVE"**

1. `text-tertiary` → color: tertiary (#4FC3F7)
2. `text-[10px]` → font-size: 10sp
3. `font-black` → font-weight: 900 (Black)
4. `uppercase`
5. `tracking-widest` → letter-spacing: 0.1em (× font-size for sp)

- [35] `<div>` `flex justify-between items-center`
**[36] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-2` → gap: 8dp
5. `flex-1`

**[37] `<div>` — "🇲🇽"**

1. `w-12` → width: 48dp
2. `h-12` → height: 48dp
3. `rounded-full` → corner-radius: CircleShape
4. `bg-surface-variant` → background: surface-variant (#1E3020)
5. `flex`
6. `items-center`
7. `justify-center`
8. `text-2xl` → font-size: 24sp

**[38] `<span>` — "MEX"**

1. `text-xs` → font-size: 12sp
2. `font-bold` → font-weight: 700 (Bold)
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `uppercase`

**[39] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-1` → gap: 4dp
5. `flex-1`

**[40] `<span>` — "2 - 1"**

1. `text-3xl` → font-size: 30sp
2. `font-black` → font-weight: 900 (Black)
3. `text-secondary` → color: secondary (#FFD700)

**[41] `<span>` — "74'"**

1. `text-[10px]` → font-size: 10sp
2. `font-medium` → font-weight: 500 (Medium)
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)

**[42] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-2` → gap: 8dp
5. `flex-1`

**[43] `<div>` — "🇳🇱"**

1. `w-12` → width: 48dp
2. `h-12` → height: 48dp
3. `rounded-full` → corner-radius: CircleShape
4. `bg-surface-variant` → background: surface-variant (#1E3020)
5. `flex`
6. `items-center`
7. `justify-center`
8. `text-2xl` → font-size: 24sp

**[44] `<span>` — "NED"**

1. `text-xs` → font-size: 12sp
2. `font-bold` → font-weight: 700 (Bold)
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `uppercase`

### <!-- Card 2: Upcoming -->

**[45] `<div>`**

1. `min-w-[280px]` → min-width: 280px
2. `bg-surface` → background: surface (#141E12)
3. `rounded-xl` → corner-radius: 24dp
4. `p-4` → padding: 16dp
5. `border` → border-width: 1dp
6. `border-outline-variant/50` → border-color: outline-variant (#2E4A2C) @ 50%
7. `relative`
8. `overflow-hidden`
9. `group`
10. `opacity-80` → opacity: 0.8

**[46] `<div>`**

1. `flex`
2. `justify-between`
3. `items-center`
4. `mb-4` → bottom margin: 16dp

**[47] `<span>` — "GROUP B"**

1. `px-2` → horizontal padding: 8dp
2. `py-0.5` → vertical padding: 2dp
3. `bg-surface-variant` → background: surface-variant (#1E3020)
4. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
5. `text-[10px]` → font-size: 10sp
6. `font-bold` → font-weight: 700 (Bold)
7. `rounded-sm` → corner-radius: 2dp
8. `uppercase`
9. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)

**[48] `<span>` — "20:00 KICKOFF"**

1. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
2. `text-[10px]` → font-size: 10sp
3. `font-bold` → font-weight: 700 (Bold)
4. `uppercase`
5. `tracking-widest` → letter-spacing: 0.1em (× font-size for sp)

- [49] `<div>` `flex justify-between items-center`
**[50] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-2` → gap: 8dp
5. `flex-1`

**[51] `<div>` — "🇺🇸"**

1. `w-12` → width: 48dp
2. `h-12` → height: 48dp
3. `rounded-full` → corner-radius: CircleShape
4. `bg-surface-variant` → background: surface-variant (#1E3020)
5. `flex`
6. `items-center`
7. `justify-center`
8. `text-2xl` → font-size: 24sp

**[52] `<span>` — "USA"**

1. `text-xs` → font-size: 12sp
2. `font-bold` → font-weight: 700 (Bold)
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `uppercase`

**[53] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-1` → gap: 4dp
5. `flex-1`
6. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)

**[54] `<span>` — "VS"**

1. `text-xl` → font-size: 20sp
2. `font-black` → font-weight: 900 (Black)

**[55] `<div>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `gap-2` → gap: 8dp
5. `flex-1`

**[56] `<div>` — "🏴󠁧󠁢󠁥󠁮󠁧󠁿"**

1. `w-12` → width: 48dp
2. `h-12` → height: 48dp
3. `rounded-full` → corner-radius: CircleShape
4. `bg-surface-variant` → background: surface-variant (#1E3020)
5. `flex`
6. `items-center`
7. `justify-center`
8. `text-2xl` → font-size: 24sp

**[57] `<span>` — "ENG"**

1. `text-xs` → font-size: 12sp
2. `font-bold` → font-weight: 700 (Bold)
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `uppercase`

### <!-- Group Standings Section -->

**[58] `<section>`**

1. `space-y-4` → children spaced 16dp vertically

**[59] `<h3>` — "Group Standings"**

1. `font-bold` → font-weight: 700 (Bold)
2. `text-lg` → font-size: 18sp
3. `tracking-tight` → letter-spacing: -0.025em (× font-size for sp)

### <!-- Horizontal Tabs -->

**[60] `<div>`**

1. `flex`
2. `overflow-x-auto`
3. `hide-scrollbar`
4. `gap-2` → gap: 8dp
5. `-mx-4` → horizontal margin: -16dp
6. `px-4` → horizontal padding: 16dp
7. `pb-1` → bottom padding: 4dp

**[61] `<button>` — "Group A"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `bg-primary/10` → background: primary (#86E8AB) @ 10%
4. `text-primary` → color: primary (#86E8AB)
5. `border-b-2` → border-b width: 2dp
6. `border-primary` → border-color: primary (#86E8AB)
7. `text-xs` → font-size: 12sp
8. `font-bold` → font-weight: 700 (Bold)
9. `whitespace-nowrap`

**[62] `<button>` — "Group B"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[63] `<button>` — "Group C"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[64] `<button>` — "Group D"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[65] `<button>` — "Group E"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[66] `<button>` — "Group F"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[67] `<button>` — "Group G"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[68] `<button>` — "Group H"**

1. `px-4` → horizontal padding: 16dp
2. `py-2` → vertical padding: 8dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
4. `text-xs` → font-size: 12sp
5. `font-bold` → font-weight: 700 (Bold)
6. `whitespace-nowrap`

**[69] `<div>`**

1. `bg-surface` → background: surface (#141E12)
2. `rounded-xl` → corner-radius: 24dp
3. `overflow-hidden`
4. `border` → border-width: 1dp
5. `border-outline-variant/50` → border-color: outline-variant (#2E4A2C) @ 50%

**[70] `<table>`**

1. `w-full` → width: 100%
2. `text-left`
3. `text-xs` → font-size: 12sp

**[71] `<thead>`**

1. `bg-surface-variant/50` → background: surface-variant (#1E3020) @ 50%
2. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
3. `font-bold` → font-weight: 700 (Bold)
4. `uppercase`
5. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)

**[73] `<th>` — "Pos"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp

**[74] `<th>` — "Team"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp

**[75] `<th>` — "P"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[76] `<th>` — "W"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[77] `<th>` — "D"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[78] `<th>` — "L"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[79] `<th>` — "Pts"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-right`

**[80] `<tbody>`**

1. `divide-y`
2. `divide-outline-variant/20` → divide-color: outline-variant (#2E4A2C) @ 20%

**[81] `<tr>`**

1. `border-l-4` → border-l width: 4dp
2. `border-primary` → border-color: primary (#86E8AB)
3. `bg-primary/5` → background: primary (#86E8AB) @ 5%

**[82] `<td>` — "1"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `font-bold` → font-weight: 700 (Bold)

**[83] `<td>`**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `font-bold` → font-weight: 700 (Bold)
4. `flex`
5. `items-center`
6. `gap-2` → gap: 8dp

**[84] `<span>` — "🇲🇽 MEX"**

1. `text-base` → font-size: 16sp

**[85] `<td>` — "2"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[86] `<td>` — "2"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[87] `<td>` — "0"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[88] `<td>` — "0"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[89] `<td>` — "6"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-right`
4. `font-black` → font-weight: 900 (Black)
5. `text-primary` → color: primary (#86E8AB)

**[91] `<td>` — "2"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)

**[92] `<td>`**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `flex`
4. `items-center`
5. `gap-2` → gap: 8dp

**[93] `<span>` — "🇳🇱 NED"**

1. `text-base` → font-size: 16sp

**[94] `<td>` — "2"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[95] `<td>` — "1"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[96] `<td>` — "0"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[97] `<td>` — "1"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[98] `<td>` — "3"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-right`
4. `font-bold` → font-weight: 700 (Bold)

**[100] `<td>` — "3"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)

**[101] `<td>`**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `flex`
4. `items-center`
5. `gap-2` → gap: 8dp

**[102] `<span>` — "🇸🇦 KSA"**

1. `text-base` → font-size: 16sp

**[103] `<td>` — "1"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[104] `<td>` — "0"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[105] `<td>` — "0"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[106] `<td>` — "1"**

1. `px-2` → horizontal padding: 8dp
2. `py-3` → vertical padding: 12dp
3. `text-center`

**[107] `<td>` — "0"**

1. `px-4` → horizontal padding: 16dp
2. `py-3` → vertical padding: 12dp
3. `text-right`
4. `font-bold` → font-weight: 700 (Bold)

### <!-- BottomNavBar -->

**[108] `<nav>`**

1. `fixed` (positioning: fixed — Compose: Box overlay or BottomBar slot)
2. `bottom-0` → bottom: 0dp
3. `w-full` → width: 100%
4. `z-50` (z-index — Compose has no z-index; layering is order-based)
5. `flex`
6. `justify-around`
7. `items-center`
8. `h-20` → height: 80dp
9. `pb-safe`
10. `bg-surface/90` → background: surface (#141E12) @ 90%
11. `backdrop-blur-md`
12. `rounded-t-xl` → corner-radius (t): 24dp
13. `shadow-lg` → shadow: ~8dp elevation
14. `border-t` → border-t width: 1dp
15. `border-outline-variant/30` → border-color: outline-variant (#2E4A2C) @ 30%

### <!-- Home (Active) -->

**[109] `<a>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `justify-center`
5. `text-primary` → color: primary (#86E8AB)
6. `font-bold` → font-weight: 700 (Bold)
7. `scale-110`
8. `active:scale-100` (motion: interaction — DROP — touch press feedback)
9. `transition-transform` (motion: transition hint — Entrance/Value family)
10. `hover:text-primary-container` (motion: web-only — DROP — pointer/hover)
11. `transition-colors` (motion: transition hint — Entrance/Value family)

**[110] `<span>` — "home"**

1. `material-symbols-outlined`
- _inline style_: `font-variation-settings: 'FILL' 1;`

**[111] `<span>` — "Home"**

1. `font-label`
2. `text-[10px]` → font-size: 10sp
3. `uppercase`
4. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)
5. `mt-1` → top margin: 4dp

### <!-- Matches -->

**[112] `<a>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `justify-center`
5. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
6. `scale-110`
7. `active:scale-100` (motion: interaction — DROP — touch press feedback)
8. `transition-transform` (motion: transition hint — Entrance/Value family)
9. `hover:text-primary-container` (motion: web-only — DROP — pointer/hover)
10. `transition-colors` (motion: transition hint — Entrance/Value family)

- [113] `<span>` `material-symbols-outlined` — "sports_soccer"
**[114] `<span>` — "Matches"**

1. `font-label`
2. `text-[10px]` → font-size: 10sp
3. `uppercase`
4. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)
5. `mt-1` → top margin: 4dp

### <!-- Profile -->

**[115] `<a>`**

1. `flex`
2. `flex-col`
3. `items-center`
4. `justify-center`
5. `text-on-surface-variant` → color: on-surface-variant (#A5C0A0)
6. `scale-110`
7. `active:scale-100` (motion: interaction — DROP — touch press feedback)
8. `transition-transform` (motion: transition hint — Entrance/Value family)
9. `hover:text-primary-container` (motion: web-only — DROP — pointer/hover)
10. `transition-colors` (motion: transition hint — Entrance/Value family)

- [116] `<span>` `material-symbols-outlined` — "person"
**[117] `<span>` — "Profile"**

1. `font-label`
2. `text-[10px]` → font-size: 10sp
3. `uppercase`
4. `tracking-wider` → letter-spacing: 0.05em (× font-size for sp)
5. `mt-1` → top margin: 4dp

---
**Total elements**: 117 | **Visual**: 103 | **Layout-only**: 11 | **Total classes**: 515 | **Auto-converted**: 310 (60%)

