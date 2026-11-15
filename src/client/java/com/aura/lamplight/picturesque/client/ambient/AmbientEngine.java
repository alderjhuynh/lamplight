package com.aura.lamplight.picturesque.client.ambient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.world.entity.animal.bee.Bee;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class AmbientEngine {
	private static final AmbientEngine INSTANCE = new AmbientEngine();

	private static final int MAX_STREAMED = 5;
	private static final float START_THRESHOLD = 0.02F;
	private static final float STOP_THRESHOLD = 0.002F;
	private static final float INCUMBENT_BONUS = 0.05F;
	private static final int RETRY_DELAY_TICKS = 40;

	private final List<AmbientLayer> layers = AmbientLayers.create();
	private final AmbientLoopInstance[] instances = new AmbientLoopInstance[layers.size()];
	private final long[] startedAt = new long[layers.size()];
	private final long[] retryAt = new long[layers.size()];
	private final float[] targets = new float[layers.size()];
	private final AmbientContext ctx = new AmbientContext();
	private final AmbientScanner scanner = new AmbientScanner();

	private @Nullable ClientLevel level;
	private long tickCount;

	private AmbientEngine() {}

	public static AmbientEngine get() {
		return INSTANCE;
	}

	public void tick(Minecraft mc, boolean enabled) {
		ClientLevel clientLevel = mc.level;
		LocalPlayer player = mc.player;
		if (!enabled || clientLevel == null || player == null) {
			stopAll(mc.getSoundManager());
			return;
		}
		if (clientLevel != level) {
			stopAll(mc.getSoundManager());
			level = clientLevel;
			ctx.reset();
			scanner.reset();
			ctx.sampleBiomes(clientLevel, player);
		}
		tickCount++;

		if (tickCount % 10 == 0) {
			ctx.sampleBiomes(clientLevel, player);
		}
		ctx.update(clientLevel, player);

		scanner.tick(clientLevel, player, tickCount);
		ctx.leafCount = scanner.leafCount;
		ctx.fallCount = scanner.fallCount;
		ctx.nearestFall = scanner.nearestFall;
		if (tickCount % 20 == 7) {
			ctx.beeCount = clientLevel.getEntitiesOfClass(Bee.class, player.getBoundingBox().inflate(14.0)).size();
		}

		computeTargets();
		apply(mc.getSoundManager());
	}

	private void computeTargets() {
		for (int i = 0; i < layers.size(); i++) {
			targets[i] = layers.get(i).target(ctx);
		}
		int streamed = 0;
		boolean[] taken = new boolean[layers.size()];
		while (true) {
			int best = -1;
			float bestScore = START_THRESHOLD;
			for (int i = 0; i < layers.size(); i++) {
				if (taken[i] || !layers.get(i).streamed || targets[i] < START_THRESHOLD) {
					continue;
				}
				float score = targets[i] + (instances[i] != null ? INCUMBENT_BONUS : 0.0F);
				if (score > bestScore) {
					bestScore = score;
					best = i;
				}
			}
			if (best < 0) {
				break;
			}
			taken[best] = true;
			if (++streamed > MAX_STREAMED) {
				targets[best] = 0.0F;
			}
		}
	}

	private void apply(SoundManager sounds) {
		for (int i = 0; i < layers.size(); i++) {
			AmbientLayer layer = layers.get(i);
			AmbientLoopInstance instance = instances[i];
			float target = targets[i];

			if (instance != null && (instance.isStopped()
					|| (tickCount - startedAt[i] > 10 && !sounds.isActive(instance)))) {
				instance = null;
				instances[i] = null;
			}

			if (instance == null) {
				if (target >= START_THRESHOLD && tickCount >= retryAt[i]) {
					instance = new AmbientLoopInstance(layer.sound, layer.positional);
					instance.setTarget(target);
					if (layer.positional) {
						placeAtWaterfall(instance);
					}
					if (sounds.play(instance) == SoundEngine.PlayResult.NOT_STARTED) {
						retryAt[i] = tickCount + RETRY_DELAY_TICKS;
					} else {
						instances[i] = instance;
						startedAt[i] = tickCount;
					}
				}
				continue;
			}

			instance.setTarget(target);
			if (layer.positional) {
				placeAtWaterfall(instance);
			}
			if (target <= 0.0F && instance.current() <= STOP_THRESHOLD) {
				instance.kill();
				sounds.stop(instance);
				instances[i] = null;
			}
		}
	}

	private void placeAtWaterfall(AmbientLoopInstance instance) {
		if (ctx.nearestFall != null) {
			instance.moveTo(ctx.nearestFall.x, ctx.nearestFall.y, ctx.nearestFall.z);
		}
	}

	private void stopAll(SoundManager sounds) {
		for (int i = 0; i < instances.length; i++) {
			AmbientLoopInstance instance = instances[i];
			if (instance != null) {
				instance.kill();
				sounds.stop(instance);
				instances[i] = null;
			}
		}
		level = null;
	}
}
