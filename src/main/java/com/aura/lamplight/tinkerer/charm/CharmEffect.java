package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface CharmEffect {

	Codec<CharmEffect> CODEC = CharmEffectTypes.CODEC;

	MapCodec<? extends CharmEffect> codec();
}
