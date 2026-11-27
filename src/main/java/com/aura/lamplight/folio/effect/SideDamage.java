package com.aura.lamplight.folio.effect;

import com.aura.lamplight.mixin.folio.LivingEntityLastHurtAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

final class SideDamage {
    private SideDamage() {}

    static boolean deal(
        final ServerLevel level,
        final LivingEntity target,
        final ResourceKey<DamageType> type,
        final @Nullable LivingEntity attacker,
        final float amount
    ) {
        if (!target.isAlive() || amount <= 0.0F) {
            return false;
        }
         DamageSource source = new CreditedSource(
            level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(type),
            attacker);

        LivingEntityLastHurtAccessor accessor = (LivingEntityLastHurtAccessor) target;
        int invulnerableTime = target.invulnerableTime;
        float lastHurt = accessor.folio$getLastHurt();
        target.invulnerableTime = 0;
        try {
            return target.hurtServer(level, source, amount);
        } finally {
            target.invulnerableTime = invulnerableTime;
            accessor.folio$setLastHurt(lastHurt);
        }
    }

    private static final class CreditedSource extends DamageSource {
        private CreditedSource(final Holder<DamageType> type, final @Nullable LivingEntity attacker) {
            super(type, null, attacker);
        }

        @Override
        public Component getLocalizedDeathMessage(final LivingEntity victim) {
            Entity attacker = this.getEntity();
            if (attacker == null || attacker == victim) {
                return Component.translatable("death.attack." + this.getMsgId(), victim.getDisplayName());
            }
            return Component.translatable("death.attack." + this.getMsgId() + ".player",
                victim.getDisplayName(), attacker.getDisplayName());
        }
    }
}
