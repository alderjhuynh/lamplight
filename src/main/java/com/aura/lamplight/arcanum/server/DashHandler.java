package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class DashHandler {
    private static final double STRENGTH = 0.6D;

    private DashHandler() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArcanumPackets.DashPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!ArcanumEnchantmentHelper.hasDash(player)) return;

                    Vec3 direction = player.getLookAngle();
                    if (direction.lengthSqr() < 1.0E-6D) return;
                    direction = direction.normalize();
                    if (payload.backwards()) {
                        direction = direction.scale(-1.0D);
                    }
                    player.addDeltaMovement(new Vec3(
                            direction.x * STRENGTH,
                            direction.y * STRENGTH,
                            direction.z * STRENGTH
                    ));
                    player.hurtMarked = true;

                    ServerLevel level = (ServerLevel) player.level();
                    var random = player.getRandom();
                    for (int i = 0; i < 10; i++) {
                        level.sendParticles(
                                ParticleTypes.CLOUD,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1,
                                0, 0, 0,
                                0.05
                        );
                    }
                    level.playSound(
                            null,
                            player.blockPosition(),
                            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("lamplight", "dash")),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }));
    }
}
