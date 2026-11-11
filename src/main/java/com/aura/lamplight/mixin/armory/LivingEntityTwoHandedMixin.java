package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.TwoHanded;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// while a two handed item is wielded, the offhand does not exist:
// every hand read resolves to empty, so nothing can be used, triggered
// (totem, shield, ammo) or rendered from it, on either side.
@Mixin(LivingEntity.class)
public abstract class LivingEntityTwoHandedMixin {
	@Inject(method = "getOffhandItem", at = @At("HEAD"), cancellable = true)
	private void armory$twoHandedSuppressOffhand(CallbackInfoReturnable<ItemStack> cir) {
		if ((Object) this instanceof Player player && TwoHanded.isOffhandSuppressed(player)) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}

	@Inject(method = "getItemInHand", at = @At("HEAD"), cancellable = true)
	private void armory$twoHandedSuppressOffhandHand(InteractionHand hand, CallbackInfoReturnable<ItemStack> cir) {
		if (hand != InteractionHand.OFF_HAND) {
			return;
		}
		if ((Object) this instanceof Player player && TwoHanded.isOffhandSuppressed(player)) {
			cir.setReturnValue(ItemStack.EMPTY);
		}
	}
}
