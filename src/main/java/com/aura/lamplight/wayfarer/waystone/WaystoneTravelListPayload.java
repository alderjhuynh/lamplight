package com.aura.lamplight.wayfarer.waystone;

import com.aura.lamplight.wayfarer.Wayfarer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record WaystoneTravelListPayload(BlockPos source, List<Entry> stones)
		implements CustomPacketPayload {

	public record Entry(ResourceKey<Level> dimension, BlockPos pos, String name) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Entry::dimension),
						BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
						Codec.STRING.optionalFieldOf("name", "").forGetter(Entry::name)
				).apply(instance, Entry::new));

		public boolean named() {
			return !this.name.isEmpty();
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				ResourceKey.streamCodec(Registries.DIMENSION), Entry::dimension,
				BlockPos.STREAM_CODEC, Entry::pos,
				ByteBufCodecs.stringUtf8(48), Entry::name,
				Entry::new);
	}

	public static final Type<WaystoneTravelListPayload> TYPE =
			new Type<>(Wayfarer.id("waystone_travel_list"));

	private static final StreamCodec<RegistryFriendlyByteBuf, List<Entry>> STONES_CODEC =
			ByteBufCodecs.collection(java.util.ArrayList::new, Entry.STREAM_CODEC);

	public static final StreamCodec<RegistryFriendlyByteBuf, WaystoneTravelListPayload> CODEC =
			StreamCodec.composite(
					BlockPos.STREAM_CODEC, WaystoneTravelListPayload::source,
					STONES_CODEC, WaystoneTravelListPayload::stones,
					WaystoneTravelListPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
	}
}
