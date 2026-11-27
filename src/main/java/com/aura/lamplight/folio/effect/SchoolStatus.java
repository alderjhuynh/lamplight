package com.aura.lamplight.folio.effect;

import com.aura.lamplight.folio.spell.SpellSchool;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public enum SchoolStatus {
    CHILL(SpellSchool.ICE, Recipient.TARGET, true, () -> FolioEffects.CHILL),
    SHOCK(SpellSchool.LIGHTNING, Recipient.TARGET, false, () -> FolioEffects.SHOCK),
    RESILIENCE(SpellSchool.NATURE, Recipient.CASTER, false, () -> FolioEffects.RESILIENCE),
    VOID(SpellSchool.ARCANE, Recipient.TARGET, false, () -> FolioEffects.VOID),
    BURN(SpellSchool.FLAME, Recipient.TARGET, true, () -> FolioEffects.BURN),
    BLEED(SpellSchool.BLOOD, Recipient.TARGET, true, () -> FolioEffects.BLEED),
    SWIRL(SpellSchool.WIND, Recipient.CASTER, false, () -> FolioEffects.SWIRL),
    JUDGEMENT(SpellSchool.HOLY, Recipient.TARGET, true, () -> FolioEffects.JUDGEMENT);

    public enum Recipient {
        CASTER,
        TARGET
    }

    private final SpellSchool school;
    private final Recipient recipient;
    private final boolean triggersAtMax;
    private final Supplier<Holder<MobEffect>> effect;

    SchoolStatus(
        final SpellSchool school,
        final Recipient recipient,
        final boolean triggersAtMax,
        final Supplier<Holder<MobEffect>> effect
    ) {
        this.school = school;
        this.recipient = recipient;
        this.triggersAtMax = triggersAtMax;
        this.effect = effect;
    }

    public SpellSchool school() {
        return school;
    }

    public Recipient recipient() {
        return recipient;
    }

    public boolean triggersAtMax() {
        return triggersAtMax;
    }

    public Holder<MobEffect> effect() {
        return effect.get();
    }

    public static SchoolStatus of(final SpellSchool school) {
        for (SchoolStatus status : values()) {
            if (status.school == school) {
                return status;
            }
        }
        throw new IllegalStateException("No status effect for school " + school);
    }
}
