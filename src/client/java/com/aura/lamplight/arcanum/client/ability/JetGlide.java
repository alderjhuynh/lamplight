package com.aura.lamplight.arcanum.client.ability;

import com.aura.lamplight.arcanum.client.util.FluidCheck;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class JetGlide {
    private JetGlide() {}

    private static final double ACCELERATION = 0.09D;
    private static final double VERTICAL_BOOST = 0.05D;

    private static SoundEvent GLIDE_SOUND = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("lamplight", "entity_glides"));
    private static JetGlideSound activeSoundInstance = null;

    public static boolean enabled = true;

    public static boolean isGliding = false;

    public static int boostTicksRemaining = 0;
    private static final int BOOST_DURATION_TICKS = 60;

    public static boolean isBoosted() {
        return boostTicksRemaining > 0;
    }

    public static void setBoost(int ticks) {
        boostTicksRemaining = ticks;

        Minecraft client = Minecraft.getInstance();
        if (client.player != null && enabled && !client.player.onGround() && !client.player.isFallFlying() && !FluidCheck.anyCheck()) {
            if (!isGliding) {
                isGliding = true;
                startSound(client, client.player);
            }
        }
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (boostTicksRemaining > 0) {
            boostTicksRemaining--;
        }

        if (player.onGround() || player.isFallFlying() || FluidCheck.anyCheck()) {
            if (isGliding) stopGliding();
            if (player.onGround()) {

                boostTicksRemaining = 0;
            }
            return;
        }

        if (!isGliding && isBoosted() && canStartGlide(player)) {
            isGliding = true;
            startSound(client, player);
        }

        if (isGliding && canContinueGlide(player)) {

            try {
                ClientPlayNetworking.send(new ArcanumPackets.GlidePayload());
            } catch (Exception ignored) {}
        }
    }

    private static void startSound(Minecraft client, LocalPlayer player) {
        activeSoundInstance = new JetGlideSound(player, GLIDE_SOUND);
        client.getSoundManager().play(activeSoundInstance);
    }

    private static void stopGliding() {
        isGliding = false;
        activeSoundInstance = null;
    }

    public static boolean canStartGlide(LocalPlayer player) {
        return enabled
                && !player.onGround()
                && !player.isFallFlying()
                && isBoosted();
    }

    public static boolean canContinueGlide(LocalPlayer player) {

        return enabled
                && !player.onGround()
                && !player.isFallFlying();
    }

    public static Vec3 getBoostDirection(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        double x = velocity.x;
        double z = velocity.z;
        double horizLengthSq = x * x + z * z;
        if (horizLengthSq < 1.0E-6D) {
            Vec3 look = player.getLookAngle();
            Vec3 dir = new Vec3(look.x, 0, look.z);
            if (dir.lengthSqr() < 1.0E-6D) return Vec3.ZERO;
            return dir.normalize();
        }
        double scale = 1.0 / Math.sqrt(horizLengthSq);
        return new Vec3(x * scale, 0, z * scale);
    }
}
