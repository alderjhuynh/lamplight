package com.aura.lamplight.picturesque.client.ambient;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class AmbientContext {
	private static final int BLEND_DISTANCE = 10;

	private final List<Holder<Biome>> biomeSamples = new ArrayList<>(5);
	private boolean precipitation;

	float open;
	float cave;
	float night;
	float day = 1.0F;
	float rain;
	boolean submerged;
	boolean hasSky = true;
	double eyeY;

	int leafCount;
	int fallCount;
	int beeCount;
	@Nullable Vec3 nearestFall;

	void reset() {
		biomeSamples.clear();
		precipitation = false;
		open = 0.0F;
		cave = 0.0F;
		night = 0.0F;
		day = 1.0F;
		rain = 0.0F;
		submerged = false;
		leafCount = 0;
		fallCount = 0;
		beeCount = 0;
		nearestFall = null;
	}

	void sampleBiomes(ClientLevel level, LocalPlayer player) {
		BlockPos p = player.blockPosition();
		biomeSamples.clear();
		biomeSamples.add(level.getBiome(p));
		biomeSamples.add(level.getBiome(p.offset(BLEND_DISTANCE, 0, 0)));
		biomeSamples.add(level.getBiome(p.offset(-BLEND_DISTANCE, 0, 0)));
		biomeSamples.add(level.getBiome(p.offset(0, 0, BLEND_DISTANCE)));
		biomeSamples.add(level.getBiome(p.offset(0, 0, -BLEND_DISTANCE)));
		precipitation = biomeSamples.get(0).value().hasPrecipitation();
	}

	void update(ClientLevel level, LocalPlayer player) {
		hasSky = level.dimensionType().hasSkyLight();
		submerged = player.isUnderWater();
		eyeY = player.getEyeY();

		BlockPos eye = BlockPos.containing(player.getEyePosition());
		int sky = level.getBrightness(LightLayer.SKY, eye);
		open = hasSky ? Mth.clamp((sky - 2) / 10.0F, 0.0F, 1.0F) : 0.0F;

		cave = 0.0F;
		if (hasSky && !submerged && sky <= 1) {
			int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, eye.getX(), eye.getZ());
			cave = smooth(((float) (surface - eyeY) - 6.0F) / 10.0F);
		}

		night = nightFactor(level.getDefaultClockTime());
		day = 1.0F - night;

		float raw = level.getRainLevel(1.0F);
		rain = precipitation ? raw : 0.0F;
	}

	float biomeShare(TagKey<Biome> tag) {
		if (biomeSamples.isEmpty()) {
			return 0.0F;
		}
		int hits = 0;
		for (Holder<Biome> biome : biomeSamples) {
			if (biome.is(tag)) {
				hits++;
			}
		}
		return hits / (float) biomeSamples.size();
	}

	@SafeVarargs
	final float maxShare(TagKey<Biome>... tags) {
		float best = 0.0F;
		for (TagKey<Biome> tag : tags) {
			best = Math.max(best, biomeShare(tag));
		}
		return best;
	}

	float surfaceGate() {
		if (!hasSky) {
			return 0.0F;
		}
		float gate = Math.max(open, 0.12F * (1.0F - cave));
		return submerged ? gate * 0.25F : gate;
	}

	float gate(AmbientLayer.Gate gate) {
		return switch (gate) {
			case SURFACE -> surfaceGate();
			case CAVE -> cave;
			case NONE -> 1.0F;
		};
	}

	static float smooth(float x) {
		float t = Mth.clamp(x, 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}

	static float nightFactor(long clock) {
		long t = ((clock % 24000L) + 24000L) % 24000L;
		if (t >= 13500L && t < 22500L) {
			return 1.0F;
		}
		if (t >= 22500L) {
			return 1.0F - (t - 22500L) / 2500.0F;
		}
		if (t < 1000L) {
			return 1.0F - (t + 1500L) / 2500.0F;
		}
		if (t >= 12000L) {
			return (t - 12000L) / 1500.0F;
		}
		return 0.0F;
	}
}
