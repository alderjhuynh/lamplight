package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class BuildLockItemStackMixin {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void nullbloom$lockItemUseOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (NullbloomDimension.isBuildLocked(context.getLevel()) || GodAudience.locksBuilding(context.getPlayer())) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
