package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DaggerItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDaggerMixin {

	@WrapOperation(
			method = "dealDefaultKnockback",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDDLnet/minecraft/world/damagesource/DamageSource;F)V"
			)
	)
	private void armory$daggersDealNoKnockback(final LivingEntity instance, final double power, final double xd,
	                                           final double zd, final DamageSource source, final float damage,
	                                           final Operation<Void> original) {
		if (DaggerItem.isDaggerAttack(source)) {
			return;
		}
		original.call(instance, power, xd, zd, source, damage);
	}

	@ModifyVariable(method = "causeExtraKnockback", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float armory$daggerNoExtraKnockback(final float knockback, final Entity target,
	                                            final float knockbackArg, final Vec3 oldMovement,
	                                            final DamageSource damageSource, final float damage,
	                                            final boolean comesFromEffect) {
		return DaggerItem.isDaggerAttack(damageSource) ? 0.0F : knockback;
	}
}
