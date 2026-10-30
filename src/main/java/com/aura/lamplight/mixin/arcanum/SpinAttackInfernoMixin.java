package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class SpinAttackInfernoMixin {

    @Shadow protected ItemStack autoSpinAttackItemStack;

    @WrapOperation(
        method = "checkAutoSpinAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;doAutoAttackOnTouch(Lnet/minecraft/world/entity/LivingEntity;)V"
        )
    )
    private void arcanum$infernoIgnitesOnTouch(LivingEntity self, LivingEntity victim, Operation<Void> op) {
        op.call(self, victim);
        if (self.level().isClientSide() || !self.isAutoSpinAttack()) {
            return;
        }
        if (FreezeHandler.getLevel(this.autoSpinAttackItemStack, ArcanumEnchantments.INFERNO) > 0) {
            victim.igniteForSeconds(5.0F);
        }
    }
}
