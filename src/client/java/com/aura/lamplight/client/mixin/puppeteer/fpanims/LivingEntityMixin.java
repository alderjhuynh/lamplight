package com.aura.lamplight.client.mixin.puppeteer.fpanims;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;
import com.aura.lamplight.puppeteer.fpanims.FpAnimsClient;
import com.aura.lamplight.puppeteer.fpanims.FpAnimsConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapOperation(
        method = "getCurrentSwingDuration",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/component/SwingAnimation;duration()I"
        )
    )
    private int fpanims$modifySwingDuration(SwingAnimation animation, Operation<Integer> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        boolean isLocalPlayer = minecraft != null && minecraft.player == self;
        if (isLocalPlayer && FpAnimsClient.CUSTOM_KEY instanceof KeyMapping toggleKey && keyIsDown(toggleKey)) {
            return Math.max(1, Math.round(original.call(animation) * FpAnimsConfig.SWING_SPEED / 6.0f));
        }
        return original.call(animation);
    }

    private boolean keyIsDown(KeyMapping keyMapping) {
        InputConstants.Key key = ((KeyMappingAccessor) keyMapping).fpanims$key();
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getValue());
    }
}
