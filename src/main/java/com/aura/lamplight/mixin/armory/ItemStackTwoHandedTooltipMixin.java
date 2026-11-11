package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.TwoHanded;
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

// any item carrying the two_handed component gets a gray, non-italic "Two Handed"
// line. Injected at the TAIL of addDetailsToTooltip rather than via the fabric
// ItemTooltipCallback, because arcanum's tooltip mixin re-injects getTooltipLines
// returns and shifts fabric's ordinal-1 target off the real return.
@Mixin(ItemStack.class)
public abstract class ItemStackTwoHandedTooltipMixin {
	@Inject(method = "addDetailsToTooltip", at = @At("TAIL"))
	private void armory$appendTwoHandedLine(
			Item.TooltipContext context,
			net.minecraft.world.item.component.TooltipDisplay display,
			@Nullable Player player,
			TooltipFlag flag,
			Consumer<Component> builder,
			CallbackInfo ci
	) {
		if (TwoHanded.isTwoHanded((ItemStack) (Object) this)) {
			builder.accept(Component.translatable("tooltip.lamplight.two_handed")
					.withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false)));
		}
	}
}
