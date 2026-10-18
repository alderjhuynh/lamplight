package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class IceShieldMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void arcanum$applyIceShield(ServerLevel level, DamageSource source, float amount,
                                        CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        LivingEntity victim = (LivingEntity) (Object) this;
        if (victim.level().isClientSide()) return;

        Entity attackerEntity = source.getEntity();
        if (!(attackerEntity instanceof LivingEntity attacker)) return;
        if (attacker == victim) return;

        ItemStack chest = victim.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty()) return;

        int lvl = FreezeHandler.getLevel(chest, ArcanumEnchantments.ICE_SHIELD);
        if (lvl <= 0) return;

        FreezeHandler.applyFreeze(attacker, lvl);
    }
}
