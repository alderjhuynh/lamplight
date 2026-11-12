package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DualWielded;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDualWieldDropMixin {
	@Inject(
		method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void armory$neverDropGhost(ItemStack stack, boolean dropAround, boolean includeThrowerName,
			CallbackInfoReturnable<ItemEntity> cir) {
		if ((Object) this instanceof Player && DualWielded.isGhost(stack)) {
			cir.setReturnValue(null);
		}
	}
}
