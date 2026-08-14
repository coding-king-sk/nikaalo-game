#!/usr/bin/env python3
"""Nikaalo level generator.

Randomly places vehicles on a grid, solves each board with BFS, and keeps only
the boards whose optimal solution length falls inside the requested difficulty
window. Pure standard library -- no dependencies.

Usage:
    python3 tools/level_generator.py --count 60 --min-moves 6 --max-moves 14 \\
        --grid 6 --out app/src/main/assets/levels.json
"""

from __future__ import annotations

import argparse
import json
import random
from collections import deque

BLOCKER_TYPES_2 = ["car", "bike", "thela"]
BLOCKER_TYPES_3 = ["bus", "truck"]


def cells(vehicle):
    r, c, length, direction = vehicle["r"], vehicle["c"], vehicle["len"], vehicle["dir"]
    if direction == "h":
        return [(r, c + i) for i in range(length)]
    return [(r + i, c) for i in range(length)]


def build_grid(vehicles, size):
    grid = [[-1] * size for _ in range(size)]
    for index, vehicle in enumerate(vehicles):
        for r, c in cells(vehicle):
            if not (0 <= r < size and 0 <= c < size) or grid[r][c] != -1:
                return None
            grid[r][c] = index
    return grid


def state_of(vehicles):
    return tuple((v["r"], v["c"]) for v in vehicles)


def apply_state(vehicles, state):
    return [dict(v, r=pos[0], c=pos[1]) for v, pos in zip(vehicles, state)]


def is_solved(vehicles, size):
    auto = vehicles[0]
    return auto["c"] + auto["len"] >= size


def neighbours(vehicles, size):
    grid = build_grid(vehicles, size)
    if grid is None:
        return
    for index, vehicle in enumerate(vehicles):
        if vehicle["type"] == "cow":
            continue
        horizontal = vehicle["dir"] == "h"
        # backward (left / up)
        step = 1
        while True:
            r = vehicle["r"] if horizontal else vehicle["r"] - step
            c = vehicle["c"] - step if horizontal else vehicle["c"]
            if r < 0 or c < 0 or grid[r][c] != -1:
                break
            moved = list(vehicles)
            moved[index] = dict(vehicle, r=r, c=c)
            yield state_of(moved)
            step += 1
        # forward (right / down)
        step = 1
        while True:
            tail_r = vehicle["r"] + (0 if horizontal else vehicle["len"] - 1) + (0 if horizontal else step)
            tail_c = vehicle["c"] + (vehicle["len"] - 1 + step if horizontal else 0)
            if tail_r >= size or tail_c >= size or grid[tail_r][tail_c] != -1:
                break
            moved = list(vehicles)
            moved[index] = dict(
                vehicle,
                r=vehicle["r"] if horizontal else vehicle["r"] + step,
                c=vehicle["c"] + step if horizontal else vehicle["c"],
            )
            yield state_of(moved)
            step += 1


def min_moves(vehicles, size, max_states=200_000):
    """BFS: the first solution found is always the shortest one."""
    start = state_of(vehicles)
    if is_solved(vehicles, size):
        return 0
    seen = {start}
    queue = deque([(start, 0)])
    expanded = 0
    while queue and expanded < max_states:
        state, depth = queue.popleft()
        expanded += 1
        current = apply_state(vehicles, state)
        for nxt in neighbours(current, size):
            if nxt in seen:
                continue
            seen.add(nxt)
            candidate = apply_state(vehicles, nxt)
            if is_solved(candidate, size):
                return depth + 1
            queue.append((nxt, depth + 1))
    return None


def random_board(size, blockers, cow_chance, rng):
    auto_row = rng.randrange(size)
    auto_col = rng.randrange(0, max(1, size - 3))
    vehicles = [{"type": "auto", "r": auto_row, "c": auto_col, "len": 2, "dir": "h"}]

    attempts = 0
    while len(vehicles) - 1 < blockers and attempts < blockers * 40:
        attempts += 1
        if rng.random() < cow_chance:
            kind, length, direction = "cow", 1, "h"
        elif rng.random() < 0.3:
            kind, length = rng.choice(BLOCKER_TYPES_3), 3
            direction = rng.choice(["h", "v"])
        else:
            kind, length = rng.choice(BLOCKER_TYPES_2), 2
            direction = rng.choice(["h", "v"])

        if direction == "h":
            r, c = rng.randrange(size), rng.randrange(size - length + 1)
        else:
            r, c = rng.randrange(size - length + 1), rng.randrange(size)

        candidate = {"type": kind, "r": r, "c": c, "len": length, "dir": direction}
        # A horizontal blocker on the auto's row can never be passed.
        if direction == "h" and r == auto_row:
            continue
        if kind == "cow" and r == auto_row:
            continue
        if build_grid(vehicles + [candidate], size) is None:
            continue
        vehicles.append(candidate)
    return vehicles


def main():
    parser = argparse.ArgumentParser(description="Generate Nikaalo levels")
    parser.add_argument("--count", type=int, default=40)
    parser.add_argument("--grid", type=int, default=6)
    parser.add_argument("--min-moves", type=int, default=4)
    parser.add_argument("--max-moves", type=int, default=14)
    parser.add_argument("--blockers", type=int, default=7)
    parser.add_argument("--cow-chance", type=float, default=0.0)
    parser.add_argument("--tries", type=int, default=20000)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--out", default="levels.json")
    args = parser.parse_args()

    rng = random.Random(args.seed)
    levels = []
    seen_signatures = set()

    for _ in range(args.tries):
        if len(levels) >= args.count:
            break
        vehicles = random_board(args.grid, args.blockers, args.cow_chance, rng)
        if len(vehicles) < 3:
            continue
        signature = json.dumps(vehicles, sort_keys=True)
        if signature in seen_signatures:
            continue
        seen_signatures.add(signature)

        moves = min_moves(vehicles, args.grid)
        if moves is None or not (args.min_moves <= moves <= args.max_moves):
            continue

        levels.append({
            "id": len(levels) + 1,
            "grid": args.grid,
            "minMoves": moves,
            "chapter": 1,
            "vehicles": vehicles,
        })

    levels.sort(key=lambda level: level["minMoves"])
    for index, level in enumerate(levels):
        level["id"] = index + 1
        level["chapter"] = min(6, index // 40 + 1)

    with open(args.out, "w", encoding="utf-8") as handle:
        json.dump(levels, handle, indent=2)
        handle.write("\n")

    print(f"Wrote {len(levels)} levels to {args.out}")
    if levels:
        print(
            "Difficulty range: "
            f"{levels[0]['minMoves']} - {levels[-1]['minMoves']} moves"
        )


if __name__ == "__main__":
    main()
