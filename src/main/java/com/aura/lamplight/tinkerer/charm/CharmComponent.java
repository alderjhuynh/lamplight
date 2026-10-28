package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

public record CharmComponent(Identifier templateId, Identifier settingItem, int settingCount, List<Inset> insets) {

	public record Inset(Identifier item, int count) {
		public static final Codec<Inset> CODEC = RecordCodecBuilder.create(
			i -> i.group(
				Identifier.CODEC.fieldOf("item").forGetter(Inset::item),
				Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(Inset::count)
			).apply(i, Inset::new)
		);

		public static final StreamCodec<ByteBuf, Inset> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, Inset::item,
			ByteBufCodecs.VAR_INT, Inset::count,
			Inset::new
		);
	}

	public static final Codec<CharmComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Identifier.CODEC.fieldOf("template").forGetter(CharmComponent::templateId),
			Identifier.CODEC.fieldOf("setting").forGetter(CharmComponent::settingItem),
			Codec.intRange(1, 99).optionalFieldOf("setting_count", 1).forGetter(CharmComponent::settingCount),
			Inset.CODEC.listOf().optionalFieldOf("insets", List.of()).forGetter(CharmComponent::insets)
		).apply(instance, CharmComponent::new)
	);

	public static final StreamCodec<ByteBuf, java.util.List<Inset>> INSETS_STREAM_CODEC =
			ByteBufCodecs.collection(java.util.ArrayList::new, Inset.STREAM_CODEC);

	public static final StreamCodec<ByteBuf, CharmComponent> STREAM_CODEC = StreamCodec.composite(
		Identifier.STREAM_CODEC, CharmComponent::templateId,
		Identifier.STREAM_CODEC, CharmComponent::settingItem,
		ByteBufCodecs.VAR_INT, CharmComponent::settingCount,
		INSETS_STREAM_CODEC, CharmComponent::insets,
		CharmComponent::new
	);

	public static ResolvedCharm resolved(net.minecraft.world.item.ItemStack stack) {
		return ResolvedCharm.of(stack);
	}
}
