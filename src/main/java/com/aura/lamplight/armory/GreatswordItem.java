package com.aura.lamplight.armory;

import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.BlocksAttacks;

public class GreatswordItem extends Item {
	public GreatswordItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
		super(properties.sword(material, attackDamageBaseline, attackSpeedBaseline));
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.NONE;
	}

	public static void addBlockingComponent(DefaultItemComponentEvents.ModifyContext context, Item greatsword) {
		context.modify(greatsword, (builder, lookupProvider, item) -> builder.set(DataComponents.BLOCKS_ATTACKS,
				new BlocksAttacks(
						0.25F,
						1.0F,
						List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 0.5F)),
						new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
						Optional.of(lookupProvider.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
						Optional.of(SoundEvents.SHIELD_BLOCK),
						Optional.of(SoundEvents.SHIELD_BREAK))));
	}
}
