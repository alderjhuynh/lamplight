package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DaggerItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDaggerMixin {

	@Inject(method = "canCriticalAttack", at = @At("HEAD"), cancellable = true)
	private void armory$daggerSprintCrit(Entity target, CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (DaggerItem.isDagger(self.getWeaponItem())) {
			cir.setReturnValue(DaggerItem.canSprintCrit(self, target));
		}
	}

	@ModifyVariable(method = "causeExtraKnockback", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float armory$daggerNoExtraKnockback(final float knockbackAmount, final Entity entity,
	                                            final float knockbackAmountArg, final Vec3 oldMovement,
	                                            final DamageSource damageSource, final float damage,
	                                            final boolean comesFromEffect) {
		return DaggerItem.isDaggerAttack(damageSource) ? 0.0F : knockbackAmount;
	}
}
