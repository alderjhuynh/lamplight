package com.aura.lamplight.menagerie.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Warmth carried by a tamed wisp. The movement-efficiency modifier mirrors
 * Soul Speed, so the two never stack past their shared cap.
 */
public class KindledEffect extends MobEffect {
    public KindledEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
