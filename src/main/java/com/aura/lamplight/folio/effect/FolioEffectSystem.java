package com.aura.lamplight.folio.effect;

public final class FolioEffectSystem {
    private FolioEffectSystem() {}

    public static void initialize() {
        FolioEffects.initialize();
        EffectSources.initialize();
        EffectScheduler.initialize();
        FolioEffectCommand.initialize();
    }
}
