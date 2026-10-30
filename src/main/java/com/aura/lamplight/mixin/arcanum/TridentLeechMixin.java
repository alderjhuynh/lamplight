package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import com.aura.lamplight.arcanum.enchantment.LeechHandler;
import com.aura.lamplight.arcanum.enchantment.ReelingHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.EntityHitResult;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public class TridentLeechMixin {

    @WrapOperation(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        )
    )
    private boolean arcanum$reelingReplacesHit(Entity victim, DamageSource source, float amount, Operation<Boolean> original) {
        ThrownTrident self = (ThrownTrident) (Object) this;
        int level = FreezeHandler.getLevel(self.getWeaponItem(), ArcanumEnchantments.REELING);
        if (level > 0 && victim instanceof LivingEntity living) {
            return ReelingHandler.landReelingHit(self, living, self.getOwner(), level);
        }
        return original.call(victim, source, amount);
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void arcanum$startLeechLatch(EntityHitResult hitResult, CallbackInfo ci) {
        ThrownTrident self = (ThrownTrident) (Object) this;
        if (!(hitResult.getEntity() instanceof LivingEntity victim)) return;
        int level = FreezeHandler.getLevel(self.getWeaponItem(), ArcanumEnchantments.LEECH);
        if (level <= 0) return;
        LeechHandler.startLatch(self, victim, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void arcanum$tickLeechLatch(CallbackInfo ci) {
        LeechHandler.tickLatch((ThrownTrident) (Object) this);
    }

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void arcanum$blockPickupWhileLatched(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (LeechHandler.isLatched((ThrownTrident) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
