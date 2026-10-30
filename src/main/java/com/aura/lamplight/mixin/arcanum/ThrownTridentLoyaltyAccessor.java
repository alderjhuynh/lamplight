package com.aura.lamplight.mixin.arcanum;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThrownTrident.class)
public interface ThrownTridentLoyaltyAccessor {

    @Accessor("ID_LOYALTY")
    static EntityDataAccessor<Byte> lamplight$loyaltyData() {
        throw new AssertionError();
    }
}
