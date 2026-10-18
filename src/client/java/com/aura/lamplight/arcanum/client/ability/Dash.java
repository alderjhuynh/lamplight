package com.aura.lamplight.arcanum.client.ability;

import com.aura.lamplight.arcanum.client.util.FluidCheck;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class Dash {
    private static final int COOLDOWN_TICKS = 40;

    private static int cooldownTimer = 0;
    private static int ticksSinceLastDash = Integer.MAX_VALUE;

    private static boolean wasAirborne = false;
    private static boolean wasSneaking = false;

    public static int getTicksSinceLastDash() { return ticksSinceLastDash; }
    public static int getCooldownTimer() { return cooldownTimer; }
    public static int getCooldownTicks() { return COOLDOWN_TICKS; }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;

        if (ticksSinceLastDash < Integer.MAX_VALUE) ticksSinceLastDash++;

        if (cooldownTimer > 0) {
            cooldownTimer--;
        }

        if (player == null) {
            wasAirborne = false;
            wasSneaking = false;
            return;
        }

        if (!ArcanumEnchantmentHelper.hasDash(player)) {
            wasAirborne = false;
            wasSneaking = false;
            return;
        }

        if (FluidCheck.anyCheck()) {
            wasAirborne = false;
            wasSneaking = false;
            return;
        }

        boolean isAirborne = !player.onGround();
        boolean sneakPressed = client.options.keyShift.isDown();

        if (player.isFallFlying() || FlowGlide.isGliding) {
            wasAirborne = false;
            wasSneaking = false;
            return;
        }

        boolean dashTriggered = isAirborne && sneakPressed
                && wasAirborne && !wasSneaking;

        wasAirborne = isAirborne;
        wasSneaking = sneakPressed;

        if (!dashTriggered) return;
        if (cooldownTimer > 0) return;

        Vec3 direction = getBoostDirection(player);
        if (direction.lengthSqr() < 1.0E-6D) return;

        try {
            ClientPlayNetworking.send(new ArcanumPackets.DashPayload(client.options.keyDown.isDown()));
        } catch (Exception ignored) {}

        cooldownTimer = COOLDOWN_TICKS;
        ticksSinceLastDash = 0;
    }

    public static Vec3 getBoostDirection(LocalPlayer player) {
        Vec3 lookDirection = player.getLookAngle();
        if (lookDirection.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }
        return lookDirection.normalize();
    }
}
