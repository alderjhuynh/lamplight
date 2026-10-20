#!/usr/bin/env python3
"""Generate terracotta slabs and stairs from vanilla's own models and textures.

The block models reference the existing ``minecraft:block/*_terracotta`` textures,
so this only writes JSON: no new texture files are needed. Run after changing the
Minecraft version if the vanilla slab/stair shapes or terracotta list change.

Usage:
    python3 tools/generate_terracotta_blocks.py
    MINECRAFT_CLIENT_JAR=/path/to/minecraft-client.jar \
        python3 tools/generate_terracotta_blocks.py
"""

from __future__ import annotations

import copy
import json
import os
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RESOURCES = ROOT / "src" / "main" / "resources"
NAMESPACE = "lamplight"
MINECRAFT = "minecraft"


def minecraft_version() -> str:
    properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    match = re.search(r"^minecraft_version=(.+)$", properties, re.MULTILINE)
    if not match:
        raise SystemExit("Could not read minecraft_version from gradle.properties")
    return match.group(1).strip()


def find_client_jar() -> Path:
    override = os.environ.get("MINECRAFT_CLIENT_JAR")
    if override:
        jar = Path(override).expanduser()
        if not jar.is_file():
            raise SystemExit(f"MINECRAFT_CLIENT_JAR does not exist: {jar}")
        return jar

    version = minecraft_version()
    candidates = [
        Path.home() / ".gradle" / "caches" / "fabric-loom" / version / "minecraft-client.jar",
        *sorted(
            (ROOT / ".gradle" / "loom-cache" / "minecraftMaven" / "net" / "minecraft").glob(
                f"minecraft-clientOnly-*/{version}/minecraft-clientOnly-*-{version}.jar"
            )
        ),
    ]
    required = {
        "assets/minecraft/blockstates/stone_slab.json",
        "assets/minecraft/blockstates/stone_stairs.json",
        "data/minecraft/tags/block/terracotta.json",
        "data/minecraft/loot_table/blocks/stone_slab.json",
        "data/minecraft/loot_table/blocks/stone_stairs.json",
    }
    for candidate in candidates:
        if not candidate.is_file():
            continue
        with zipfile.ZipFile(candidate) as jar:
            if required.issubset(jar.namelist()):
                return candidate
    raise SystemExit(
        "Could not find a Minecraft client jar containing both assets and data. "
        "Set MINECRAFT_CLIENT_JAR to its path."
    )


def load_json(jar: zipfile.ZipFile, name: str) -> dict:
    return json.loads(jar.read(name))


def write_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def replace_strings(value, replacements: dict[str, str]):
    """Deep-copy ``value`` while replacing exact string values."""
    if isinstance(value, dict):
        return {key: replace_strings(item, replacements) for key, item in value.items()}
    if isinstance(value, list):
        return [replace_strings(item, replacements) for item in value]
    if isinstance(value, str):
        return replacements.get(value, value)
    return copy.deepcopy(value)


def display_name(base_name: str) -> str:
    return base_name.replace("_", " ").title()


def main() -> None:
    jar_path = find_client_jar()
    with zipfile.ZipFile(jar_path) as jar:
        terracotta_ids = load_json(jar, "data/minecraft/tags/block/terracotta.json")["values"]
        slab_blockstate = load_json(jar, "assets/minecraft/blockstates/stone_slab.json")
        stair_blockstate = load_json(jar, "assets/minecraft/blockstates/stone_stairs.json")
        slab_loot = load_json(jar, "data/minecraft/loot_table/blocks/stone_slab.json")
        stair_loot = load_json(jar, "data/minecraft/loot_table/blocks/stone_stairs.json")

    bases = [entry.removeprefix(f"{MINECRAFT}:") for entry in terracotta_ids]

    slabs: list[str] = []
    stairs: list[str] = []
    translations: dict[str, str] = {}

    for base in bases:
        slab = f"{base}_slab"
        stair = f"{base}_stairs"
        slabs.append(slab)
        stairs.append(stair)

        texture = f"{MINECRAFT}:block/{base}"

        # --- Blockstates -------------------------------------------------
        write_json(
            RESOURCES / "assets" / NAMESPACE / "blockstates" / f"{slab}.json",
            replace_strings(
                slab_blockstate,
                {
                    "minecraft:block/stone_slab_top": f"{NAMESPACE}:block/{slab}_top",
                    "minecraft:block/stone_slab": f"{NAMESPACE}:block/{slab}",
                    "minecraft:block/stone": f"{MINECRAFT}:block/{base}",
                },
            ),
        )
        write_json(
            RESOURCES / "assets" / NAMESPACE / "blockstates" / f"{stair}.json",
            replace_strings(
                stair_blockstate,
                {
                    "minecraft:block/stone_stairs_inner": f"{NAMESPACE}:block/{stair}_inner",
                    "minecraft:block/stone_stairs_outer": f"{NAMESPACE}:block/{stair}_outer",
                    "minecraft:block/stone_stairs": f"{NAMESPACE}:block/{stair}",
                },
            ),
        )

        # --- Block models ------------------------------------------------
        textures = {"bottom": texture, "side": texture, "top": texture}
        for suffix, parent in (
            ("", "slab"),
            ("_top", "slab_top"),
            ("", "stairs"),
            ("_inner", "inner_stairs"),
            ("_outer", "outer_stairs"),
        ):
            if suffix == "" and parent == "slab":
                model_name = slab
            elif parent in ("slab", "slab_top"):
                model_name = slab + suffix
            else:
                model_name = stair + suffix
            write_json(
                RESOURCES / "assets" / NAMESPACE / "models" / "block" / f"{model_name}.json",
                {
                    "parent": f"{MINECRAFT}:block/{parent}",
                    "textures": textures,
                },
            )

        # --- Item definitions --------------------------------------------
        for item, model in ((slab, slab), (stair, stair)):
            write_json(
                RESOURCES / "assets" / NAMESPACE / "items" / f"{item}.json",
                {
                    "model": {
                        "type": "minecraft:model",
                        "model": f"{NAMESPACE}:block/{model}",
                    }
                },
            )

        # --- Loot tables -------------------------------------------------
        write_json(
            RESOURCES / "data" / NAMESPACE / "loot_table" / "blocks" / f"{slab}.json",
            replace_strings(
                slab_loot,
                {
                    "minecraft:stone_slab": f"{NAMESPACE}:{slab}",
                    "minecraft:blocks/stone_slab": f"{NAMESPACE}:blocks/{slab}",
                },
            ),
        )
        write_json(
            RESOURCES / "data" / NAMESPACE / "loot_table" / "blocks" / f"{stair}.json",
            replace_strings(
                stair_loot,
                {
                    "minecraft:stone_stairs": f"{NAMESPACE}:{stair}",
                    "minecraft:blocks/stone_stairs": f"{NAMESPACE}:blocks/{stair}",
                },
            ),
        )

        # --- Recipes -----------------------------------------------------
        write_json(
            RESOURCES / "data" / NAMESPACE / "recipe" / f"{slab}.json",
            {
                "type": "minecraft:crafting_shaped",
                "category": "building",
                "key": {"#": f"{MINECRAFT}:{base}"},
                "pattern": ["###"],
                "result": {"count": 6, "id": f"{NAMESPACE}:{slab}"},
            },
        )
        write_json(
            RESOURCES / "data" / NAMESPACE / "recipe" / f"{slab}_from_{base}_stonecutting.json",
            {
                "type": "minecraft:stonecutting",
                "ingredient": f"{MINECRAFT}:{base}",
                "result": {"count": 2, "id": f"{NAMESPACE}:{slab}"},
            },
        )
        write_json(
            RESOURCES / "data" / NAMESPACE / "recipe" / f"{stair}.json",
            {
                "type": "minecraft:crafting_shaped",
                "category": "building",
                "key": {"#": f"{MINECRAFT}:{base}"},
                "pattern": ["#  ", "## ", "###"],
                "result": {"count": 4, "id": f"{NAMESPACE}:{stair}"},
            },
        )
        write_json(
            RESOURCES / "data" / NAMESPACE / "recipe" / f"{stair}_from_{base}_stonecutting.json",
            {
                "type": "minecraft:stonecutting",
                "ingredient": f"{MINECRAFT}:{base}",
                "result": {"id": f"{NAMESPACE}:{stair}"},
            },
        )

        # --- Language ----------------------------------------------------
        translations[f"block.{NAMESPACE}.{slab}"] = f"{display_name(base)} Slab"
        translations[f"block.{NAMESPACE}.{stair}"] = f"{display_name(base)} Stairs"

    # --- Tags ------------------------------------------------------------
    tag_dir = RESOURCES / "data" / MINECRAFT / "tags"
    write_json(
        tag_dir / "block" / "mineable" / "pickaxe.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in slabs + stairs]},
    )
    write_json(
        tag_dir / "block" / "slabs.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in slabs]},
    )
    write_json(
        tag_dir / "block" / "stairs.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in stairs]},
    )
    write_json(
        tag_dir / "item" / "slabs.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in slabs]},
    )
    write_json(
        tag_dir / "item" / "stairs.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in stairs]},
    )

    # --- Language file ---------------------------------------------------
    lang_path = RESOURCES / "assets" / NAMESPACE / "lang" / "en_us.json"
    lang = json.loads(lang_path.read_text(encoding="utf-8"))
    for key, value in translations.items():
        lang.setdefault(key, value)
    lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    print(f"Generated {len(slabs)} slabs and {len(stairs)} stairs from {jar_path}")
    print("Using vanilla textures: " + ", ".join(f"minecraft:block/{base}" for base in bases))


if __name__ == "__main__":
    try:
        main()
    except (OSError, KeyError, zipfile.BadZipFile) as error:
        print(f"error: {error}", file=sys.stderr)
        raise
