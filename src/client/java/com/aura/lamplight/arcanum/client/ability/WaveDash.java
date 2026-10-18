package com.aura.lamplight.arcanum.client.ability;

import com.aura.lamplight.arcanum.client.util.FluidCheck;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.Identifier;

public final class WaveDash {

    private static final int WINDOW_TICKS = 5;
    private static final int BOOST_DURATION_TICKS = 60;

    private static final float MIN_PITCH_TO_WAVEDASH = 60.0F;

    private static boolean wasAirborne = false;
    private static int ticksSinceLanding = Integer.MAX_VALUE;
    public static int boostTicksRemaining = 0;
    private static boolean canWaveDash = true;

    public static boolean isBoosted() {
        return boostTicksRemaining > 0;
    }

    public static int getBoostTicksRemaining() {
        return boostTicksRemaining;
    }

    private static boolean isLookingDown(LocalPlayer player) {
        return player.getXRot() >= MIN_PITCH_TO_WAVEDASH;
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            boostTicksRemaining = 0;
            return;
        }
        if (!ArcanumEnchantmentHelper.hasWavedashPrereqs(player)) {
            if (boostTicksRemaining > 0) {

                boostTicksRemaining = 0;
                canWaveDash = true;
            }
            wasAirborne = !player.onGround();
            return;
        }

        if (FluidCheck.anyCheck() && boostTicksRemaining > 0) {
            boostTicksRemaining = 0;
            canWaveDash = true;
            SoundEvent wddSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("lamplight", "wavedash_deactivates"));
            if (client.level != null) {
                client.level.playSound(
                        player,
                        player.blockPosition(),
                        wddSound,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F
                );
            }
            return;
        }

        if (boostTicksRemaining > 0) {
            boostTicksRemaining--;
            if (boostTicksRemaining == 0) {
                canWaveDash = true;
            }
        }

        boolean onGround = player.onGround();

        if (wasAirborne && onGround) {
            if (Dash.getTicksSinceLastDash() <= Dash.getCooldownTicks()/2) {
                ticksSinceLanding = 0;
            }
        }

        if (onGround && ticksSinceLanding < Integer.MAX_VALUE) {
            if (ticksSinceLanding <= WINDOW_TICKS && canWaveDash && isLookingDown(player)) {
                boostTicksRemaining = BOOST_DURATION_TICKS;
                canWaveDash = false;
                ticksSinceLanding = Integer.MAX_VALUE;

                SoundEvent wdSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("lamplight", "wavedash_activates"));
                if (client.level != null) {
                    client.level.playSound(
                            player,
                            player.blockPosition(),
                            wdSound,
                            SoundSource.PLAYERS,
                            1.0F,
                            1.0F
                    );
                }
            } else {
                ticksSinceLanding++;
                if (ticksSinceLanding > WINDOW_TICKS) {
                    ticksSinceLanding = Integer.MAX_VALUE;
                }
            }
        }

        wasAirborne = !onGround;
    }
}
