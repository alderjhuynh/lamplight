#!/usr/bin/env python3
"""Generate derivative slabs, stairs, and walls from vanilla Minecraft blocks.

Every model references the base block's existing textures, so this only writes
JSON: no new texture files are needed. Column blocks such as basalt, deepslate,
and cut sandstone keep their separate top/side textures on slabs and stairs,
while their walls use the side texture like vanilla's sandstone walls do.

Covers terracotta, the stone-like derivatives, and the 16 dyed concrete
blocks.

Usage:
    python3 tools/generate_derivative_blocks.py
    MINECRAFT_CLIENT_JAR=/path/to/minecraft-client.jar \
        python3 tools/generate_derivative_blocks.py
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

# (name prefix, base block, kinds to generate)
STONE_DERIVATIVES: list[tuple[str, str, tuple[str, ...]]] = [
    ("stone", "minecraft:stone", ("wall",)),
    ("deepslate", "minecraft:deepslate", ("slab", "stairs", "wall")),
    ("packed_mud", "minecraft:packed_mud", ("slab", "stairs", "wall")),
    ("netherrack", "minecraft:netherrack", ("slab", "stairs", "wall")),
    ("basalt", "minecraft:basalt", ("slab", "stairs", "wall")),
    ("quartz_brick", "minecraft:quartz_bricks", ("slab", "stairs", "wall")),
    ("polished_andesite", "minecraft:polished_andesite", ("wall",)),
    ("polished_diorite", "minecraft:polished_diorite", ("wall",)),
    ("polished_granite", "minecraft:polished_granite", ("wall",)),
    ("polished_basalt", "minecraft:polished_basalt", ("slab", "stairs", "wall")),
    ("cut_sandstone", "minecraft:cut_sandstone", ("stairs", "wall")),
    ("cut_red_sandstone", "minecraft:cut_red_sandstone", ("stairs", "wall")),
    ("smooth_stone", "minecraft:smooth_stone", ("stairs", "wall")),
    ("smooth_basalt", "minecraft:smooth_basalt", ("slab", "stairs", "wall")),
    ("purpur", "minecraft:purpur_block", ("wall",)),
    ("quartz", "minecraft:quartz_block", ("wall",)),
    ("prismarine_brick", "minecraft:prismarine_bricks", ("wall",)),
    ("dark_prismarine", "minecraft:dark_prismarine", ("wall",)),
    ("smooth_red_sandstone", "minecraft:smooth_red_sandstone", ("wall",)),
    ("smooth_sandstone", "minecraft:smooth_sandstone", ("wall",)),
    ("end_stone", "minecraft:end_stone", ("slab", "stairs", "wall")),
]

CONCRETE_COLORS = [
    "white",
    "orange",
    "magenta",
    "light_blue",
    "yellow",
    "lime",
    "pink",
    "gray",
    "light_gray",
    "cyan",
    "purple",
    "blue",
    "brown",
    "green",
    "red",
    "black",
]


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
        "assets/minecraft/blockstates/stone_brick_wall.json",
        "data/minecraft/tags/block/terracotta.json",
        "data/minecraft/loot_table/blocks/stone_slab.json",
        "data/minecraft/loot_table/blocks/stone_stairs.json",
        "data/minecraft/loot_table/blocks/stone_brick_wall.json",
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


def face_textures(jar: zipfile.ZipFile, base_id: str) -> dict[str, str]:
    """Derive slab/stair/wall textures from the base block's model."""
    _, base_name = base_id.split(":", 1)
    model = load_json(jar, f"assets/minecraft/models/block/{base_name}.json")
    textures = model.get("textures", {})

    if "all" in textures:
        all_texture = textures["all"]
        return {"bottom": all_texture, "side": all_texture, "top": all_texture, "wall": all_texture}

    if "end" in textures:
        return {
            "bottom": textures["end"],
            "side": textures["side"],
            "top": textures["end"],
            "wall": textures["side"],
        }

    if "side" in textures:
        side = textures["side"]
        return {
            "bottom": textures.get("bottom", side),
            "side": side,
            "top": textures.get("top", side),
            "wall": side,
        }

    raise SystemExit(f"Unsupported model textures for {base_id}: {textures}")


def display_name(prefix: str) -> str:
    return prefix.replace("_", " ").title()


def item_definition(model: str) -> dict:
    return {"model": {"type": "minecraft:model", "model": model}}


def emit_slab(name: str, base_id: str, faces: dict[str, str], templates: dict) -> None:
    base_name = base_id.split(":", 1)[1]
    texture_set = {"bottom": faces["bottom"], "side": faces["side"], "top": faces["top"]}

    write_json(
        RESOURCES / "assets" / NAMESPACE / "blockstates" / f"{name}.json",
        replace_strings(
            templates["slab_blockstate"],
            {
                "minecraft:block/stone_slab_top": f"{NAMESPACE}:block/{name}_top",
                "minecraft:block/stone_slab": f"{NAMESPACE}:block/{name}",
                "minecraft:block/stone": f"{MINECRAFT}:block/{base_name}",
            },
        ),
    )
    write_json(
        RESOURCES / "assets" / NAMESPACE / "models" / "block" / f"{name}.json",
        {"parent": f"{MINECRAFT}:block/slab", "textures": texture_set},
    )
    write_json(
        RESOURCES / "assets" / NAMESPACE / "models" / "block" / f"{name}_top.json",
        {"parent": f"{MINECRAFT}:block/slab_top", "textures": texture_set},
    )
    write_json(
        RESOURCES / "assets" / NAMESPACE / "items" / f"{name}.json",
        item_definition(f"{NAMESPACE}:block/{name}"),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "loot_table" / "blocks" / f"{name}.json",
        replace_strings(
            templates["slab_loot"],
            {
                "minecraft:stone_slab": f"{NAMESPACE}:{name}",
                "minecraft:blocks/stone_slab": f"{NAMESPACE}:blocks/{name}",
            },
        ),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}.json",
        {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": {"#": base_id},
            "pattern": ["###"],
            "result": {"count": 6, "id": f"{NAMESPACE}:{name}"},
        },
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}_from_{base_name}_stonecutting.json",
        {
            "type": "minecraft:stonecutting",
            "ingredient": base_id,
            "result": {"count": 2, "id": f"{NAMESPACE}:{name}"},
        },
    )


def emit_stairs(name: str, base_id: str, faces: dict[str, str], templates: dict) -> None:
    base_name = base_id.split(":", 1)[1]
    texture_set = {"bottom": faces["bottom"], "side": faces["side"], "top": faces["top"]}

    write_json(
        RESOURCES / "assets" / NAMESPACE / "blockstates" / f"{name}.json",
        replace_strings(
            templates["stair_blockstate"],
            {
                "minecraft:block/stone_stairs_inner": f"{NAMESPACE}:block/{name}_inner",
                "minecraft:block/stone_stairs_outer": f"{NAMESPACE}:block/{name}_outer",
                "minecraft:block/stone_stairs": f"{NAMESPACE}:block/{name}",
            },
        ),
    )
    for suffix, parent in (
        ("", "stairs"),
        ("_inner", "inner_stairs"),
        ("_outer", "outer_stairs"),
    ):
        write_json(
            RESOURCES / "assets" / NAMESPACE / "models" / "block" / f"{name}{suffix}.json",
            {"parent": f"{MINECRAFT}:block/{parent}", "textures": texture_set},
        )
    write_json(
        RESOURCES / "assets" / NAMESPACE / "items" / f"{name}.json",
        item_definition(f"{NAMESPACE}:block/{name}"),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "loot_table" / "blocks" / f"{name}.json",
        replace_strings(
            templates["stair_loot"],
            {
                "minecraft:stone_stairs": f"{NAMESPACE}:{name}",
                "minecraft:blocks/stone_stairs": f"{NAMESPACE}:blocks/{name}",
            },
        ),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}.json",
        {
            "type": "minecraft:crafting_shaped",
            "category": "building",
            "key": {"#": base_id},
            "pattern": ["#  ", "## ", "###"],
            "result": {"count": 4, "id": f"{NAMESPACE}:{name}"},
        },
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}_from_{base_name}_stonecutting.json",
        {
            "type": "minecraft:stonecutting",
            "ingredient": base_id,
            "result": {"id": f"{NAMESPACE}:{name}"},
        },
    )


def emit_wall(name: str, base_id: str, faces: dict[str, str], templates: dict) -> None:
    base_name = base_id.split(":", 1)[1]

    write_json(
        RESOURCES / "assets" / NAMESPACE / "blockstates" / f"{name}.json",
        replace_strings(
            templates["wall_blockstate"],
            {
                "minecraft:block/stone_brick_wall_post": f"{NAMESPACE}:block/{name}_post",
                "minecraft:block/stone_brick_wall_side_tall": f"{NAMESPACE}:block/{name}_side_tall",
                "minecraft:block/stone_brick_wall_side": f"{NAMESPACE}:block/{name}_side",
            },
        ),
    )
    for suffix, parent in (
        ("_post", "template_wall_post"),
        ("_side", "template_wall_side"),
        ("_side_tall", "template_wall_side_tall"),
        ("_inventory", "wall_inventory"),
    ):
        write_json(
            RESOURCES / "assets" / NAMESPACE / "models" / "block" / f"{name}{suffix}.json",
            {"parent": f"{MINECRAFT}:block/{parent}", "textures": {"wall": faces["wall"]}},
        )
    write_json(
        RESOURCES / "assets" / NAMESPACE / "items" / f"{name}.json",
        item_definition(f"{NAMESPACE}:block/{name}_inventory"),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "loot_table" / "blocks" / f"{name}.json",
        replace_strings(
            templates["wall_loot"],
            {
                "minecraft:stone_brick_wall": f"{NAMESPACE}:{name}",
                "minecraft:blocks/stone_brick_wall": f"{NAMESPACE}:blocks/{name}",
            },
        ),
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}.json",
        {
            "type": "minecraft:crafting_shaped",
            "key": {"#": base_id},
            "pattern": ["###", "###"],
            "result": {"count": 6, "id": f"{NAMESPACE}:{name}"},
        },
    )
    write_json(
        RESOURCES / "data" / NAMESPACE / "recipe" / f"{name}_from_{base_name}_stonecutting.json",
        {
            "type": "minecraft:stonecutting",
            "ingredient": base_id,
            "result": {"id": f"{NAMESPACE}:{name}"},
        },
    )


def emit_slab_and_stairs(
    prefix: str,
    base_id: str,
    faces: dict[str, str],
    templates: dict,
    slabs: list[str],
    stairs: list[str],
    translations: dict[str, str],
) -> None:
    slab = f"{prefix}_slab"
    stair = f"{prefix}_stairs"

    emit_slab(slab, base_id, faces, templates)
    emit_stairs(stair, base_id, faces, templates)
    slabs.append(slab)
    stairs.append(stair)

    translations[f"block.{NAMESPACE}.{slab}"] = f"{display_name(prefix)} Slab"
    translations[f"block.{NAMESPACE}.{stair}"] = f"{display_name(prefix)} Stairs"


def main() -> None:
    jar_path = find_client_jar()
    slabs: list[str] = []
    stairs: list[str] = []
    walls: list[str] = []
    translations: dict[str, str] = {}

    with zipfile.ZipFile(jar_path) as jar:
        terracotta_ids = load_json(jar, "data/minecraft/tags/block/terracotta.json")["values"]
        templates = {
            "slab_blockstate": load_json(jar, "assets/minecraft/blockstates/stone_slab.json"),
            "stair_blockstate": load_json(jar, "assets/minecraft/blockstates/stone_stairs.json"),
            "wall_blockstate": load_json(jar, "assets/minecraft/blockstates/stone_brick_wall.json"),
            "slab_loot": load_json(jar, "data/minecraft/loot_table/blocks/stone_slab.json"),
            "stair_loot": load_json(jar, "data/minecraft/loot_table/blocks/stone_stairs.json"),
            "wall_loot": load_json(jar, "data/minecraft/loot_table/blocks/stone_brick_wall.json"),
        }

        # Terracotta: one slab and one stair for every block in vanilla's tag.
        for base_id in terracotta_ids:
            base_name = base_id.split(":", 1)[1]
            emit_slab_and_stairs(
                base_name,
                base_id,
                face_textures(jar, base_id),
                templates,
                slabs,
                stairs,
                translations,
            )

        # Stone-like derivatives from the user-facing list.
        for prefix, base_id, kinds in STONE_DERIVATIVES:
            faces = face_textures(jar, base_id)
            for kind in kinds:
                name = f"{prefix}_{kind}"
                if kind == "slab":
                    emit_slab(name, base_id, faces, templates)
                    slabs.append(name)
                elif kind == "stairs":
                    emit_stairs(name, base_id, faces, templates)
                    stairs.append(name)
                elif kind == "wall":
                    emit_wall(name, base_id, faces, templates)
                    walls.append(name)
                else:
                    raise SystemExit(f"Unknown block kind: {kind}")
                translations[f"block.{NAMESPACE}.{name}"] = f"{display_name(prefix)} {kind.title()}"

        # Concrete: slabs and stairs for the 16 dyed concrete blocks.
        for color in CONCRETE_COLORS:
            prefix = f"{color}_concrete"
            base_id = f"{MINECRAFT}:{prefix}"
            emit_slab_and_stairs(
                prefix,
                base_id,
                face_textures(jar, base_id),
                templates,
                slabs,
                stairs,
                translations,
            )

    # --- Tags ------------------------------------------------------------
    tag_dir = RESOURCES / "data" / MINECRAFT / "tags"
    all_blocks = slabs + stairs + walls
    write_json(
        tag_dir / "block" / "mineable" / "pickaxe.json",
        {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in all_blocks]},
    )
    for collection, names in (("slabs", slabs), ("stairs", stairs), ("walls", walls)):
        for kind in ("block", "item"):
            write_json(
                tag_dir / kind / f"{collection}.json",
                {"replace": False, "values": [f"{NAMESPACE}:{name}" for name in names]},
            )

    # --- Language file ---------------------------------------------------
    lang_path = RESOURCES / "assets" / NAMESPACE / "lang" / "en_us.json"
    lang = json.loads(lang_path.read_text(encoding="utf-8"))
    for key, value in translations.items():
        lang.setdefault(key, value)
    lang_path.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    print(f"Generated {len(slabs)} slabs, {len(stairs)} stairs, and {len(walls)} walls")
    print(f"Used vanilla models from {jar_path}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, KeyError, zipfile.BadZipFile) as error:
        print(f"error: {error}", file=sys.stderr)
        raise
