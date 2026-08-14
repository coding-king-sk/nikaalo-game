#!/usr/bin/env python3
"""Nikaalo level generator.

Every board is verified with a bitmask breadth-first search, so the exported
`minMoves` is the true optimal solution length - never an estimate. The app
uses the same algorithm at runtime to produce hints.

Pure random placement almost never produces a 15+ move board, so the miner
uses hill climbing: mutate one blocker at a time and keep boards whose BFS
solution length does not get shorter.

Usage:
    # mine a pool of verified boards for 10 minutes
    python3 tools/level_generator.py mine --seconds 600 --out pool.json

    # lay a pool out into chapters and write the app asset
    python3 tools/level_generator.py curate --pool pool.json \\
        --out app/src/main/assets/levels.txt
"""
from __future__ import annotations

import argparse
import json
import random
import time
from collections import Counter, deque

BLOCK2 = ["car", "bike", "thela"]
BLOCK3 = ["bus", "truck"]

CODE = {
    "auto": "a",
    "bike": "k",
    "car": "c",
    "thela": "t",
    "bus": "b",
    "truck": "T",
    "cow": "w",
}

# name, count, min moves, max moves, prefer boards containing a cow
CHAPTERS = [
    ("Gali Mohalla", 15, 3, 6, False),
    ("Sabzi Mandi", 25, 7, 11, False),
    ("Bus Stand", 25, 12, 13, False),
    ("Highway Toll", 20, 14, 15, False),
    ("Gaay Chowk", 20, 16, 18, True),
    ("Rush Hour Mumbai", 15, 19, 45, False),
]

# grid, blockers, cow chance
CONFIGS = [
    (6, 6, 0.00),
    (6, 9, 0.00),
    (6, 11, 0.00),
    (6, 12, 0.00),
    (7, 12, 0.00),
    (7, 13, 0.12),
    (7, 14, 0.10),
]


# ---------------------------------------------------------------- solver

def build_masks(size, specs):
    """Per-vehicle, per-position cell bitmasks over a size*size bitboard."""
    masks = []
    for v in specs:
        ln = v["len"]
        horizontal = v["dir"] == "h"
        row = []
        for p in range(size - ln + 1):
            m = 0
            for k in range(ln):
                if horizontal:
                    m |= 1 << (v["r"] * size + p + k)
                else:
                    m |= 1 << ((p + k) * size + v["c"])
            row.append(m)
        masks.append(row)
    start = tuple(v["c"] if v["dir"] == "h" else v["r"] for v in specs)
    return masks, start


def min_moves(size, specs, cap=160_000):
    """Optimal solution length, or None if unsolvable within the state cap.

    BFS explores by depth, so the first solution reached is always shortest.
    """
    masks, start = build_masks(size, specs)

    board = 0
    for i, p in enumerate(start):
        if board & masks[i][p]:
            return None  # overlapping pieces
        board |= masks[i][p]

    n = len(specs)
    lens = [v["len"] for v in specs]
    movable = [v["type"] != "cow" for v in specs]
    maxpos = [size - lens[i] for i in range(n)]
    player_len = lens[0]
    if start[0] + player_len >= size:
        return 0

    seen = {start}
    queue = deque([(start, 0)])
    expanded = 0

    while queue and expanded < cap:
        pos, depth = queue.popleft()
        expanded += 1

        board = 0
        for i in range(n):
            board |= masks[i][pos[i]]

        for i in range(n):
            if not movable[i]:
                continue
            others = board ^ masks[i][pos[i]]
            mi = masks[i]
            for step in (-1, 1):
                p = pos[i]
                while True:
                    p += step
                    if p < 0 or p > maxpos[i]:
                        break
                    if mi[p] & others:
                        break  # blocked, and everything beyond is unreachable
                    nxt = pos[:i] + (p,) + pos[i + 1:]
                    if nxt in seen:
                        continue
                    seen.add(nxt)
                    if i == 0 and p + player_len >= size:
                        return depth + 1
                    queue.append((nxt, depth + 1))
    return None


# ------------------------------------------------------------- generation

def cells_of(v):
    return set(
        (v["r"] + (k if v["dir"] == "v" else 0), v["c"] + (k if v["dir"] == "h" else 0))
        for k in range(v["len"])
    )


def random_piece(size, auto_row, occupied, cow_chance, rng):
    for _ in range(40):
        roll = rng.random()
        if roll < cow_chance:
            kind, ln, d = "cow", 1, "h"
        elif roll < cow_chance + 0.36:
            kind, ln, d = rng.choice(BLOCK3), 3, rng.choice(["h", "v"])
        else:
            kind, ln, d = rng.choice(BLOCK2), 2, rng.choice(["h", "v"])

        if d == "h":
            r, c = rng.randrange(size), rng.randrange(size - ln + 1)
        else:
            r, c = rng.randrange(size - ln + 1), rng.randrange(size)

        # A horizontal piece or a cow in the auto's lane can never be passed.
        if r == auto_row and (d == "h" or kind == "cow"):
            continue

        cand = {"type": kind, "r": r, "c": c, "len": ln, "dir": d}
        if cells_of(cand) & occupied:
            continue
        return cand
    return None


def random_board(size, blockers, cow_chance, rng):
    auto_row = rng.randrange(size)
    auto_col = rng.randrange(0, size - 2)
    specs = [{"type": "auto", "r": auto_row, "c": auto_col, "len": 2, "dir": "h"}]
    occupied = cells_of(specs[0])
    while len(specs) - 1 < blockers:
        piece = random_piece(size, auto_row, occupied, cow_chance, rng)
        if piece is None:
            break
        occupied |= cells_of(piece)
        specs.append(piece)
    return specs


def mutate(specs, size, cow_chance, rng):
    """Reposition exactly one blocker."""
    if len(specs) < 2:
        return None
    out = [dict(v) for v in specs]
    index = rng.randrange(1, len(out))
    occupied = set()
    for i, v in enumerate(out):
        if i != index:
            occupied |= cells_of(v)
    piece = random_piece(size, out[0]["r"], occupied, cow_chance, rng)
    if piece is None:
        return None
    out[index] = piece
    return out


def signature(size, specs):
    return (
        size,
        json.dumps(sorted((v["type"], v["r"], v["c"], v["len"], v["dir"]) for v in specs)),
    )


def mine(seconds, min_keep, seed, out_path):
    rng = random.Random(seed)
    started = time.time()
    collected = {}
    evaluated = 0
    reported = 0.0

    while time.time() - started < seconds:
        size, blockers, cow = CONFIGS[rng.randrange(len(CONFIGS))]
        current = random_board(size, blockers, cow, rng)
        score = min_moves(size, current) or 0
        evaluated += 1

        for _ in range(120):
            if time.time() - started > seconds:
                break
            candidate = mutate(current, size, cow, rng)
            if candidate is None:
                continue
            value = min_moves(size, candidate)
            evaluated += 1
            if not value:
                continue
            if value >= min_keep:
                collected.setdefault(signature(size, candidate), (value, candidate, size))
            # Accept equal-or-better boards so the search can drift sideways.
            if value >= score - 1:
                current, score = candidate, value

        now = time.time() - started
        if now - reported > 45:
            reported = now
            best = max((v for v, _s, _g in collected.values()), default=0)
            print(f"[{now:.0f}s] evaluated={evaluated} kept={len(collected)} max={best}", flush=True)

    pool = [
        {"grid": g, "minMoves": v, "vehicles": s}
        for v, s, g in collected.values()
    ]
    with open(out_path, "w", encoding="utf-8") as fh:
        json.dump(pool, fh)
    print(f"wrote {len(pool)} verified boards to {out_path}")


# --------------------------------------------------------------- curation

def has_cow(level):
    return any(v["type"] == "cow" for v in level["vehicles"])


def curate(pool_path, out_path, extra_pools):
    raw = json.load(open(pool_path, encoding="utf-8"))
    for extra in extra_pools or []:
        raw += json.load(open(extra, encoding="utf-8"))

    pool, seen = [], set()
    for item in raw:
        sig = signature(item["grid"], item["vehicles"])
        if sig in seen:
            continue
        verified = min_moves(item["grid"], item["vehicles"], cap=250_000)
        if not verified or verified < 2:
            continue
        seen.add(sig)
        pool.append({"grid": item["grid"], "minMoves": verified, "vehicles": item["vehicles"]})

    print("verified pool:", len(pool))
    print("distribution:", sorted(Counter(p["minMoves"] for p in pool).items()))

    used = set()

    def take(count, lo, hi, prefer_cow):
        """Pick `count` boards spread evenly across the whole move band."""
        by_moves = {}
        for p in pool:
            if lo <= p["minMoves"] <= hi and id(p) not in used:
                by_moves.setdefault(p["minMoves"], []).append(p)
        for group in by_moves.values():
            group.sort(key=lambda p: (not has_cow(p) if prefer_cow else has_cow(p), -len(p["vehicles"])))

        picked, values = [], sorted(by_moves)
        while len(picked) < count and any(by_moves[v] for v in values):
            for value in values:
                if len(picked) >= count:
                    break
                if by_moves[value]:
                    p = by_moves[value].pop(0)
                    used.add(id(p))
                    picked.append(p)
        picked.sort(key=lambda p: (p["minMoves"], len(p["vehicles"])))
        return picked

    lines = [
        "# Nikaalo levels - generated and BFS-verified by tools/level_generator.py",
        "# format: grid:minMoves:chapter:piece,piece,...",
        "# piece:  [type][row][col][len][dir]   a=auto k=bike c=car t=thela b=bus T=truck w=cow",
    ]
    total = 0
    for chapter, (name, count, lo, hi, prefer_cow) in enumerate(CHAPTERS, start=1):
        picked = take(count, lo, hi, prefer_cow)
        for p in picked:
            pieces = ",".join(
                f"{CODE[v['type']]}{v['r']}{v['c']}{v['len']}{v['dir']}" for v in p["vehicles"]
            )
            lines.append(f"{p['grid']}:{p['minMoves']}:{chapter}:{pieces}")
        total += len(picked)
        moves = [p["minMoves"] for p in picked]
        span = f"{min(moves)}-{max(moves)}" if moves else "-"
        print(f"Ch{chapter} {name}: {len(picked)}/{count} levels, {span} moves")

    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines) + "\n")
    print(f"wrote {total} levels to {out_path}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)

    miner = sub.add_parser("mine", help="hill climb for verified boards")
    miner.add_argument("--seconds", type=float, default=600.0)
    miner.add_argument("--min-keep", type=int, default=10)
    miner.add_argument("--seed", type=int, default=90210)
    miner.add_argument("--out", default="pool.json")

    builder = sub.add_parser("curate", help="lay a pool out into chapters")
    builder.add_argument("--pool", default="pool.json")
    builder.add_argument("--also", nargs="*", dest="extra")
    builder.add_argument("--out", default="app/src/main/assets/levels.txt")

    args = parser.parse_args()
    if args.command == "mine":
        mine(args.seconds, args.min_keep, args.seed, args.out)
    else:
        curate(args.pool, args.out, args.extra)


if __name__ == "__main__":
    main()
