package com.aura.lamplight.nullbloom.client;

import com.aura.lamplight.nullbloom.NullbloomDimension;
import com.aura.lamplight.nullbloom.NullbloomSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.Random;

public final class NullbloomClient {
	private static final Random RANDOM = new Random();

	private static SoundInstance loopInstance;
	private static SoundInstance whisperInstance;
	private static int quakeTicks = 600 + RANDOM.nextInt(601);

	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	private void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			stopAll(client);
			return;
		}
		if (!NullbloomDimension.is(client.level)) {
			stopAll(client);
			return;
		}

		if (loopInstance == null || client.getSoundManager().isActive(loopInstance) == false) {
			loopInstance = new SimpleSoundInstance(
				NullbloomSounds.NULLBLOOM_LOOP.location(), SoundSource.AMBIENT, 0.55F, 1.0F,
				net.minecraft.util.RandomSource.create(), true, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
			);
			client.getSoundManager().play(loopInstance);
		}

		if (quakeTicks <= 0) {
			playOneShot(client, NullbloomSounds.ABYSS_QUAKE, 0.75F);
			quakeTicks = 600 + RANDOM.nextInt(601);
		} else {
			quakeTicks--;
		}

	}

	public static void startWhisper() {
		com.aura.lamplight.nullbloom.client.god.GodSequence.onSpeak();
		stopWhisper();
		Minecraft mc = Minecraft.getInstance();
		if (mc == null) return;
		whisperInstance = new SimpleSoundInstance(
			NullbloomSounds.GOD_WHISPER.location(), SoundSource.AMBIENT, 0.35F,
			1.0F + (RANDOM.nextFloat() - 0.5F) * 0.1F,
			net.minecraft.util.RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
		);
		mc.getSoundManager().play(whisperInstance);
	}

	public static void stopWhisper() {
		Minecraft mc = Minecraft.getInstance();
		if (mc != null && whisperInstance != null) {
			mc.getSoundManager().stop(whisperInstance);
		}
		whisperInstance = null;
	}

	private void playOneShot(Minecraft client, SoundEvent event, float volume) {
		SimpleSoundInstance inst = SimpleSoundInstance.forLocalAmbience(event, 1.0F, volume);
		client.getSoundManager().play(inst);
	}

	private void stopAll(Minecraft client) {
		if (loopInstance != null) {
			client.getSoundManager().stop(loopInstance);
			loopInstance = null;
		}
		if (whisperInstance != null) {
			client.getSoundManager().stop(whisperInstance);
			whisperInstance = null;
		}
		quakeTicks = 600 + RANDOM.nextInt(601);
	}
}
