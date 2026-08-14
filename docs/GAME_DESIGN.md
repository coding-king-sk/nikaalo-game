# Nikaalo — Game Design Doc

## 1. Core concept

Ek grid ek jammed chowk hai. Player ki **peeli auto** ko right wall ke exit tak pahunchana hai.

Rules (ek hi rule, tutorial ki zarurat nahi):

- Har vehicle sirf apni lambai ki direction me slide hota hai
- Koi vehicle ghum nahi sakta
- Kisi ke upar se cross nahi kar sakta
- Auto right edge tak pahunch gayi = level clear

Score = moves. Kam moves = zyada stars.

## 2. Vehicles

| Vehicle | Cells | Role |
|---|---|---|
| Auto (yellow) | 2 | Player, ek hi hota hai |
| Bike | 2 | Chhota blocker |
| Car | 2 | Standard blocker |
| Thela | 2 | Standard blocker, desi flavour |
| Bus | 3 | Bada blocker |
| Truck | 3 | Bada blocker |
| Gaay | 1 | Hilti nahi, permanent obstacle |

## 3. Chapters

| Chapter | Theme | Levels | Grid | Naya element |
|---|---|---|---|---|
| 1 | Gali Mohalla | 1-40 | 6x6 | Bike, Car (easy) |
| 2 | Sabzi Mandi | 41-90 | 6x6 | Thela, zyada density |
| 3 | Bus Stand | 91-150 | 6x6 | Bus, Truck (3-cell) |
| 4 | Highway Toll | 151-210 | 7x7 | Bada grid |
| 5 | Gaay Chowk | 211-270 | 7x7 | Immovable gaay |
| 6 | Rush Hour Mumbai | 271-350 | 8x8 | Max difficulty |

Difficulty curve minimum-moves se measure hoti hai: Ch1 = 4-10 moves, Ch3 = 12-20, Ch6 = 25-45.

## 4. Screens

1. **Home** — bada PLAY button, progress, stars, Levels button
2. **Level Select** — chapter-wise grid, star rating, locked levels
3. **Gameplay** — board, move counter, target moves, best moves, Undo / Restart / Hint
4. **Level Complete** — stars, moves vs optimal, Next / Retry
5. **Daily Challenge** (planned) — roz ek tough level + streak
6. **Settings** (planned) — sound, vibration, theme, restore purchase

## 5. Level generation

Manually 350 levels banana mahine lega. Instead `tools/level_generator.py`:

1. Grid pe random valid vehicles place karta hai
2. BFS se optimal solution length nikalta hai (BFS ka pehla solution hamesha shortest hota hai)
3. Sirf woh boards rakhta hai jo target move-range me fit hote hain
4. Difficulty ke hisaab se sort + chapter assign
5. `levels.json` export

Ek hi algorithm app ke andar bhi hai (`game/Solver.kt`) — wahan se **hints** aate hain: optimal path ka pehla move.

## 6. Tech stack

- **Kotlin + Jetpack Compose** — koi game engine nahi, APK ~10MB
- Board immutable hai; har move naya `Board` return karta hai → Compose state simple, BFS trivial
- Drag: `detectDragGestures` + `freeRange` clamp + snap-to-grid on release
- Storage: SharedPreferences (best moves, stars, unlocks)
- Levels: bundled asset, fully offline

## 7. Monetization (planned)

| Type | Detail |
|---|---|
| Interstitial ad | Har 3 levels ke baad |
| Rewarded ad | Hint ke liye |
| Rewarded ad | Level skip |
| Banner | Sirf level select screen pe |
| IAP ₹99 | Remove ads + 50 hints, lifetime |

**Rule:** gameplay screen pe kabhi banner ya popup nahi. Puzzle games me interruption = uninstall.

## 8. Retention (planned)

- Daily challenge + streak
- Local notification 7 PM
- Star collection + achievements
- Coin economy for hints
- Share card on every win (WhatsApp)

## 9. Play Store ASO

- **Title:** Nikaalo — Traffic Jam Puzzle
- **Short desc:** Jam se apni auto nikaalo! Dimaag ka dahi karne wale levels.
- **Keywords:** traffic puzzle, unblock car, sliding puzzle, offline puzzle, brain game
- **Icon:** bright yellow auto, top-down, chunky outline
- **Screenshots:** in-progress → almost solved → 3 stars → chapter map → daily challenge

## 10. Risks

| Risk | Fix |
|---|---|
| Genre saturated | Desi theme + gaay mechanic + Hinglish copy |
| Difficulty spike se churn | Analytics se fail-rate dekho, level order reshuffle |
| Ads se uninstall | Gameplay pe zero ads |
| Content khatam | Generator se update me 200 more levels |
