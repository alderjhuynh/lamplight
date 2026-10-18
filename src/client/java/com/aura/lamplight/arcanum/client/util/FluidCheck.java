package com.aura.lamplight.arcanum.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class FluidCheck {
    private FluidCheck() {}

    private static LocalPlayer player() {
        Minecraft client = Minecraft.getInstance();
        return client.player;
    }

    public static boolean isTouchingWater() {
        LocalPlayer p = player();
        return p != null && p.isInWater();
    }

    public static boolean isInWater() {
        LocalPlayer p = player();
        return p != null && p.isInWater();
    }

    public static boolean isInLava() {
        LocalPlayer p = player();
        return p != null && p.isInLava();
    }

    public static boolean isEyeInFluid() {
        LocalPlayer p = player();
        return p != null && p.isUnderWater();
    }

    public static boolean anyCheck() {
        return isTouchingWater() || isInWater() || isInLava() || isEyeInFluid();
    }
}
