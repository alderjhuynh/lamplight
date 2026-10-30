package com.aura.lamplight.mixin.armory;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Weapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityTridentShieldDisableMixin {

    @Inject(
        method = "applyItemBlocking",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/item/component/BlocksAttacks;resolveBlockedDamage(Lnet/minecraft/world/damagesource/DamageSource;FD)F"
        )
    )
    private void armory$tridentsDisableShields(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Float> cir, @Local(index = 9) float damageBlocked) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (damageBlocked <= 0.0F || !(source.getDirectEntity() instanceof ThrownTrident)) {
            return;
        }
        ItemStack shield = self.getItemBlockingWith();
        if (shield == null || shield.isEmpty()) {
            return;
        }
        BlocksAttacks blocksAttacks = shield.get(DataComponents.BLOCKS_ATTACKS);
        if (blocksAttacks == null) {
            return;
        }
        blocksAttacks.disable(level, self, Weapon.AXE_DISABLES_BLOCKING_FOR_SECONDS, shield);
    }
}
