package com.aura.lamplight.arcanum.enchantment;

import com.aura.lamplight.arcanum.Arcanum;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ArcanumEnchantments {
    private ArcanumEnchantments() {}

    public static final ResourceKey<Enchantment> PROPULSION = key("propulsion");
    public static final ResourceKey<Enchantment> AERODYNAMICS = key("aerodynamics");
    public static final ResourceKey<Enchantment> DASH = key("dash");
    public static final ResourceKey<Enchantment> DOUBLE_JUMP = key("double_jump");
    public static final ResourceKey<Enchantment> FROST = key("frost");
    public static final ResourceKey<Enchantment> ICE_ASPECT = key("ice_aspect");
    public static final ResourceKey<Enchantment> COLD_STEEL = key("cold_steel");
    public static final ResourceKey<Enchantment> DARKNESS_CLOAK = key("darkness_cloak");
    public static final ResourceKey<Enchantment> DRAGONHEART = key("dragonheart");
    public static final ResourceKey<Enchantment> FIRE_SHIELD = key("fire_shield");
    public static final ResourceKey<Enchantment> FLAME_WALKER = key("flame_walker");
    public static final ResourceKey<Enchantment> ICE_SHIELD = key("ice_shield");
    public static final ResourceKey<Enchantment> LEAPING = key("leaping");
    public static final ResourceKey<Enchantment> NIGHT_VISION = key("night_vision");
    public static final ResourceKey<Enchantment> REGROWTH = key("regrowth");
    public static final ResourceKey<Enchantment> SATURATION = key("saturation");
    public static final ResourceKey<Enchantment> ANCHOR = key("anchor");
    public static final ResourceKey<Enchantment> WATER_BREATHING = key("water_breathing");
    public static final ResourceKey<Enchantment> REELING = key("reeling");
    public static final ResourceKey<Enchantment> LEECH = key("leech");
    public static final ResourceKey<Enchantment> INFERNO = key("inferno");

    // capstones
    public static final ResourceKey<Enchantment> CINDERHEART = key("cinderheart");
    public static final ResourceKey<Enchantment> WINTERS_EDGE = key("winters_edge");
    public static final ResourceKey<Enchantment> THUNDERSTEP = key("thunderstep");
    public static final ResourceKey<Enchantment> SUNDER = key("sunder");
    public static final ResourceKey<Enchantment> HEARTWOOD = key("heartwood");
    public static final ResourceKey<Enchantment> WINDRUNNER = key("windrunner");
    public static final ResourceKey<Enchantment> GRAVECALLER = key("gravecaller");
    public static final ResourceKey<Enchantment> MAGNUM_OPUS = key("magnum_opus");
    public static final ResourceKey<Enchantment> EMBERFORGE = key("emberforge");
    public static final ResourceKey<Enchantment> ICEBOUND = key("icebound");
    public static final ResourceKey<Enchantment> STORMWARD = key("stormward");
    public static final ResourceKey<Enchantment> UNBROKEN = key("unbroken");
    public static final ResourceKey<Enchantment> GROVEKEEPER = key("grovekeeper");
    public static final ResourceKey<Enchantment> ZEPHYR = key("zephyr");
    public static final ResourceKey<Enchantment> SHROUD = key("shroud");
    public static final ResourceKey<Enchantment> PROVISIONER = key("provisioner");

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, Arcanum.id(path));
    }
}
