package com.aura.lamplight.client.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomBlocks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class SoulboundClientDropMixin {
	@Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventDrop(boolean dropAll, CallbackInfoReturnable<Boolean> cir) {
		LocalPlayer self = (LocalPlayer) (Object) this;
		ItemStack selected = self.getInventory().getSelectedItem();
		if (selected != null && selected.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "handleCreativeModeItemDrop", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventCreativeDrop(ItemStack stack, CallbackInfo ci) {
		if (stack != null && stack.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			ci.cancel();
		}
	}
}
