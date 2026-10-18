package com.aura.lamplight.mixin.ambrosia;

import com.aura.lamplight.ambrosia.effect.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    @Shadow
    private int foodLevel;

    @Shadow
    public abstract void addExhaustion(float exhaustion);

    @Unique
    private int ambrosia$cachedFoodLevel;

    @Unique
    private boolean ambrosia$nourished;

    @Unique
    private int ambrosia$comfortTimer;

    @Inject(method = "tick", at = @At("HEAD"))
    private void ambrosia$captureFoodLevel(ServerPlayer player, CallbackInfo ci) {
        this.ambrosia$cachedFoodLevel = this.foodLevel;
        this.ambrosia$nourished = player.hasEffect(ModEffects.NOURISHMENT);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ambrosia$restoreFoodLevel(ServerPlayer player, CallbackInfo ci) {
        if (this.ambrosia$nourished && this.foodLevel < this.ambrosia$cachedFoodLevel) {
            this.foodLevel = this.ambrosia$cachedFoodLevel;
        }
        this.ambrosia$nourished = false;
        ambrosia$comfortRegen(player);
    }

    @Unique
    private void ambrosia$comfortRegen(ServerPlayer player) {
        if (!player.hasEffect(ModEffects.COMFORT)) {
            this.ambrosia$comfortTimer = 0;
            return;
        }
        if (this.foodLevel >= 18) {
            this.ambrosia$comfortTimer = 0;
            return;
        }
        if (!player.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
            this.ambrosia$comfortTimer = 0;
            return;
        }
        if (!player.isHurt()) {
            this.ambrosia$comfortTimer = 0;
            return;
        }
        this.ambrosia$comfortTimer++;
        if (this.ambrosia$comfortTimer >= 80) {
            player.heal(1.0F);
            this.addExhaustion(6.0F);
            this.ambrosia$comfortTimer = 0;
        }
    }
}
