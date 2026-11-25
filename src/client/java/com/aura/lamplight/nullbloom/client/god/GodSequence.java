package com.aura.lamplight.nullbloom.client.god;

import com.aura.lamplight.nullbloom.GodCallPayload;
import com.aura.lamplight.nullbloom.GodCallTiming;
import com.aura.lamplight.nullbloom.NullbloomSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;

public final class GodSequence {
	private static boolean active;
	private static GodFxTimeline.Mode mode = GodFxTimeline.Mode.FULL;
	private static int ticks;
	private static int endTicks = -1;
	private static long seed;
	private static double centerX;
	private static double centerZ;
	private static float flashYaw;
	private static ClientLevel level;
	private static float speakAt = -1000.0F;

	private static SoundInstance bells;
	private static GodLoop loop;

	private static float baseR = 0.47F;
	private static float baseG = 0.65F;
	private static float baseB = 1.0F;

	private GodSequence() {
	}

	public static void init() {
		ClientPlayNetworking.registerGlobalReceiver(GodCallPayload.TYPE,
			(payload, context) -> context.client().execute(() -> handle(context.client(), payload)));
		ClientTickEvents.END_CLIENT_TICK.register(GodSequence::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset(client));
	}

	private static void handle(Minecraft client, GodCallPayload payload) {
		switch (payload.action()) {
			case GodCallPayload.START_FULL -> start(client, GodFxTimeline.Mode.FULL);
			case GodCallPayload.START_BRIEF -> start(client, GodFxTimeline.Mode.BRIEF);
			case GodCallPayload.END -> {
				if (active && endTicks < 0) {
					endTicks = 0;
					play(client, NullbloomSounds.GOD_GLITCH, 0.6F);
					if (loop != null) {
						loop.fadeOut();
					}
				}
			}
			case GodCallPayload.ABORT -> reset(client);
			default -> {
			}
		}
	}

	private static void start(Minecraft client, GodFxTimeline.Mode newMode) {
		reset(client);
		if (client.level == null || client.player == null) {
			return;
		}
		active = true;
		mode = newMode;
		ticks = 0;
		endTicks = -1;
		seed = System.nanoTime();
		level = client.level;
		centerX = client.player.getX();
		centerZ = client.player.getZ();
		flashYaw = client.player.getYRot();
		if (newMode == GodFxTimeline.Mode.FULL) {
			bells = SimpleSoundInstance.forLocalAmbience(NullbloomSounds.GOD_BELLS, 1.0F, 1.0F);
			client.getSoundManager().play(bells);
		} else {
			play(client, NullbloomSounds.GOD_GLITCH, 0.8F);
		}
	}

	public static void reset(Minecraft client) {
		if (client != null) {
			if (bells != null) {
				client.getSoundManager().stop(bells);
			}
			if (loop != null) {
				loop.fadeOut();
			}
		}
		bells = null;
		loop = null;
		active = false;
		endTicks = -1;
		ticks = 0;
		level = null;
	}

	private static void play(Minecraft client, SoundEvent event, float volume) {
		client.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(event, 1.0F, volume));
	}

	private static void tick(Minecraft client) {
		if (!active) {
			return;
		}
		if (client.level == null || client.player == null || client.level != level) {
			reset(client);
			return;
		}
		if (client.isPaused()) {
			return;
		}
		ticks++;
		if (endTicks >= 0) {
			endTicks++;
			if (endTicks > GodCallTiming.OUTRO_TICKS + 40) {
				reset(client);
			}
			return;
		}
		if (mode == GodFxTimeline.Mode.FULL) {
			if (ticks == GodFxTimeline.FLASH - GodFxTimeline.GLITCH_LEAD_TICKS) {
				play(client, NullbloomSounds.GOD_GLITCH, 0.9F);
			}
			if (ticks == GodFxTimeline.FLASH) {
				flashYaw = client.player.getYRot();
				loop = new GodLoop(NullbloomSounds.NULLBLOOM_LOOP, 0.45F, 3.0F);
				client.getSoundManager().play(loop);
			}
		}
	}

	private static float now(Minecraft client) {
		float partial = client.isPaused() ? 0.0F : client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		return ticks + partial;
	}

	private static float endNow(Minecraft client) {
		if (endTicks < 0) {
			return -1.0F;
		}
		float partial = client.isPaused() ? 0.0F : client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		return endTicks + partial;
	}

	public static boolean active() {
		return active;
	}

	public static GodFxTimeline.Fx fx() {
		if (!active) {
			return null;
		}
		Minecraft client = Minecraft.getInstance();
		float distortion = (float) Mth.clamp(client.options.screenEffectScale().get(), 0.0, 1.0);
		return GodFxTimeline.sample(mode, now(client), endNow(client), seed, distortion);
	}

	public static boolean godSkyActive() {
		if (!active || mode != GodFxTimeline.Mode.FULL) {
			return false;
		}
		return endTicks < 0 ? ticks >= GodFxTimeline.FLASH : endTicks < 18.5F;
	}

	public static boolean suppressesWorldGrade() {
		return active && (mode == GodFxTimeline.Mode.BRIEF || godSkyActive());
	}

	public static float flashYaw() {
		return flashYaw;
	}

	public static double centerX() {
		return centerX;
	}

	public static double centerZ() {
		return centerZ;
	}

	public static float seconds() {
		return now(Minecraft.getInstance()) / 20.0F;
	}

	public static float frameSeed() {
		return (float) Math.floor(seconds() * 12.0F);
	}

	public static void onSpeak() {
		if (active) {
			speakAt = now(Minecraft.getInstance());
		}
	}

	public static float speakPulse() {
		if (!active) {
			return 0.0F;
		}
		float since = now(Minecraft.getInstance()) - speakAt;
		return since < 0.0F || since > 80.0F ? 0.0F : (float) Math.exp(-since / 14.0F);
	}

	public static void applySkyOverrides(ClientLevel clientLevel, SkyRenderState state) {
		if (!active || clientLevel != level) {
			return;
		}
		double realTime = GodSkyTables.wrap(clientLevel.getDefaultClockTime());
		float realMultiplier = GodSkyTables.skyMultiplier(realTime);
		if (realMultiplier > 0.5F) {
			baseR = Math.min(1.0F, ((state.skyColor >> 16) & 0xFF) / 255.0F / realMultiplier);
			baseG = Math.min(1.0F, ((state.skyColor >> 8) & 0xFF) / 255.0F / realMultiplier);
			baseB = Math.min(1.0F, (state.skyColor & 0xFF) / 255.0F / realMultiplier);
		}
		GodFxTimeline.Fx fx = fx();
		if (fx == null || !fx.timeWarp()) {
			return;
		}
		float spin = fx.bodyCycles() * (float) (Math.PI * 2.0);
		state.sunAngle += spin;
		state.moonAngle += spin;
		state.starAngle += spin;

		double fakeTime = realTime + GodSkyTables.DAY_LENGTH * (double) fx.lightCycles();
		float multiplier = GodSkyTables.skyMultiplier(fakeTime);
		state.skyColor = 0xFF000000
			| (Math.round(baseR * multiplier * 255.0F) << 16)
			| (Math.round(baseG * multiplier * 255.0F) << 8)
			| Math.round(baseB * multiplier * 255.0F);
		state.sunriseAndSunsetColor = GodSkyTables.sunriseColor(fakeTime);
		state.starBrightness = GodSkyTables.starBrightness(fakeTime);
	}
	
	public static float worldDimming(ClientLevel clientLevel, GodFxTimeline.Fx fx) {
		if (!fx.timeWarp() || clientLevel == null) {
			return 1.0F;
		}
		double realTime = GodSkyTables.wrap(clientLevel.getDefaultClockTime());
		double fakeTime = realTime + GodSkyTables.DAY_LENGTH * (double) fx.lightCycles();
		float real = 0.28F + 0.72F * GodSkyTables.skyMultiplier(realTime);
		float fake = 0.28F + 0.72F * GodSkyTables.skyMultiplier(fakeTime);
		return Mth.clamp(fake / real, 0.3F, 2.5F);
	}
}
