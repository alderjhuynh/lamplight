package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public record CharmSyncPayload(Map<Identifier, CharmMaterial> materials,
                               Map<Identifier, CharmTemplate> templates,
                               Map<Identifier, CharmSlotDefinition> slots,
                               Map<String, Integer> capacities,
                               Map<String, Integer> acquired,
                               List<Identifier> wells) implements CustomPacketPayload {

	public static final Type<CharmSyncPayload> TYPE = new Type<>(Tinkerer.id("charm_sync"));

	public static final Codec<Map<Identifier, CharmMaterial>> MATERIALS_CODEC =
			Codec.unboundedMap(Identifier.CODEC, CharmMaterial.CODEC);
	public static final Codec<Map<Identifier, CharmTemplate>> TEMPLATES_CODEC =
			Codec.unboundedMap(Identifier.CODEC, CharmTemplate.CODEC);
	public static final Codec<Map<Identifier, CharmSlotDefinition>> SLOTS_CODEC =
			Codec.unboundedMap(Identifier.CODEC, CharmSlotDefinition.CODEC);
	public static final Codec<Map<String, Integer>> CAPACITIES_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.INT);
	public static final Codec<Map<String, Integer>> ACQUIRED_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.INT);
	public static final Codec<List<Identifier>> WELLS_CODEC = Identifier.CODEC.listOf();

	public static final StreamCodec<RegistryFriendlyByteBuf, CharmSyncPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.fromCodec(MATERIALS_CODEC), CharmSyncPayload::materials,
			ByteBufCodecs.fromCodec(TEMPLATES_CODEC), CharmSyncPayload::templates,
			ByteBufCodecs.fromCodec(SLOTS_CODEC), CharmSyncPayload::slots,
			ByteBufCodecs.fromCodec(CAPACITIES_CODEC), CharmSyncPayload::capacities,
			ByteBufCodecs.fromCodec(ACQUIRED_CODEC), CharmSyncPayload::acquired,
			ByteBufCodecs.fromCodec(WELLS_CODEC), CharmSyncPayload::wells,
			CharmSyncPayload::new
	);

	public static CharmSyncPayload snapshot(final Player player) {
		Map<String, Integer> capacities = CharmSlotAttributes.capacities(player);
		Map<String, Integer> acquired = CharmSlotAttributes.acquired(player);
		return new CharmSyncPayload(CharmMaterials.all(), CharmTemplates.all(), CharmSlotTypes.snapshots(),
				capacities, acquired,
				CharmDeckLayout.wells(player, capacities, acquired).stream()
						.map(CharmDeckLayout.Well::id)
						.toList());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
