package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.SpinAttackRailData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSpinRailDataMixin implements SpinAttackRailData {

    @Unique
    private static final EntityDataAccessor<Boolean> ARCANUM_INFERNO_SPIN_RAIL =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void arcanum$defineSpinRail(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(ARCANUM_INFERNO_SPIN_RAIL, false);
    }

    @Override
    public boolean arcanum$isInfernoSpinRail() {
        return ((Player) (Object) this).getEntityData().get(ARCANUM_INFERNO_SPIN_RAIL);
    }

    @Override
    public void arcanum$setInfernoSpinRail(boolean inferno) {
        ((Player) (Object) this).getEntityData().set(ARCANUM_INFERNO_SPIN_RAIL, inferno);
    }
}
