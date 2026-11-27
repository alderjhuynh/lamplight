package com.aura.lamplight.folio.effect;

public final class EffectTuning {
    private EffectTuning() {}

    // chill
    public static final double CHILL_SLOW_PER_STACK = 0.10D;
    public static final int FREEZE_TICKS = 5 * 20;

    // shock
    public static final float SHOCK_CHANCE_PER_STACK = 0.10F;
    public static final float SHOCK_CHAIN_DAMAGE_SHARE = 0.5F;
    public static final double SHOCK_CHAIN_RADIUS = 5.0D;

    // resilience
    public static final float RESILIENCE_REDUCTION_PER_STACK = 0.10F;

    // void
    public static final float VOID_BONUS_PER_STACK = 0.10F;

    // burn
    public static final float BURN_DAMAGE_PER_STACK = 0.5F;
    public static final int BURN_INTERVAL_TICKS = 20;
    public static final float EXPLODE_DAMAGE = 20.0F;

    // bleed
    public static final float BLEED_DAMAGE_PER_STACK = 0.5F;
    public static final int BLEED_INTERVAL_TICKS = 20;
    public static final float RUPTURE_DAMAGE = 10.0F;

    // swirl
    public static final double SWIRL_AIR_DRAG_REDUCTION_PER_STACK = 0.05D;

    // judgement
    public static final float JUDGEMENT_REDUCTION_PER_STACK = 0.10F;
    public static final float RETRIBUTION_DAMAGE = 15.0F;
}
