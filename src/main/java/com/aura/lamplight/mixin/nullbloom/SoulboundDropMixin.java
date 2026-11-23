package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class SoulboundDropMixin {
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventDrop(ItemStack stack, boolean dropAround, CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
		if (stack != null && stack.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "handleCreativeModeItemDrop", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventCreativeDrop(ItemStack stack, CallbackInfo ci) {
		if (stack != null && stack.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			ci.cancel();
		}
	}
}
