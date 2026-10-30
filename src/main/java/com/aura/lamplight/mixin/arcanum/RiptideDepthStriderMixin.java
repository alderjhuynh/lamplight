package com.aura.lamplight.mixin.arcanum;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class RiptideDepthStriderMixin {

    @ModifyExpressionValue(
        method = "travelInWater",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getAttributeValue(Lnet/minecraft/core/Holder;)D"
        )
    )
    private double arcanum$riptideIgnoresDepthStrider(double original) {
        if (((LivingEntity) (Object) this).isAutoSpinAttack()) {
            return 0.0D;
        }
        return original;
    }
}
