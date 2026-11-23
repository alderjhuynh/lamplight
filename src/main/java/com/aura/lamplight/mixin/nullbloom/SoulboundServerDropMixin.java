package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomBlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class SoulboundServerDropMixin {
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventDrop(ItemStack stack, boolean dropAround, boolean includeSelected, CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
		if (stack != null && stack.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventSelectedDrop(boolean dropAll, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		ItemStack selected = self.getInventory().getSelectedItem();
		if (selected != null && selected.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			ci.cancel();
		}
	}
}
