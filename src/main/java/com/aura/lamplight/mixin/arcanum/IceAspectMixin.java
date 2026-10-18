package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class IceAspectMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void arcanum$applyIceAspect(ServerLevel level, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!source.isDirect()) return;

        LivingEntity victim = (LivingEntity) (Object) this;
        if (victim.level().isClientSide()) return;

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return;
        if (attacker == victim) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;

        int lvl = FreezeHandler.getLevel(weapon, ArcanumEnchantments.ICE_ASPECT);
        if (lvl <= 0) return;

        FreezeHandler.applyFreeze(victim, lvl);
    }
}
