package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
	@Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
	private void nullbloom$redirectToNullbloom(
		ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos, CallbackInfoReturnable<@Nullable TeleportTransition> cir
	) {
		if (currentLevel.dimension() == Level.END) {
			return;
		}
		if (NullbloomDimension.is(currentLevel)) {
			cir.setReturnValue(null);
			return;
		}
		TeleportTransition destination = NullbloomDimension.portalDestination(currentLevel, entity);
		if (destination != null) {
			cir.setReturnValue(destination);
		}
	}
}
