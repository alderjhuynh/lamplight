package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Level.class, ServerLevel.class})
public class BuildLockLevelMixin {
	@Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
	private void nullbloom$lockInteraction(Entity entity, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (NullbloomDimension.isBuildLocked((Level) (Object) this) || GodAudience.locksBuilding(entity)) {
			cir.setReturnValue(false);
		}
	}
}
