package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPlaceContext.class)
public class BuildLockPlaceContextMixin {
	@Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
	private void nullbloom$lockPlacing(CallbackInfoReturnable<Boolean> cir) {
		BlockPlaceContext self = (BlockPlaceContext) (Object) this;
		if (NullbloomDimension.isBuildLocked(self.getLevel()) || GodAudience.locksBuilding(self.getPlayer())) {
			cir.setReturnValue(false);
		}
	}
}
