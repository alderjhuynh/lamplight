package com.aura.lamplight.picturesque.client.ambient;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

final class AmbientLoopInstance extends AbstractTickableSoundInstance {
	private static final float RISE_PER_TICK = 1.0F / 80.0F;
	private static final float FALL_PER_TICK = 1.0F / 60.0F;

	private float target;

	AmbientLoopInstance(SoundEvent event, boolean positional) {
		super(event, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
		this.looping = true;
		this.delay = 0;
		this.volume = 0.0F;
		this.relative = !positional;
		this.attenuation = positional ? SoundInstance.Attenuation.LINEAR : SoundInstance.Attenuation.NONE;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	void setTarget(float target) {
		this.target = target;
	}

	float current() {
		return this.volume;
	}

	void moveTo(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	void kill() {
		this.stop();
	}

	@Override
	public void tick() {
		if (this.volume < this.target) {
			this.volume = Math.min(this.target, this.volume + RISE_PER_TICK);
		} else if (this.volume > this.target) {
			this.volume = Math.max(this.target, this.volume - FALL_PER_TICK);
		}
	}
}
