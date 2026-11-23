package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SoulboundSlotMixin {
	@Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
	private void lamplight$preventSoulboundInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack != null && stack.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
			Slot self = (Slot) (Object) this;
			if (!(self.container instanceof Inventory)) {
				cir.setReturnValue(false);
			}
		}
	}
}
