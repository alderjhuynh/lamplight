package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;

public final class DoubleJumpHandler {
    private DoubleJumpHandler() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArcanumPackets.DoubleJumpPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!ArcanumEnchantmentHelper.hasDoubleJump(player)) return;

                    player.jumpFromGround();

                    player.hurtMarked = true;

                    ServerLevel level = (ServerLevel) player.level();
                    for (int i = 0; i < 5; i++) {
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
                            SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("lamplight", "double_jump")),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }));
    }
}
