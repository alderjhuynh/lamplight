package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.network.ArcanumPackets;
import com.aura.lamplight.arcanum.server.JetServerHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketPropulsionMixin {

    @Inject(method = "explode", at = @At("HEAD"))
    private void arcanum$onJetExplode(ServerLevel level, CallbackInfo ci) {
        FireworkRocketEntity self = (FireworkRocketEntity) (Object) this;
        if (!self.entityTags().contains("lamplight:jet_rocket")) return;

        var owner = self.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;

        Vec3 rocketPos = self.position();
        Vec3 feetPos = player.position().add(0, 0.1, 0);
        double distSq = rocketPos.distanceToSqr(feetPos);

        if (distSq > 9.0) {

            double horizDistSq = (rocketPos.x - feetPos.x) * (rocketPos.x - feetPos.x) + (rocketPos.z - feetPos.z) * (rocketPos.z - feetPos.z);
            if (horizDistSq > 4.0 || Math.abs(rocketPos.y - feetPos.y) > 3.0) {
                return;
            }
        }

        Vec3 look = player.getLookAngle();
        Vec3 horiz = new Vec3(look.x, 0, look.z);
        if (horiz.lengthSqr() > 1.0E-6D) {
            horiz = horiz.normalize().scale(0.85);
        } else {
            horiz = Vec3.ZERO;
        }
        Vec3 boost = new Vec3(horiz.x, 1.4, horiz.z);
        player.addDeltaMovement(boost);
        player.hurtMarked = true;
        level.sendParticles(ParticleTypes.FIREWORK, rocketPos.x, rocketPos.y, rocketPos.z, 20, 0.4, 0.3, 0.4, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.0F, 1.0F);

        JetServerHandler.setBoost(player, 60);

        try {
            ServerPlayNetworking.send(player, new ArcanumPackets.JetBoostPayload(60));
        } catch (Exception ignored) {
        }
    }
}
