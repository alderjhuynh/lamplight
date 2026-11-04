package com.aura.lamplight.vantage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class SpawnWaystonePlacer {
	private static final TagKey<Structure> WAYSTONE_STRUCTURES =
		TagKey.create(Registries.STRUCTURE, Vantage.id("waystone"));

	private SpawnWaystonePlacer() {
	}

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTED.register(SpawnWaystonePlacer::placeAtSpawn);
	}

	private static void placeAtSpawn(MinecraftServer server) {
		ServerLevel level = server.overworld();
		if (level == null || level.dimension() != Level.OVERWORLD) {
			return;
		}
		try {
			placeAtWorldSpawn(level);
		} catch (Exception e) {
			Vantage.LOGGER.error("Failed to place a waystone structure at the world spawn", e);
		}
	}

	private static void placeAtWorldSpawn(ServerLevel level) {
		List<Holder<Structure>> candidates = new ArrayList<>();
		level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getTagOrEmpty(WAYSTONE_STRUCTURES).forEach(candidates::add);
		if (candidates.isEmpty()) {
			Vantage.LOGGER.warn("No waystone structures are registered under {}, skipping spawn placement", WAYSTONE_STRUCTURES.location());
			return;
		}
		Set<Structure> waystoneStructures = candidates.stream().map(Holder::value).collect(Collectors.toUnmodifiableSet());

		ChunkPos origin = ChunkPos.containing(spawnPos(level));
		if (waystoneAlreadyPlaced(level, origin, waystoneStructures)) {
			Vantage.LOGGER.info("A waystone structure was already generated near the world spawn, not placing another");
			return;
		}

		Holder<Structure> holder = candidates.get(level.getRandom().nextInt(candidates.size()));
		Structure structure = holder.value();
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		StructureManager structureManager = level.structureManager();

		StructureStart start = structure.generate(
			holder,
			level.dimension(),
			level.registryAccess(),
			generator,
			generator.getBiomeSource(),
			level.getChunkSource().randomState(),
			level.getStructureManager(),
			level.getSeed(),
			origin,
			0,
			level,
			biome -> true
		);
		if (!start.isValid()) {
			Vantage.LOGGER.warn("Could not generate a valid waystone structure at the world spawn, skipping placement");
			return;
		}

		Vantage.LOGGER.info(
			"Placed {} at the world spawn chunk {}, {}",
			level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(structure),
			origin.x(),
			origin.z()
		);

		BoundingBox bounds = start.getBoundingBox();
		ChunkPos minChunk = new ChunkPos(SectionPos.blockToSectionCoord(bounds.minX()), SectionPos.blockToSectionCoord(bounds.minZ()));
		ChunkPos maxChunk = new ChunkPos(SectionPos.blockToSectionCoord(bounds.maxX()), SectionPos.blockToSectionCoord(bounds.maxZ()));
		ChunkPos.rangeClosed(minChunk, maxChunk).forEach(pos -> {
			level.getChunk(pos.x(), pos.z(), ChunkStatus.FULL, true);
			start.placeInChunk(
				level,
				structureManager,
				generator,
				level.getRandom(),
				new BoundingBox(pos.getMinBlockX(), level.getMinY(), pos.getMinBlockZ(), pos.getMaxBlockX(), level.getMaxY() + 1, pos.getMaxBlockZ()),
				pos
			);
		});

		level.getChunk(origin.x(), origin.z(), ChunkStatus.STRUCTURE_STARTS, true).setStartForStructure(structure, start);
	}

	private static BlockPos spawnPos(ServerLevel level) {
		return level.getLevelData().getRespawnData().pos();
	}

	private static boolean waystoneAlreadyPlaced(ServerLevel level, ChunkPos origin, Set<Structure> waystoneStructures) {
		for (int x = origin.x() - 1; x <= origin.x() + 1; x++) {
			for (int z = origin.z() - 1; z <= origin.z() + 1; z++) {
				ChunkAccess chunk = level.getChunk(x, z, ChunkStatus.STRUCTURE_STARTS, false);
				if (chunk == null) {
					continue;
				}
				for (var entry : chunk.getAllStarts().entrySet()) {
					if (waystoneStructures.contains(entry.getKey()) && entry.getValue().isValid()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
