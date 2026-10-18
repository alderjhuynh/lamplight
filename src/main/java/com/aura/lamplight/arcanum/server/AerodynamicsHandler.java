package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.phys.Vec3;

public final class AerodynamicsHandler {
    private static final double ACCELERATION = 0.02D;
    public static final double BOOST_MULTIPLIER = 3.0D;

    private AerodynamicsHandler() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ArcanumPackets.AerodynamicsPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!ArcanumEnchantmentHelper.hasAerodynamics(player)) return;
                    Vec3 direction = player.getLookAngle();
                    if (direction.lengthSqr() < 1.0E-6D) return;
                    direction = direction.normalize();
                    double currentAccel = ACCELERATION;
                    if (payload.boosted()) {

                        if (ArcanumEnchantmentHelper.hasWavedashPrereqs(player)) {
                            currentAccel *= BOOST_MULTIPLIER;
                        }
                    }
                    player.addDeltaMovement(new Vec3(
                            direction.x * currentAccel,
                            direction.y * currentAccel,
                            direction.z * currentAccel
                    ));
                    player.hurtMarked = true;
                }));
    }
}
