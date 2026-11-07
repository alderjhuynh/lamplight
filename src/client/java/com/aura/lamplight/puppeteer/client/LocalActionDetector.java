package com.aura.lamplight.puppeteer.client;

import com.aura.lamplight.puppeteer.network.PlayerActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class LocalActionDetector {
    private LocalActionDetector() {}

    private static final int HOLD_REFRESH_TICKS = 8;

    private static boolean lastJumpDown = false;
    private static boolean jumpSent = false;
    private static int jumpRefreshCounter = 0;

    private static boolean lastAttackDown = false;
    private static int attackRefreshCounter = 0;

    private static boolean lastSwapOffhandDown = false;

    private static int airborneTicks = 0;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(LocalActionDetector::onClientTick);
    }

    private static void onClientTick(final Minecraft client) {
        if (client.player == null || client.player.isDeadOrDying()) {
            airborneTicks = 0;
            lastJumpDown = false;
            jumpSent = false;
            jumpRefreshCounter = 0;
            lastAttackDown = false;
            attackRefreshCounter = 0;
            lastSwapOffhandDown = false;
            return;
        }

        var options = client.options;
        boolean jumpDown = options.keyJump.isDown();
        boolean onGround = client.player.onGround();
        boolean attacking = options.keyAttack.isDown();
        boolean swappingOffhand = options.keySwapOffhand.isDown();

        if (swappingOffhand && !lastSwapOffhandDown) {
            send("swap_offhand");
        }
        lastSwapOffhandDown = swappingOffhand;

        if (jumpDown && !lastJumpDown) {
            jumpRefreshCounter = 0;
        }
        lastJumpDown = jumpDown;
        if (jumpDown && !onGround) {
            if (!jumpSent || jumpRefreshCounter == 0) {
                send("jump");
            }
            jumpSent = true;
            jumpRefreshCounter = (jumpRefreshCounter + 1) % HOLD_REFRESH_TICKS;
        } else {
            jumpSent = false;
            jumpRefreshCounter = 0;
        }

        if (onGround) {
            if (airborneTicks >= 3) {
                send("land");
            }
            airborneTicks = 0;
        } else {
            airborneTicks++;
        }

        if (attacking) {
            if (!lastAttackDown) {
                lastAttackDown = true;
                attackRefreshCounter = 0;
                send("attack");
            } else {
                attackRefreshCounter++;
                if (attackRefreshCounter >= HOLD_REFRESH_TICKS) {
                    attackRefreshCounter = 0;
                    send("attack");
                }
            }
        } else {
            lastAttackDown = false;
            attackRefreshCounter = 0;
        }
    }

    private static void send(final String action) {
        ClientPlayNetworking.send(new PlayerActionPayload(action));
    }
}
