package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowPropulsionMixin {

    @Inject(method = "createProjectile", at = @At("RETURN"))
    private void arcanum$tagJetRocket(Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit, CallbackInfoReturnable<Projectile> cir) {
        Projectile proj = cir.getReturnValue();
        if (proj == null) return;
        if (projectile == null || projectile.isEmpty()) return;
        if (!projectile.is(Items.FIREWORK_ROCKET)) return;
        if (weapon == null || weapon.isEmpty()) return;
        if (!ArcanumEnchantmentHelper.hasPropulsion(weapon)) return;

        proj.addTag("lamplight:jet_rocket");

    }
}
