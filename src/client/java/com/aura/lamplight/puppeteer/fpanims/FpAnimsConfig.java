package com.aura.lamplight.puppeteer.fpanims;

import java.util.List;

public class FpAnimsConfig {
    public static final double ANIMATION_SPEED = 1.0;
    public static final double SWITCH_SPEED = 0.018;
    public static final boolean ENABLE_SWIMMING_ANIM = true;
    public static final int SWING_SPEED = 2;
    public static final boolean ENABLE_CLIMB_AND_CRAWL = true;
    public static final boolean ENABLE_PUNCHING = true;
    public static final double VIEWMODEL_X_OFFSET = 0.0;
    public static final double VIEWMODEL_Y_OFFSET = 0.0;
    public static final double VIEWMODEL_Z_OFFSET = 0.0;
    public static final boolean ENABLE_3D_BUCKETS = false;
    public static final boolean MB3D_COMPAT = false;
    public static final List<String> MOD_IDS_TO_EXCLUDE = List.of("pointblank", "jeg", "cataclysm");
    public static final List<String> ITEM_IDS_TO_EXCLUDE = List.of();
    public static final List<String> RENDER_BLOCK_AS_ITEM = List.of(
        "create:cogwheel", "create:large_cogwheel", "create:shaft", "create:gearbox",
        "create:vertical_gearbox", "create:clutch", "create:gearshift", "create:encased_chain_drive",
        "create:adjustable_chain_gearshift", "create:belt_connector", "create:chain_conveyor",
        "create:creative_motor", "create:water_wheel", "create:large_water_wheel", "create:nozzle",
        "create:turntable", "create:hand_crank", "create:millstone", "create:crushing_wheel",
        "create:mechanical_press", "create:mechanical_mixer", "create:empty_blaze_burner",
        "create:blaze_burner", "create:depot", "create:weighted_ejector", "create:speedometer",
        "create:stressometer", "create:wooden_bracket", "create:metal_bracket", "create:schematicannon",
        "create:fluid_pipe", "create:mechanical_pump", "create:smart_fluid_pipe", "create:fluid_valve",
        "create:copper_valve_handle", "create:hose_pulley", "create:portable_fluid_interface",
        "create:steam_engine", "create:gantry_carriage", "create:gantry_shaft", "create:windmill_bearing",
        "create:mechanical_bearing", "create:clockwork_bearing", "create:rope_pulley",
        "create:elevator_pulley", "create:cart_assembler", "create:sticker", "create:contraption_controls",
        "create:mechanical_drill", "create:mechanical_saw", "create:deployer",
        "create:portable_storage_interface", "create:mechanical_harvester", "create:mechanical_plough",
        "create:mechanical_roller", "create:mechanical_crafter", "create:sequenced_gearshift",
        "create:flywheel", "create:rotation_speed_controller", "create:mechanical_arm",
        "create:controls", "create:andesite_funnel", "create:brass_funnel", "create:andesite_tunnel",
        "create:brass_tunnel", "create:package_frogport", "create:factory_gauge", "create:display_board",
        "create:peculiar_bell", "create:haunted_bell", "create:brown_toolbox", "create:clipboard"
    );

    public static void load() {
    }

    public static float getXOffset() {
        return (float) VIEWMODEL_X_OFFSET;
    }

    public static float getYOffset() {
        return (float) VIEWMODEL_Y_OFFSET;
    }

    public static float getZOffset() {
        return (float) VIEWMODEL_Z_OFFSET;
    }

    public static boolean isInRenderBlockAsItem(String itemID) {
        return RENDER_BLOCK_AS_ITEM.contains(itemID);
    }
}
