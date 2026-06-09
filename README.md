<div align="center">

# ⚽ Kickoff26

Follow group standings, matches, and a countdown to kickoff —
one shared Kotlin Multiplatform codebase for **Android** and **iOS**.

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-4285F4?logo=jetpackcompose&logoColor=white)
![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS-2C9F45)
[![Built with KMPilot](https://img.shields.io/badge/Built%20with-KMPilot-FFB300?logo=rocket&logoColor=white)](https://github.com/ThisIsSadeghi/KMPilot)
![Status](https://img.shields.io/badge/Status-Under%20Development-orange)

</div>

---

<table align="center">
  <tr>
    <td align="center"><img src="docs/screenshots/home_android.png" alt="Kickoff26 — Home (Android)" width="280" /></td>
    <td align="center"><img src="docs/screenshots/home_ios.png" alt="Kickoff26 — Home (iOS)" width="280" /></td>
  </tr>
  <tr>
    <td align="center"><sub>🤖 Android</sub></td>
    <td align="center"><sub>🍏 iOS</sub></td>
  </tr>
</table>

## 📖 About

Kickoff26 is a 2026 World Cup companion app: browse group standings, track
matches with live scores, and watch the countdown to kickoff — all from one
Kotlin Multiplatform codebase shared across Android and iOS.

> 🚧 **Under active development.** Only the **Home** feature (countdown,
> upcoming matches, group standings) is implemented so far. The **Matches** and
> **Profile** tabs are placeholders — more features are on the way.

## ✨ Features

- 🟢 **Group standings** — tabbed group tables with team standings
- ⚽ **Matches** — match cards with live badges and scorelines
- ⏱️ **Countdown** to kickoff
- 🎨 **World Cup green/gold theme** via a shared design system

## 🚀 Powered by KMPilot

Built on **[KMPilot](https://github.com/ThisIsSadeghi/KMPilot)** — a Compose
Multiplatform + Clean Architecture starter that scaffolds features, enforces
architecture, and ships an AI-assisted ([Claude Code](https://claude.com/claude-code))
workflow. Every module and convention here comes from KMPilot; see
[`CLAUDE.md`](CLAUDE.md) for the conventions.

## 🧰 Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin 2.4.0 |
| UI | Compose Multiplatform 1.11.1 |
| Targets | Android, iOS (arm64 + simulator) |
| Networking | Ktor 3.5.0 |
| DI | Koin 4.2.1 |
| Navigation | Compose Navigation 2.9.2 |
| Async | Coroutines + Flow |

## 🙏 Acknowledgements

**World Cup 2026 data** is powered by the open
[worldcup2026 API](https://github.com/rezarahiminia/worldcup2026) by
**[Reza Rahiminia](https://github.com/rezarahiminia)** — thank you for building
and sharing it. 🏆

---

<div align="center">

⚽ **Kickoff26**

</div>
