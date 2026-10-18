package com.aura.lamplight.panacea.client;

import com.aura.lamplight.panacea.blockentity.PotionCauldronBlockEntity;
import com.aura.lamplight.panacea.item.TippedWeapon;
import com.aura.lamplight.panacea.registry.ModBlocks;
import com.aura.lamplight.panacea.registry.ModComponents;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.state.BlockState;

public class PanaceaClient implements ClientModInitializer {
	private static final BlockTintSource POTION_CAULDRON_TINT = new BlockTintSource() {
		@Override
		public int color(BlockState state) {
			return -1;
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron && !cauldron.isEmpty()) {
				return 0xFF000000 | cauldron.getBlendedColor();
			}
			return -1;
		}
	};

	@Override
	public void onInitializeClient() {
		BlockColorRegistry.register(List.of(POTION_CAULDRON_TINT), ModBlocks.POTION_CAULDRON);

		CreativeModeTabEvents.modifyOutputEvent(
				ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("functional_blocks"))
		).register(entries -> entries.accept(ModBlocks.POTION_CAULDRON));

		ItemTooltipCallback.EVENT.register(PanaceaClient::appendTippedWeaponTooltip);
	}

	private static void appendTippedWeaponTooltip(
			ItemStack stack,
			Item.TooltipContext tooltipContext,
			TooltipFlag tooltipFlag,
			List<Component> lines
	) {
		if (!(stack.get(ModComponents.TIPPED_WEAPON) instanceof TippedWeapon tipped)) {
			return;
		}

		lines.add(Component.empty());
		for (MobEffectInstance effect : tipped.potion().getAllEffects()) {
			lines.add(Component.literal("")
					.append(PotionContents.getPotionDescription(effect.getEffect(), effect.getAmplifier()))
					.withStyle(ChatFormatting.BLUE)
			);
		}
		int charges = tipped.charges();
		lines.add(Component.literal(charges + (charges == 1 ? " Charge" : " Charges")).withStyle(ChatFormatting.AQUA));
	}
}
