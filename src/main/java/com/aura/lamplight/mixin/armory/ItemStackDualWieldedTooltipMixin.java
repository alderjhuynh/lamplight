package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DualWielded;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackDualWieldedTooltipMixin {
	@Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
	private void armory$appendDualWieldedLine(
			Item.TooltipContext context,
			net.minecraft.world.item.component.TooltipDisplay display,
			@Nullable Player player,
			TooltipFlag flag,
			Consumer<Component> builder,
			CallbackInfo ci
	) {
		ItemStack stack = (ItemStack) (Object) this;
		if (DualWielded.isDualWielded(stack) && !DualWielded.isGhost(stack)) {
			builder.accept(Component.translatable("tooltip.lamplight.dual_wielded")
					.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
		}
	}
}
