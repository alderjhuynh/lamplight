package com.aura.lamplight.nullbloom.client.god;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@SuppressWarnings ("all")
public final class GodLoop extends AbstractTickableSoundInstance {
	private final float maxVolume;
	private float goal;
	private final float rate;

	public GodLoop(SoundEvent event, float maxVolume, float fadeSeconds) {
		super(event, SoundSource.AMBIENT, RandomSource.create());
		this.maxVolume = maxVolume;
		this.rate = maxVolume / Math.max(0.05F, fadeSeconds * 20.0F);
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.goal = maxVolume;
	}

	public void fadeOut() {
		this.goal = 0.0F;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		if (this.volume < this.goal) {
			this.volume = Math.min(this.goal, this.volume + this.rate);
		} else if (this.volume > this.goal) {
			this.volume = Math.max(this.goal, this.volume - this.rate);
		}
		if (this.goal <= 0.0F && this.volume <= 0.001F) {
			this.stop();
		}
	}
}
