# Nikaalo — Game Design Doc

**Tagline:** Jam se apni auto nikaalo.
**Store title:** Nikaalo — Traffic Jam Puzzle
**Platform:** Android (Kotlin + Jetpack Compose), offline, no game engine.
**Version:** 1.1.0 (versionCode 2)

## 1. Core loop

Open app → pick next level → slide vehicles to clear a path → auto exits right
edge → stars awarded → next level unlocks. A session is a handful of 30–90
second puzzles.

## 2. Rules

1. Each vehicle slides only along its own axis. Nothing rotates.
2. Nothing passes through anything.
3. Gaay (cow) occupies one cell and never moves.
4. The auto is always horizontal, 2 cells long, and exits through the right edge.
5. A move is one vehicle sliding any distance in one direction.

The last rule matters: sliding a truck three cells counts as one move, which is
what makes the optimal-move counts meaningful.

## 3. Pieces

| Piece | Cells | Colour | Role |
|---|---|---|---|
| Auto | 2 | Yellow `#FFC53D` | Player |
| Bike | 2 | Violet `#9B7BFF` | Small blocker |
| Car | 2 | Blue `#4FA8FF` | Standard blocker |
| Thela | 2 | Orange `#FF8A4C` | Standard blocker |
| Bus | 3 | Teal `#2ED3B7` | Large blocker |
| Truck | 3 | Pink `#FF6B9D` | Large blocker |
| Gaay | 1 | Brown `#A1785C` | Immovable |

The yellow auto is the only warm-yellow object on the board, so the player never
has to be told which piece is theirs.

## 4. Difficulty curve

Every level is verified with a bitmask BFS at generation time, so `minMoves` is
the true optimal solution length. The curve below is the shipped 120-level set.

| Chapter | Theme | Levels | Grid | Optimal moves | Cow levels |
|---|---|---|---|---|---|
| 1 | Gali Mohalla | 15 | 6x6 | 3–6 | 0 |
| 2 | Sabzi Mandi | 25 | 6x6, 7x7 | 7–11 | 0 |
| 3 | Bus Stand | 25 | 6x6, 7x7 | 12–13 | 0 |
| 4 | Highway Toll | 20 | 6x6, 7x7 | 14–15 | 0 |
| 5 | Gaay Chowk | 20 | 6x6, 7x7 | 16–18 | 10 |
| 6 | Rush Hour Mumbai | 15 | 6x6, 7x7 | 19–23 | 0 |

Design intent per chapter:

- **Ch1** teaches the rule set without a tutorial. 3–4 move boards first.
- **Ch2** introduces the 7x7 grid and boards with 13–15 pieces.
- **Ch3–4** are the plateau where the player learns to look two moves ahead.
- **Ch5** adds cows: dead cells that break the symmetry of a solution path.
- **Ch6** is the endgame, 19–23 optimal moves, deliberately short at 15 levels.

Within a chapter, levels are ordered by optimal move count, and the curation
step picks round robin across move values so a chapter never becomes 25 boards
of identical difficulty.

## 5. Stars

| Stars | Condition |
|---|---|
| 3 | Solved in the optimal number of moves |
| 2 | Within optimum + 2 |
| 1 | Solved |

Three stars being exactly optimal gives skilled players a reason to replay, and
is only fair because the optimum is computed, not estimated.

## 6. Hints

The hint button runs the same BFS solver on the current board state and
highlights the exact next move on the optimal remaining path. It works from any
position, including one the player has made worse, so it can never give advice
that does not apply.

## 7. Feel and presentation

- Vehicles are drawn top-down with Canvas: body, cabin, windshield, wheels,
  headlights, outline. Vertical pieces are the same drawing rotated 90°.
- Dragging lifts and scales the piece slightly; it snaps to the grid with a
  spring (damping 0.72) and a haptic tick.
- The board is asphalt with dashed lane markings, dark walls, and a green exit
  gate on the right edge, so the goal is visible without text.
- Win overlay: "NIKAL GAYI!", stars pop in one at a time, move count shown
  against the optimum.
- Dark palette throughout (`#0B1015` base, yellow accent).

## 8. Progression and storage

Best move count per level in `SharedPreferences` (`nikaalo_progress`, keys
`best_<index>`). A level unlocks when the previous one is solved. No accounts,
no network calls.

## 9. Level pipeline

`tools/level_generator.py`:

- `mine` — hill climbs for verified boards. Random placement almost never
  produces a 15+ move board, so it mutates one blocker at a time and keeps
  candidates whose BFS length does not get shorter. Invariants: the auto is
  always piece 0, and no horizontal piece or cow may sit in the auto's lane.
- `curate` — re-verifies the pool, spreads it across chapters, writes
  `assets/levels.txt`.

The shipped set came from a 545-board verified pool.

## 10. Roadmap

**Next:** daily challenge with streak, share card on win, sound effects.
**Later:** one-way vehicles, oil patches (slide-through-only cells), move-limit
mode, coin economy with rewarded hints, release signing and Play Store listing.

## 11. Non-goals

No accounts, no ads before the game is fun, no multiplayer, no story mode, no
image or audio assets that inflate the APK.
