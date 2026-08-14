# 🛺 Nikaalo — Traffic Jam Puzzle

**Jam se apni auto nikaalo!** Ek sliding-block puzzle game (Rush Hour genre) Indian traffic flavour ke saath. Kotlin + Jetpack Compose, koi game engine nahi — APK chhota, offline chalta hai, low-end phones pe smooth.

[![Build APK & Release](https://github.com/coding-king-sk/nikaalo-game/actions/workflows/release.yml/badge.svg)](https://github.com/coding-king-sk/nikaalo-game/actions/workflows/release.yml)

---

## 🎮 Gameplay

Ek grid ek jammed chowk hai. Tumhari **peeli auto** ko right edge ke exit tak pahunchana hai.

- Har vehicle sirf apni lambai ki direction me slide hota hai (horizontal wale left-right, vertical wale up-down)
- Koi vehicle ghum nahi sakta, aur kisi ke upar se cross nahi kar sakta
- 🐄 **Gaay hilti nahi** — permanent obstacle
- Kam moves = zyada stars (3 stars = optimal solution)

## ✨ Features

| Feature | Status |
|---|---|
| Drag-to-slide board with snap-to-grid | ✅ |
| 12 hand-crafted levels (3 chapters) | ✅ |
| BFS solver for real hints (optimal next move) | ✅ |
| Undo / Restart | ✅ |
| Star rating + best-moves tracking | ✅ |
| Progress persistence (SharedPreferences) | ✅ |
| Level generator script (Python) | ✅ |
| Auto-build + auto-release CI | ✅ |
| Daily challenge, ads, IAP | 🔜 |

## 📦 Download

Latest APK: **[Releases page](https://github.com/coding-king-sk/nikaalo-game/releases)**

Har push pe CI automatically APK build karke naya release publish karta hai.

## 🏗️ Build locally

```bash
git clone https://github.com/coding-king-sk/nikaalo-game.git
cd nikaalo-game
gradle wrapper --gradle-version 8.9   # first time only
./gradlew assembleDebug
```

APK yahan milega: `app/build/outputs/apk/debug/app-debug.apk`

Requirements: JDK 17, Android SDK 34.

## 🧩 Naye levels banao

`tools/level_generator.py` random boards generate karta hai, BFS solver se minimum moves nikalta hai, aur sirf woh levels rakhta hai jo difficulty range me fit hote hain.

```bash
python3 tools/level_generator.py --count 60 --min-moves 6 --max-moves 14 --grid 6 \
  --out app/src/main/assets/levels.json
```

Koi dependency nahi chahiye — pure standard library.

## 📁 Project structure

```
app/src/main/
  assets/levels.json                  # level data
  java/com/codingkingsk/nikaalo/
    MainActivity.kt                   # nav + app shell
    game/Models.kt                    # serializable level specs
    game/Board.kt                     # immutable board + move rules
    game/Solver.kt                    # BFS optimal solver (hints)
    game/LevelRepository.kt           # assets loader
    game/Progress.kt                  # best moves, stars, unlocks
    ui/Theme.kt                       # colors, vehicle palette
    ui/HomeScreen.kt
    ui/LevelSelectScreen.kt
    ui/GameScreen.kt                  # board rendering + drag
tools/level_generator.py              # generator + solver
docs/GAME_DESIGN.md                   # full design doc
.github/workflows/release.yml         # build + auto release
```

## 🗺️ Roadmap

- [ ] 350 generated levels across 6 chapters
- [ ] Daily challenge + streak + notification
- [ ] Share card on win (WhatsApp)
- [ ] Sound effects + haptics
- [ ] AdMob rewarded hints, ₹99 remove-ads IAP
- [ ] Release signing via CI secrets

## 📄 License

MIT — see [LICENSE](LICENSE)
