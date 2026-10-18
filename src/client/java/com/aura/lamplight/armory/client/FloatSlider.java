package com.aura.lamplight.armory.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class FloatSlider extends AbstractSliderButton {
	public interface FloatSetter {
		void set(float value);
	}

	private final String name;
	private final float min;
	private final float max;
	private final String format;
	private final FloatSetter setter;

	public FloatSlider(int x, int y, int width, String name, float min, float max, float current, String format, FloatSetter setter) {
		super(x, y, width, DEFAULT_HEIGHT, Component.literal(""), toNormalized(current, min, max));
		this.name = name;
		this.min = min;
		this.max = max;
		this.format = format;
		this.setter = setter;
		updateMessage();
	}

	private static double toNormalized(float value, float min, float max) {
		if (!(max > min)) {
			return 0.0;
		}
		double t = (value - min) / (max - min);
		return Math.min(1.0, Math.max(0.0, t));
	}

	private float denormalized() {
		return (float) (min + value * (max - min));
	}

	@Override
	protected void updateMessage() {
		if (name == null) {
			setMessage(Component.literal(""));
			return;
		}
		setMessage(Component.literal(name + ": " + String.format(Locale.ROOT, format, denormalized())));
	}

	@Override
	protected void applyValue() {
		setter.set(denormalized());
	}
}
