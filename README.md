# Nikaalo — Traffic Jam Puzzle

> Jam se apni auto nikaalo.

A sliding-block traffic puzzle for Android. The grid is a jammed chowk; slide the
other vehicles out of the way and drive the yellow auto out through the gate on
the right edge.

Built with **Kotlin + Jetpack Compose** — no game engine, no image assets, fully
offline.

## Download

Every push to `main` builds an APK and publishes it automatically:
**[Releases](https://github.com/coding-king-sk/nikaalo-game/releases)**

## Gameplay

One rule, no tutorial needed:

- Each vehicle slides only along its own length
- Nothing turns, nothing passes through anything
- Gaay (cow) never moves at all
- Auto reaches the right edge → level clear

Fewer moves means more stars. Three stars requires the **optimal** solution.

| Piece | Cells | Role |
|---|---|---|
| Auto (yellow) | 2 | The player, one per board |
| Bike | 2 | Small blocker |
| Car | 2 | Standard blocker |
| Thela | 2 | Standard blocker |
| Bus | 3 | Large blocker |
| Truck | 3 | Large blocker |
| Gaay | 1 | Immovable obstacle |

## Content

**120 levels across 6 chapters**, every one verified by BFS — the `minMoves`
value in the asset is the mathematically optimal solution length, not a guess.

| Chapter | Theme | Levels | Grid | Optimal moves |
|---|---|---|---|---|
| 1 | Gali Mohalla | 15 | 6x6 | 3–6 |
| 2 | Sabzi Mandi | 25 | 6x6, 7x7 | 7–11 |
| 3 | Bus Stand | 25 | 6x6, 7x7 | 12–13 |
| 4 | Highway Toll | 20 | 6x6, 7x7 | 14–15 |
| 5 | Gaay Chowk | 20 | 6x6, 7x7 | 16–18 |
| 6 | Rush Hour Mumbai | 15 | 6x6, 7x7 | 19–23 |

A level is unlocked once the previous one is solved.

## Features

- Drag any vehicle; it snaps to the grid with a spring animation and a haptic tick
- Vehicles are drawn with Canvas (body, cabin, windshield, wheels, headlights),
  so they scale to any grid size without assets
- **Hints run the real solver.** BFS finds the optimal remaining path and tells
  you the exact next move — it is never a canned tip
- Undo, Restart, per-level best-move tracking, star ratings
- Animated win screen with move count against the optimum

## Project layout

```
app/src/main/
  assets/levels.txt        compact level data, 120 levels under 10 KB
  java/.../game/
    Board.kt               immutable board, slide rules, legal move ranges
    Solver.kt              BFS solver - powers hints and level verification
    LevelRepository.kt     levels.txt parser
    Progress.kt            best moves, stars, unlocks (SharedPreferences)
    Models.kt, Chapters.kt
  java/.../ui/
    GameScreen.kt          board, drag gestures, animations, controls
    VehicleArt.kt          Canvas vehicle art
    WinOverlay.kt          animated level-complete screen
    HomeScreen.kt, LevelSelectScreen.kt, Theme.kt, Widgets.kt
tools/level_generator.py   level miner and chapter builder
docs/GAME_DESIGN.md        design doc
```

### Level format

One level per line in `assets/levels.txt`:

```
grid:minMoves:chapter:piece,piece,piece
```

Each piece is exactly five characters — `[type][row][col][len][dir]`, where type
is one of `a` auto, `k` bike, `c` car, `t` thela, `b` bus, `T` truck, `w` cow:

```
6:23:6:a322h,t432v,t412h,k052v,t502h,T233h,t122v,k342v,c132h,c542h,t002h,k352v,b203v
```

## Generating more levels

Random placement almost never produces a hard board, so the generator hill
climbs: it mutates one blocker at a time and keeps boards whose BFS solution
length does not get shorter.

```bash
# mine a pool of verified boards (longer run = harder boards)
python3 tools/level_generator.py mine --seconds 900 --out pool.json

# lay the pool out into chapters and write the app asset
python3 tools/level_generator.py curate --pool pool.json \
    --out app/src/main/assets/levels.txt
```

Edit `CHAPTERS` in the script to change chapter sizes or difficulty bands.

## Building

```bash
gradle wrapper --gradle-version 8.9   # only needed on a fresh clone
./gradlew assembleDebug
```

The wrapper JAR is not committed, so CI provisions Gradle 8.9 directly. Opening
the project in Android Studio also generates the wrapper for you.

Requires JDK 17, compileSdk 34, minSdk 24.

## Releases and signing

`.github/workflows/release.yml` runs on every push to `main`, on `v*` tags, and
on manual dispatch. It builds the APK, uploads it as a build artifact, and
publishes a GitHub Release tagged `v1.0.<run number>` (or the pushed tag).

Those APKs are **debug-signed** — installable, but not Play Store ready. For a
release build, add a keystore as repository secrets, wire a `signingConfigs`
block into `app/build.gradle.kts`, and switch the workflow to `assembleRelease`.

## Roadmap

- Daily challenge with a streak counter
- Share card on win
- Sound effects
- New mechanics: one-way vehicles, oil patches, move-limit mode
- Coin economy and rewarded hints

## License

MIT — see [LICENSE](LICENSE).
