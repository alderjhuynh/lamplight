package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PrivateEntityMixin {
	@Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
	private void nullbloom$hideFromStrangers(ServerPlayer viewer, CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if ((NullbloomDimension.is(self) && !NullbloomDimension.canPerceive(viewer, self)) || GodAudience.hides(viewer, self)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void nullbloom$noShoving(Entity other, CallbackInfo ci) {
		if (NullbloomDimension.areStrangers((Entity) (Object) this, other) || GodAudience.areStrangers((Entity) (Object) this, other)) {
			ci.cancel();
		}
	}
}
