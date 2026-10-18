package com.aura.lamplight.mixin.menagerie;

import com.aura.lamplight.menagerie.crow.CrowShoulderData;
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
public abstract class PlayerShoulderCrowMixin implements CrowShoulderData {
    @Unique
    private static final EntityDataAccessor<Boolean> MENAGERIE_CROW_LEFT =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final EntityDataAccessor<Boolean> MENAGERIE_CROW_RIGHT =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void menagerie$defineCrowShoulders(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(MENAGERIE_CROW_LEFT, false);
        builder.define(MENAGERIE_CROW_RIGHT, false);
    }

    @Unique
    private Player menagerie$self() {
        return (Player) (Object) this;
    }

    @Override
    public boolean menagerie$hasCrowOnLeftShoulder() {
        return menagerie$self().getEntityData().get(MENAGERIE_CROW_LEFT);
    }

    @Override
    public boolean menagerie$hasCrowOnRightShoulder() {
        return menagerie$self().getEntityData().get(MENAGERIE_CROW_RIGHT);
    }

    @Override
    public void menagerie$setCrowOnLeftShoulder(boolean present) {
        menagerie$self().getEntityData().set(MENAGERIE_CROW_LEFT, present);
    }

    @Override
    public void menagerie$setCrowOnRightShoulder(boolean present) {
        menagerie$self().getEntityData().set(MENAGERIE_CROW_RIGHT, present);
    }
}
