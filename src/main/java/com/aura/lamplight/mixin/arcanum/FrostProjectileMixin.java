package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public class FrostProjectileMixin {

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void arcanum$applyFrost(EntityHitResult hitResult, CallbackInfo ci) {
        Entity hit = hitResult.getEntity();
        if (!(hit instanceof LivingEntity victim)) return;

        AbstractArrow self = (AbstractArrow) (Object) this;
        if (self.level().isClientSide()) return;

        ItemStack weapon = self.getWeaponItem();
        int level = 0;
        if (weapon != null && !weapon.isEmpty()) {
            level = FreezeHandler.getLevel(weapon, ArcanumEnchantments.FROST);
        }
        if (level <= 0) {
            Entity owner = self.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                ItemStack main = livingOwner.getMainHandItem();
                level = FreezeHandler.getLevel(main, ArcanumEnchantments.FROST);
                if (level <= 0) {
                    ItemStack off = livingOwner.getOffhandItem();
                    level = FreezeHandler.getLevel(off, ArcanumEnchantments.FROST);
                }
            }
        }
        if (level <= 0) return;

        FreezeHandler.applyFreeze(victim, level);
    }
}
