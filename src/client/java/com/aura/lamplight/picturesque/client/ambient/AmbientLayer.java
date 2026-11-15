package com.aura.lamplight.picturesque.client.ambient;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class AmbientLayer {
	enum Gate {
		SURFACE,
		CAVE,
		NONE
	}

	enum Time {
		ANY,
		DAY,
		NIGHT,
		NIGHT_OR_RAIN
	}

	@FunctionalInterface
	interface Extra {
		float volume(AmbientContext ctx);
	}

	private record Entry(TagKey<Biome> tag, float volume, Gate gate) {}

	final String name;
	final SoundEvent sound;
	final boolean streamed;
	boolean positional;

	private final List<Entry> entries = new ArrayList<>();
	private Time time = Time.ANY;
	private float rainEffect;
	private @Nullable Extra extra;

	AmbientLayer(String name, SoundEvent sound, boolean streamed) {
		this.name = name;
		this.sound = sound;
		this.streamed = streamed;
	}

	AmbientLayer biome(TagKey<Biome> tag, float volume, Gate gate) {
		entries.add(new Entry(tag, volume, gate));
		return this;
	}

	AmbientLayer time(Time time) {
		this.time = time;
		return this;
	}

	AmbientLayer rain(float effect) {
		this.rainEffect = effect;
		return this;
	}

	AmbientLayer extra(Extra extra) {
		this.extra = extra;
		return this;
	}

	AmbientLayer positional() {
		this.positional = true;
		return this;
	}

	float target(AmbientContext c) {
		float base = 0.0F;
		for (Entry e : entries) {
			float share = c.biomeShare(e.tag());
			if (share > 0.0F) {
				base = Math.max(base, share * e.volume() * c.gate(e.gate()));
			}
		}
		if (base > 0.0F) {
			float timeMult = switch (time) {
				case ANY -> 1.0F;
				case DAY -> c.day;
				case NIGHT -> c.night;
				case NIGHT_OR_RAIN -> Math.max(c.night, c.rain * 0.8F);
			};
			float rainMult = Mth.clamp(1.0F + rainEffect * c.rain, 0.0F, 1.25F);
			base *= timeMult * rainMult;
		}
		float v = extra != null ? Math.max(base, extra.volume(c)) : base;
		return Mth.clamp(v, 0.0F, 1.0F);
	}
}
