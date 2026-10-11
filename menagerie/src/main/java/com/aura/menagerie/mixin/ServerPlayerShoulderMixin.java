package com.aura.menagerie.mixin;

import com.aura.menagerie.crow.CrowShoulderData;
import com.aura.menagerie.registry.MenagerieEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mirrors the parrot-variant sync: whenever the shoulder NBT changes (mount, dismount/respawn,
 * login, respawn), update the crow flags so clients render the perched crow.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerShoulderMixin {
    @Inject(method = "setShoulderEntityLeft", at = @At("TAIL"))
    private void menagerie$syncCrowLeft(CompoundTag tag, CallbackInfo ci) {
        ((CrowShoulderData) this).menagerie$setCrowOnLeftShoulder(isCrowTag(tag));
    }

    @Inject(method = "setShoulderEntityRight", at = @At("TAIL"))
    private void menagerie$syncCrowRight(CompoundTag tag, CallbackInfo ci) {
        ((CrowShoulderData) this).menagerie$setCrowOnRightShoulder(isCrowTag(tag));
    }

    @Unique
    private static boolean isCrowTag(CompoundTag tag) {
        if (tag.isEmpty()) {
            return false;
        }
        return tag.read("id", EntityType.CODEC).orElse(null) == MenagerieEntities.CROW;
    }
}
