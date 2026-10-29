package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.tinkerer.Tinkerer;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public final class CharmAttachments {
	private CharmAttachments() {}

	private static final Codec<Map<String, ItemStack>> WORN_CODEC =
			Codec.unboundedMap(Codec.STRING, ItemStack.OPTIONAL_CODEC);

	private static final StreamCodec<RegistryFriendlyByteBuf, Map<String, ItemStack>> WORN_STREAM_CODEC =
			ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ItemStack.OPTIONAL_STREAM_CODEC);

	public static final AttachmentType<Map<String, ItemStack>> WORN_CHARMS = AttachmentRegistry
			.<Map<String, ItemStack>>builder()
			.persistent(WORN_CODEC)
			.copyOnDeath()
			.syncWith(WORN_STREAM_CODEC, AttachmentSyncPredicate.all())
			.initializer(HashMap::new)
			.buildAndRegister(Tinkerer.id("worn_charms"));

	private static final Codec<Map<String, Boolean>> UNLOCKED_CODEC =
			Codec.unboundedMap(Codec.STRING, Codec.BOOL);

	public static final AttachmentType<Map<String, Boolean>> UNLOCKED_SLOT_TYPES = AttachmentRegistry
			.<Map<String, Boolean>>builder()
			.persistent(UNLOCKED_CODEC)
			.copyOnDeath()
			.syncWith(ByteBufCodecs.fromCodec(UNLOCKED_CODEC), AttachmentSyncPredicate.all())
			.initializer(HashMap::new)
			.buildAndRegister(Tinkerer.id("unlocked_charm_slots"));

	public static Map<String, ItemStack> wornCharms(final Player player) {
		Map<String, ItemStack> worn = player.getAttached(WORN_CHARMS);
		return worn == null ? Map.of() : worn;
	}

	public static ItemStack charmIn(final Player player, final Identifier slotId) {
		return wornCharms(player).getOrDefault(slotId.toString(), ItemStack.EMPTY);
	}

	public static void setCharm(final Player player, final Identifier slotId, final ItemStack stack) {
		Map<String, ItemStack> worn = new HashMap<>(wornCharms(player));
		if (stack == null || stack.isEmpty()) {
			worn.remove(slotId.toString());
		} else {
			worn.put(slotId.toString(), stack);
		}
		player.setAttached(WORN_CHARMS, worn);
	}

	public static boolean isUnlocked(final Player player, final CharmSlotTypes.CharmSlotType slot) {
		Map<String, Boolean> explicit = player.getAttached(UNLOCKED_SLOT_TYPES);
		if (explicit != null) {
			Boolean state = explicit.get(slot.id().toString());
			if (state != null) {
				return state;
			}
		}
		int rank = CharmSlotTypes.rankOf(slot);
		return rank > 0 && rank <= CharmSlotAttributes.acquiredValue(player, slot.def().slotType());
	}

	public static void setUnlocked(final Player player, final Identifier slotId, final boolean unlocked) {
		Map<String, Boolean> state = new HashMap<>();
		Map<String, Boolean> current = player.getAttached(UNLOCKED_SLOT_TYPES);
		if (current != null) {
			state.putAll(current);
		}
		state.put(slotId.toString(), unlocked);
		player.setAttached(UNLOCKED_SLOT_TYPES, state);
	}

	public static List<CharmAggregate.Entry> wornEntries(final Player player) {
		List<CharmAggregate.Entry> entries = new java.util.ArrayList<>();
		for (Map.Entry<String, ItemStack> entry : wornCharms(player).entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}
			Identifier wellId = Identifier.tryParse(entry.getKey());
			if (wellId != null) {
				entries.add(new CharmAggregate.Entry(wellId, entry.getValue()));
			}
		}
		return List.copyOf(entries);
	}
}
