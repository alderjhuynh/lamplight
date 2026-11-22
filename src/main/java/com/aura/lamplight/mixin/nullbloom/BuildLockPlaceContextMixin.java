package com.aura.lamplight.mixin.nullbloom;

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
		if (NullbloomDimension.isBuildLocked(((BlockPlaceContext) (Object) this).getLevel())) {
			cir.setReturnValue(false);
		}
	}
}
