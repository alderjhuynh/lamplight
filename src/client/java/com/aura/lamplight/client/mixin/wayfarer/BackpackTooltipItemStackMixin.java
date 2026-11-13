package com.aura.lamplight.client.mixin.wayfarer;

import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewContext;
import com.aura.lamplight.wayfarer.client.backpacktooltip.PreviewProvider;
import com.aura.lamplight.wayfarer.client.backpacktooltip.BackpackTooltipApi;
import com.aura.lamplight.wayfarer.client.backpacktooltip.tooltip.PreviewTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ItemStack.class)
public class BackpackTooltipItemStackMixin {
	@Inject(at = @At("HEAD"), method = "getTooltipImage()Ljava/util/Optional;", cancellable = true)
	private void lamplight$onGetTooltipImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
		Player owner = Minecraft.getInstance().player;
		PreviewContext context = PreviewContext.builder((ItemStack) (Object) this).withOwner(owner).build();

		PreviewProvider provider = BackpackTooltipApi.getProviderIfPreviewAvailable(context);
		if (provider != null)
			cir.setReturnValue(Optional.of(new PreviewTooltipComponent(provider, context)));
	}

	@Inject(at = @At("RETURN"), method =
			"getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;"
			+ "Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;")
	private void lamplight$onGetTooltipLines(Item.TooltipContext context, Player player, TooltipFlag type,
			CallbackInfoReturnable<List<Component>> cir) {
		var tooltip = cir.getReturnValue();
		BackpackTooltipApi.modifyStackTooltip((ItemStack) (Object) this, tooltip::addAll);
	}
}
