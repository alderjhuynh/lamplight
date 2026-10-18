package com.aura.lamplight.arcanum.client.ability;

import com.aura.lamplight.arcanum.client.util.FluidCheck;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.network.ArcanumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DoubleJump {
    private static boolean wasJumpPressed = false;
    private static boolean hasLeftGround = false;
    private static boolean jumpReleasedAfterAirborne = false;
    private static double GLIDING_BOOST_STRENGTH = 0.07d;
    private static int EXTRA_AERODYNAMICS_TIME = 40;

    private static final int MAX_TICKS_AVAILABLE = 80;
    private static final int JUMP_TICK_COST = 40;
    private static int ticksAvailable = MAX_TICKS_AVAILABLE;

    public static double fallBlocksThreshold = 2.0;
    private static double airborneStartY = Double.NaN;
    private static boolean fallThresholdMet = false;

    private static boolean isNearGround(LocalPlayer player) {
        AABB bb = player.getBoundingBox();
        double probeY = bb.minY - 0.05;

        double[] xs = { bb.minX + 0.05, bb.maxX - 0.05, (bb.minX + bb.maxX) / 2.0 };
        double[] zs = { bb.minZ + 0.05, bb.maxZ - 0.05, (bb.minZ + bb.maxZ) / 2.0 };

        for (double x : xs) {
            for (double z : zs) {
                BlockPos pos = BlockPos.containing(x, probeY, z);
                if (!player.level().getBlockState(pos).isAir()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getCooldownTimer() {
        return MAX_TICKS_AVAILABLE - ticksAvailable;
    }

    public static int getCooldownTicks() {
        return MAX_TICKS_AVAILABLE;
    }

    public static int getTicksAvailable() {
        return ticksAvailable;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (!ArcanumEnchantmentHelper.hasDoubleJump(player)) {

            if (ticksAvailable < MAX_TICKS_AVAILABLE) ticksAvailable++;
            wasJumpPressed = client.options.keyJump.isDown();
            return;
        }

        boolean isJumpPressed = client.options.keyJump.isDown();
        boolean onGround = isNearGround(player);

        if (ticksAvailable < MAX_TICKS_AVAILABLE) {
            ticksAvailable++;
        }

        if (FluidCheck.anyCheck()) return;

        if (onGround || player.onClimbable() || player.isInWater() || player.getAbilities().flying) {
            wasJumpPressed = isJumpPressed;
            hasLeftGround = false;
            jumpReleasedAfterAirborne = false;
            airborneStartY = Double.NaN;
            fallThresholdMet = false;
            return;
        }

        if (!onGround && !hasLeftGround) {
            hasLeftGround = true;
            airborneStartY = player.getY();
        }

        if (hasLeftGround && !Double.isNaN(airborneStartY)) {
            double fallen = airborneStartY - player.getY();
            if (fallen >= fallBlocksThreshold) {
                fallThresholdMet = true;
            }
        }

        if (hasLeftGround && wasJumpPressed && !isJumpPressed) {
            jumpReleasedAfterAirborne = true;
        }

        boolean canDoubleJumpByRelease = hasLeftGround && jumpReleasedAfterAirborne;
        boolean canDoubleJumpByFall = hasLeftGround && fallThresholdMet;

        if ((canDoubleJumpByRelease || canDoubleJumpByFall)
                && isJumpPressed
                && !wasJumpPressed
                && ticksAvailable >= JUMP_TICK_COST) {

            ticksAvailable -= JUMP_TICK_COST;

            Vec3 direction = Dash.getBoostDirection(player);
            if (direction.lengthSqr() < 1.0E-6D) {
                wasJumpPressed = isJumpPressed;
                return;
            }

            if (player.isFallFlying() || FlowGlide.isGliding) {
                player.push(
                        direction.x * GLIDING_BOOST_STRENGTH,
                        direction.y * GLIDING_BOOST_STRENGTH,
                        direction.z * GLIDING_BOOST_STRENGTH
                );
                try { ClientPlayNetworking.send(new ArcanumPackets.DoubleJumpPayload()); } catch (Exception ignored) {}
                WaveDash.boostTicksRemaining += EXTRA_AERODYNAMICS_TIME;
            } else {
                try { ClientPlayNetworking.send(new ArcanumPackets.DoubleJumpPayload()); } catch (Exception ignored) {}
            }
            jumpReleasedAfterAirborne = false;
            fallThresholdMet = false;
            airborneStartY = player.getY();
        }

        wasJumpPressed = isJumpPressed;
    }

    public static void onEnable(Minecraft client) {
        wasJumpPressed = false;
        ticksAvailable = MAX_TICKS_AVAILABLE;
        airborneStartY = Double.NaN;
        fallThresholdMet = false;
    }
}
