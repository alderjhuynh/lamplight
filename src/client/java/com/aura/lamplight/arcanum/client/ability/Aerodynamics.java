package com.aura.lamplight.arcanum.client.ability;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Aerodynamics {
    private static final boolean AllowElytra = true;

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;
        if (!ArcanumEnchantmentHelper.hasAerodynamics(player)) return;

        if (!AllowElytra) {
            if (player.onGround() || player.isFallFlying()) {
                return;
            }
        }

        if (AllowElytra) {
            if (player.onGround()) return;
        }

        if (FlowGlide.isGliding) return;

        if (player.isSprinting() || (player.isFallFlying() && AllowElytra && client.options.keySprint.isDown())) {

            Vec3 direction = getBoostDirection(player);
            if (direction.lengthSqr() < 1.0E-6D) {
                return;
            }

            try {
                ClientPlayNetworking.send(new ArcanumPackets.AerodynamicsPayload(WaveDash.isBoosted()));
            } catch (Exception ignored) {}
        }
    }

    private static Vec3 getBoostDirection(LocalPlayer player) {
        Vec3 lookDirection = player.getLookAngle();
        if (lookDirection.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }
        return lookDirection.normalize();
    }
}
