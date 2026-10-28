package com.aura.lamplight.tinkerer.charm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@SuppressWarnings ("all")
public record CharmSlotDefinition(String slotType, int order,
                                  boolean unlockedByDefault, List<CostEntry> unlockCost) {

	public static final Codec<CharmSlotDefinition> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Codec.STRING.fieldOf("slot_type").forGetter(CharmSlotDefinition::slotType),
			Codec.INT.optionalFieldOf("order", 0).forGetter(CharmSlotDefinition::order),
			Codec.BOOL.optionalFieldOf("unlocked_by_default", false).forGetter(CharmSlotDefinition::unlockedByDefault),
			CostEntry.CODEC.listOf().optionalFieldOf("unlock_cost", List.of()).forGetter(CharmSlotDefinition::unlockCost)
		).apply(instance, CharmSlotDefinition::new)
	);

	public record CostEntry(String item, int count) {
		public static final Codec<CostEntry> CODEC = RecordCodecBuilder.create(
			i -> i.group(
				Codec.STRING.fieldOf("item").forGetter(CostEntry::item),
				Codec.intRange(1, 99).fieldOf("count").forGetter(CostEntry::count)
			).apply(i, CostEntry::new)
		);

		public boolean test(ItemStack stack) {
			if (stack.isEmpty()) {
				return false;
			}
			if (item.startsWith("#")) {
				String path = item.substring(1);
				Identifier tag = path.contains(":") ? Identifier.parse(path) : Identifier.withDefaultNamespace(path);
				return stack.is(TagKey.create(net.minecraft.core.registries.Registries.ITEM, tag));
			}
			return BuiltInRegistries.ITEM.getOptional(Identifier.parse(item))
					.map(stack::is).orElse(false);
		}
	}

	public boolean canAfford(Inventory inventory) {
		for (CostEntry entry : unlockCost) {
			int remaining = entry.count();
			for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
				ItemStack stack = inventory.getItem(i);
				if (!stack.isEmpty() && entry.test(stack)) {
					remaining -= stack.getCount();
				}
			}
			if (remaining > 0) {
				return false;
			}
		}
		return true;
	}

	public void pay(Inventory inventory) {
		for (CostEntry entry : unlockCost) {
			int remaining = entry.count();
			for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
				ItemStack stack = inventory.getItem(i);
				if (!stack.isEmpty() && entry.test(stack)) {
					int take = Math.min(stack.getCount(), remaining);
					stack.shrink(take);
					remaining -= take;
				}
			}
		}
	}
}
