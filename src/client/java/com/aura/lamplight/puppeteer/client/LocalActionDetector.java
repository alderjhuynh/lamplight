package com.aura.lamplight.puppeteer.client;

import com.aura.lamplight.puppeteer.network.PlayerActionPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class LocalActionDetector {
    private LocalActionDetector() {}

    private static Boolean lastJumpDown = null;
    private static int airborneTicks = 0;
    private static Boolean lastAttackDown = null;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(LocalActionDetector::onClientTick);
    }

    private static void onClientTick(final Minecraft client) {
        if (client.player == null || client.player.isDeadOrDying()) {
            airborneTicks = 0;
            lastJumpDown = null;
            lastAttackDown = null;
            return;
        }

        var options = client.options;
        boolean jumping = options.keyJump.isDown() && client.player.onGround();

        if (lastJumpDown == null || lastJumpDown != jumping) {
            lastJumpDown = jumping;
            if (jumping) {
                send("jump");
            }
        }

        boolean onGround = client.player.onGround();
        if (onGround) {
            if (airborneTicks >= 3) {
                send("land");
            }
            airborneTicks = 0;
        } else {
            airborneTicks++;
        }

        boolean attacking = options.keyAttack.isDown() && client.hitResult != null;
        if (lastAttackDown == null || lastAttackDown != attacking) {
            lastAttackDown = attacking;
            if (attacking) {
                send("attack");
            }
        }
    }

    private static void send(final String action) {
        ClientPlayNetworking.send(new PlayerActionPayload(action));
    }
}
