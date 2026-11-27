package com.aura.lamplight.folio.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class SchoolEffect extends MobEffect {

    public SchoolEffect(final MobEffectCategory category, final int color) {
        super(category, color);
    }
    @Override
    public void addAttributeModifiers(final AttributeMap attributes, final int amplifier) {
        super.addAttributeModifiers(attributes, Math.min(amplifier, StackRules.MAX_STACKS - 1));
    }
}
