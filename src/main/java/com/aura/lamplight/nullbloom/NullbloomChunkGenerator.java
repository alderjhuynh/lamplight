package com.aura.lamplight.nullbloom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.ChunkPos;

public class NullbloomChunkGenerator extends ChunkGenerator {
	public static final MapCodec<NullbloomChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
		i -> i.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource))
			.apply(i, i.stable(NullbloomChunkGenerator::new))
	);

	public static final Identifier ALTAR_TEMPLATE = Nullbloom.id("ancient_altar");

	private static final int FLOWER_ONE_IN = 14;
	private static final long FLOWER_SALT = 0x6E756C6C626C6F6DL;

	private static final int SURFACE_Y = NullbloomDimension.SURFACE_Y;
	private static final int MIN_Y = NullbloomDimension.MIN_Y;

	public NullbloomChunkGenerator(BiomeSource biomeSource) {
		super(biomeSource);
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Nullbloom.id("nullbloom"), CODEC);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSets, RandomState randomState, long legacyLevelSeed) {
		return ChunkGeneratorStructureState.createForFlat(randomState, legacyLevelSeed, this.biomeSource, Stream.<Holder<StructureSet>>empty());
	}

	private static BlockState stateAt(int y) {
		if (y == MIN_Y) {
			return Blocks.BEDROCK.defaultBlockState();
		}
		if (y < SURFACE_Y - 3) {
			return Blocks.DEEPSLATE.defaultBlockState();
		}
		if (y < SURFACE_Y) {
			return Blocks.DIRT.defaultBlockState();
		}
		return Blocks.GRASS_BLOCK.defaultBlockState();
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
		Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		ChunkPos chunkPos = chunk.getPos();
		BlockState flower = NullbloomBlocks.NULLBLOOM.defaultBlockState();

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = MIN_Y; y <= SURFACE_Y; y++) {
					BlockState state = stateAt(y);
					chunk.setBlockState(pos.set(x, y, z), state);
					oceanFloor.update(x, y, z, state);
					worldSurface.update(x, y, z, state);
				}
				if (hasFlower(chunkPos.getMinBlockX() + x, chunkPos.getMinBlockZ() + z)) {
					chunk.setBlockState(pos.set(x, SURFACE_Y + 1, z), flower);
					worldSurface.update(x, SURFACE_Y + 1, z, flower);
				}
			}
		}
		return CompletableFuture.completedFuture(chunk);
	}

	private static boolean hasFlower(int worldX, int worldZ) {
		long h = (worldX * 0x9E3779B97F4A7C15L) ^ (worldZ * 0xC2B2AE3D27D4EB4FL) ^ FLOWER_SALT;
		h ^= h >>> 32;
		h *= 0xD6E8FEB86659FD93L;
		h ^= h >>> 32;
		h *= 0xD6E8FEB86659FD93L;
		h ^= h >>> 32;
		return Math.floorMod(h, (long) FLOWER_ONE_IN) == 0L;
	}

	@Override
	public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
		super.applyBiomeDecoration(level, chunk, structureManager);

		ChunkPos chunkPos = chunk.getPos();
		BoundingBox chunkBox = new BoundingBox(
			chunkPos.getMinBlockX(), level.getMinY(), chunkPos.getMinBlockZ(),
			chunkPos.getMaxBlockX(), level.getMaxY(), chunkPos.getMaxBlockZ()
		);
		BlockPos origin = new BlockPos(
			NullbloomDimension.ALTAR_ORIGIN_X,
			SURFACE_Y - NullbloomDimension.ALTAR_GROUND_LAYER,
			NullbloomDimension.ALTAR_ORIGIN_Z
		);
		BoundingBox altarBox = new BoundingBox(
			origin.getX(), origin.getY(), origin.getZ(),
			origin.getX() + NullbloomDimension.ALTAR_SIZE_XZ - 1, origin.getY() + 29, origin.getZ() + NullbloomDimension.ALTAR_SIZE_XZ - 1
		);
		if (!chunkBox.intersects(altarBox)) {
			return;
		}

		Optional<StructureTemplate> template = level.getLevel().getStructureManager().get(ALTAR_TEMPLATE);
		if (template.isEmpty()) {
			Nullbloom.LOGGER.error("Missing structure template {} (expected data/lamplight/structure/ancient_altar.nbt)", ALTAR_TEMPLATE);
			return;
		}

		StructurePlaceSettings settings = new StructurePlaceSettings()
			.setIgnoreEntities(true)
			.setBoundingBox(chunkBox)
			.setKnownShape(true)
			.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
		template.get().placeInWorld(level, origin, origin, settings, RandomSource.create(0L), Block.UPDATE_CLIENTS);
	}

	@Override
	public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState randomState, ChunkAccess protoChunk) {
	}

	@Override
	public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
	}

	@Override
	public int getSpawnHeight(LevelHeightAccessor heightAccessor) {
		return SURFACE_Y + 1;
	}

	@Override
	public int getGenDepth() {
		return NullbloomDimension.HEIGHT;
	}

	@Override
	public int getSeaLevel() {
		return MIN_Y;
	}

	@Override
	public int getMinY() {
		return MIN_Y;
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		return SURFACE_Y + 1;
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		BlockState[] column = new BlockState[SURFACE_Y - MIN_Y + 1];
		for (int i = 0; i < column.length; i++) {
			column[i] = stateAt(MIN_Y + i);
		}
		return new NoiseColumn(MIN_Y, column);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {
	}
}
