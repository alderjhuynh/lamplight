package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PanicGoal.class)
public class SoothingPresenceMixin {

    @Shadow
    protected PathfinderMob mob;

    @Inject(method = "shouldPanic", at = @At("HEAD"), cancellable = true)
    private void lamplight$soothingPresence(CallbackInfoReturnable<Boolean> cir) {
        if (!(this.mob instanceof AgeableMob)) return;
        for (Player player : this.mob.level().getEntitiesOfClass(Player.class,
                this.mob.getBoundingBox().inflate(8.0D))) {
            if (player.isAlive() && !player.isSpectator()
                    && RebuildHandler.armorLvl(player, ArcanumEnchantments.SOOTHING_PRESENCE) > 0) {
                cir.cancel();
                return;
            }
        }
    }
}
