package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlocksAttacks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class CapstoneHandler {
    private CapstoneHandler() {}

    private static final Map<UUID, Map<UUID, Integer>> KINDLING = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> CHILL = new HashMap<>();
    private static final Map<UUID, Integer> THUNDERSTEP_CHARGE = new HashMap<>();
    private static final Map<UUID, Integer> STORMWARD_WINDOW = new HashMap<>();
    private static final Map<UUID, Integer> EMBERFORGE_CHARGE = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_SPRINTING = new HashMap<>();
    private static final Map<UUID, Integer> SPRINT_STOP_COOLDOWN = new HashMap<>();

    private static final ThreadLocal<Boolean> RETALIATING = ThreadLocal.withInitial(() -> false);

    private static final int KINDLING_MAX = 3;
    private static final float DETONATE_RADIUS = 2.5F;
    private static final float DETONATE_DAMAGE = 5.0F;
    private static final int DETONATE_BURN_TICKS = 40;
    private static final int CHILL_MAX = 3;
    private static final int CHILL_BONUS_DAMAGE = 4;
    private static final float SHATTER_RADIUS = 2.5F;
    private static final float SHATTER_DAMAGE = 4.0F;
    private static final float THUNDERSTEP_BONUS = 3.0F;
    private static final int THUNDERSTEP_CHARGE_TICKS = 100;
    private static final int SPRINT_STOP_COOLDOWN_TICKS = 100;
    private static final int STORMWARD_WINDOW_TICKS = 60;
    private static final float STORMWARD_RETALIATION = 5.0F;
    private static final int EMBERFORGE_MAX = 4;
    private static final int EMBERFORGE_BURN_TICKS = 60;
    private static final float HEARTWOOD_LIFESTEAL = 3.0F;
    private static final float HEARTWOOD_ABSORPTION_CAP = 8.0F;
    private static final float GRAVECALLER_EXECUTE_THRESHOLD = 0.3F;
    private static final float GRAVECALLER_EXECUTE_BONUS = 6.0F;
    private static final float SHROUD_LIFESTEAL = 1.5F;
    private static final float SHROUD_LOW_HEALTH_FRACTION = 0.4F;
    private static final double SHROUD_AURA_RADIUS = 5.0D;
    private static final int SHROUD_AURA_DARKNESS_TICKS = 80;
    private static final float ZEPHYR_DODGE_CHANCE = 0.20F;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(CapstoneHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> clearPlayer(handler.getPlayer().getUUID()));
    }

    public static void clearPlayer(UUID uuid) {
        KINDLING.remove(uuid);
        CHILL.remove(uuid);
        THUNDERSTEP_CHARGE.remove(uuid);
        STORMWARD_WINDOW.remove(uuid);
        EMBERFORGE_CHARGE.remove(uuid);
        WAS_SPRINTING.remove(uuid);
        SPRINT_STOP_COOLDOWN.remove(uuid);
    }

    public static void onDash(ServerPlayer player) {
        if (RETALIATING.get()) return;
        if (ArcanumEnchantmentHelper.hasEnchantmentOnStack(player.getMainHandItem(), ArcanumEnchantments.THUNDERSTEP)) {
            THUNDERSTEP_CHARGE.put(player.getUUID(), THUNDERSTEP_CHARGE_TICKS);
        }
        if (ArcanumEnchantmentHelper.wearsEnchantment(player, ArcanumEnchantments.STORMWARD)) {
            STORMWARD_WINDOW.put(player.getUUID(), STORMWARD_WINDOW_TICKS);
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, STORMWARD_WINDOW_TICKS + 20, 1, false, false, true));
        }
    }

    public static void onHurt(ServerLevel level, LivingEntity victim, DamageSource source, boolean dealt) {
        if (RETALIATING.get()) return;
        if (victim.getHealth() <= 0.0F) return;

        Entity attackerEntity = source.getEntity();
        if (attackerEntity instanceof LivingEntity attacker) {
            ItemStack weapon = source.getWeaponItem();
            if (dealt) {
                handleAttackSide(level, attacker, weapon, victim, source);
            }
            handleWearerSide(level, victim, attacker, source, dealt);
        }
    }

    public static boolean tryDodge(LivingEntity victim, DamageSource source) {
        if (RETALIATING.get()) return false;
        if (!(source.getEntity() instanceof LivingEntity)) return false;
        if (!ArcanumEnchantmentHelper.wearsEnchantment(victim, ArcanumEnchantments.ZEPHYR)) return false;
        boolean meleeOrProjectile = source.isDirect() || source.is(DamageTypeTags.IS_PROJECTILE);
        if (!meleeOrProjectile) return false;
        return victim.getRandom().nextFloat() < ZEPHYR_DODGE_CHANCE;
    }

    public static void onDeath(ServerLevel level, LivingEntity dead) {
        if (RETALIATING.get()) return;
        boolean shatter = dead.isFullyFrozen() && chillOf(null, dead.getUUID()) >= CHILL_MAX;
        forgetTarget(dead.getUUID());
        if (!shatter) return;

        DamageSource shatterSource = attributed(level, DamageTypes.FREEZE, null);
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                dead.getBoundingBox().inflate(SHATTER_RADIUS),
                e -> e != dead && e.isAlive() && !e.isAlliedTo(dead))) {
            dealAbilityDamage(level, other, shatterSource, SHATTER_DAMAGE);
        }
    }

    private static void tick(MinecraftServer server) {
        int tick = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickThunderstep(player);
            tickStormward(player);
            if (tick % 40 == 0) tickShroudAura(server, player);
            if (tick % 200 == 0) tickGrovekeeper(player);
        }

        if (tick % 100 == 0) pruneState();
    }

    private static void tickThunderstep(ServerPlayer player) {
        UUID id = player.getUUID();
        boolean sprinting = player.isSprinting();
        boolean was = Boolean.TRUE.equals(WAS_SPRINTING.get(id));
        Integer cooldown = SPRINT_STOP_COOLDOWN.get(id);
        if (ArcanumEnchantmentHelper.hasEnchantmentOnStack(player.getMainHandItem(), ArcanumEnchantments.THUNDERSTEP)
                && was && !sprinting
                && player.getDeltaMovement().horizontalDistance() < 0.05D
                && (cooldown == null || cooldown <= 0)) {
            THUNDERSTEP_CHARGE.put(id, THUNDERSTEP_CHARGE_TICKS);
            SPRINT_STOP_COOLDOWN.put(id, SPRINT_STOP_COOLDOWN_TICKS);
        } else if (cooldown != null && cooldown > 0) {
            SPRINT_STOP_COOLDOWN.put(id, cooldown - 1);
        }
        WAS_SPRINTING.put(id, sprinting);
    }

    private static void tickStormward(ServerPlayer player) {
        UUID id = player.getUUID();
        Integer window = STORMWARD_WINDOW.get(id);
        if (window != null) {
            if (window <= 1) {
                STORMWARD_WINDOW.remove(id);
            } else {
                STORMWARD_WINDOW.put(id, window - 1);
            }
        }
    }

    private static void tickShroudAura(MinecraftServer server, ServerPlayer player) {
        if (!player.isAlive()
                || !ArcanumEnchantmentHelper.wearsEnchantment(player, ArcanumEnchantments.SHROUD)) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(SHROUD_AURA_RADIUS),
                e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
            other.addEffect(new MobEffectInstance(MobEffects.DARKNESS, SHROUD_AURA_DARKNESS_TICKS, 0, false, false, true));
        }
    }

    @SuppressWarnings("all")
    private static void tickGrovekeeper(ServerPlayer player) {
        if (!player.isAlive()
                || !ArcanumEnchantmentHelper.wearsEnchantment(player, ArcanumEnchantments.GROVEKEEPER)) {
            return;
        }
        Holder<net.minecraft.world.effect.MobEffect>[] cleanse = new Holder[] {
                MobEffects.POISON, MobEffects.WITHER, MobEffects.WEAKNESS, MobEffects.SLOWNESS,
                MobEffects.HUNGER, MobEffects.BLINDNESS, MobEffects.MINING_FATIGUE, MobEffects.NAUSEA
        };
        for (Holder<net.minecraft.world.effect.MobEffect> effect : cleanse) {
            if (player.hasEffect(effect)) {
                player.removeEffect(effect);
                break;
            }
        }
    }

    private static void pruneState() {
        clearIfEmpty(KINDLING);
        clearIfEmpty(CHILL);
    }

    private static void clearIfEmpty(Map<UUID, Map<UUID, Integer>> map) {
        Iterator<Map.Entry<UUID, Map<UUID, Integer>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Map<UUID, Integer>> entry = it.next();
            entry.getValue().values().removeIf(v -> v <= 0);
            if (entry.getValue().isEmpty()) it.remove();
        }
    }

    private static void forgetTarget(UUID targetId) {
        for (Map<UUID, Integer> inner : KINDLING.values()) inner.remove(targetId);
        for (Map<UUID, Integer> inner : CHILL.values()) inner.remove(targetId);
    }

    private static void handleAttackSide(ServerLevel level, LivingEntity attacker, ItemStack weapon,
                                         LivingEntity victim, DamageSource source) {
        boolean direct = source.isDirect();
        if (!victim.isAlive()) return;

        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.HEARTWOOD)) {
            drainLife(attacker, HEARTWOOD_LIFESTEAL, HEARTWOOD_ABSORPTION_CAP);
        }
        if (ArcanumEnchantmentHelper.wearsEnchantment(attacker, ArcanumEnchantments.SHROUD)
                && attacker.getHealth() <= attacker.getMaxHealth() * SHROUD_LOW_HEALTH_FRACTION) {
            drainLife(attacker, SHROUD_LIFESTEAL, 0.0F);
        }
        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.CINDERHEART)) {
            tickKindling(level, attacker, victim);
        }
        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.WINTERS_EDGE)) {
            tickChill(level, attacker, victim);
        }
        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.THUNDERSTEP)) {
            UUID id = attacker.getUUID();
            Integer charge = THUNDERSTEP_CHARGE.get(id);
            if (charge != null && charge > 0) {
                THUNDERSTEP_CHARGE.remove(id);
                dealAbilityDamage(level, victim, attributed(level, DamageTypes.LIGHTNING_BOLT, attacker), THUNDERSTEP_BONUS);
            }
        }
        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.SUNDER)) {
            breakShield(level, victim, weapon);
        }
        if (direct && weapon != null && ArcanumEnchantmentHelper.hasEnchantmentOnStack(weapon, ArcanumEnchantments.GRAVECALLER)
                && victim.getHealth() <= victim.getMaxHealth() * GRAVECALLER_EXECUTE_THRESHOLD) {
            dealAbilityDamage(level, victim,
                    level.damageSources().indirectMagic(attacker, attacker), GRAVECALLER_EXECUTE_BONUS);
        }
    }

    private static void tickKindling(ServerLevel level, LivingEntity attacker, LivingEntity victim) {
        UUID attackerId = attacker.getUUID();
        UUID victimId = victim.getUUID();
        int stacks = kindlingOf(attacker, victimId);
        if (stacks >= KINDLING_MAX) {
            stackFor(KINDLING, attackerId, victimId, 0);
            DamageSource fire = attributed(level, DamageTypes.IN_FIRE, attacker);
            victim.igniteForTicks(DETONATE_BURN_TICKS);
            for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                    victim.getBoundingBox().inflate(DETONATE_RADIUS),
                    e -> e != victim && e != attacker && e.isAlive() && !e.isAlliedTo(attacker))) {
                other.igniteForTicks(DETONATE_BURN_TICKS);
                dealAbilityDamage(level, other, fire, DETONATE_DAMAGE);
            }
        } else {
            stackFor(KINDLING, attackerId, victimId, stacks + 1);
        }
    }

    private static void tickChill(ServerLevel level, LivingEntity attacker, LivingEntity victim) {
        UUID attackerId = attacker.getUUID();
        UUID victimId = victim.getUUID();
        int stacks = Math.min(chillOf(attacker, victimId) + 1, CHILL_MAX);
        if (stacks >= CHILL_MAX && !victim.isFullyFrozen() && victim.canFreeze()) {
            FreezeHandler.applyFreeze(victim, 1);
        }
        if (victim.isFullyFrozen()) {
            dealAbilityDamage(level, victim, attributed(level, DamageTypes.FREEZE, attacker), CHILL_BONUS_DAMAGE);
        }
        if (!victim.isAlive()) {
            stackFor(CHILL, attackerId, victimId, 0);
        } else {
            stackFor(CHILL, attackerId, victimId, stacks);
        }
    }

    private static void handleWearerSide(ServerLevel level, LivingEntity victim, LivingEntity attacker,
                                         DamageSource source, boolean dealt) {
        if (!dealt) return;

        if (ArcanumEnchantmentHelper.wearsEnchantment(victim, ArcanumEnchantments.EMBERFORGE)
                && source.is(DamageTypeTags.IS_FIRE)) {
            UUID id = victim.getUUID();
            int charge = EMBERFORGE_CHARGE.getOrDefault(id, 0) + 1;
            if (charge >= EMBERFORGE_MAX) {
                EMBERFORGE_CHARGE.remove(id);
                attacker.igniteForTicks(EMBERFORGE_BURN_TICKS);
                victim.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 100, 0, false, false, true));
            } else {
                EMBERFORGE_CHARGE.put(id, charge);
            }
        }

        if (ArcanumEnchantmentHelper.wearsEnchantment(victim, ArcanumEnchantments.STORMWARD)
                && STORMWARD_WINDOW.containsKey(victim.getUUID())) {
            STORMWARD_WINDOW.remove(victim.getUUID());
            dealAbilityDamage(level, attacker, attributed(level, DamageTypes.LIGHTNING_BOLT, victim), STORMWARD_RETALIATION);
        }

        if (source.isDirect()
                && ArcanumEnchantmentHelper.wearsEnchantment(victim, ArcanumEnchantments.UNBROKEN)) {
            float retaliation = 1.0F + victim.getArmorValue() * 0.15F;
            dealAbilityDamage(level, attacker, level.damageSources().thorns(victim), retaliation);
        }
    }

    private static void dealAbilityDamage(ServerLevel level, LivingEntity target, DamageSource source, float amount) {
        if (target == null || !target.isAlive()) return;
        RETALIATING.set(true);
        try {
            target.hurtServer(level, source, amount);
        } finally {
            RETALIATING.set(false);
        }
    }

    private static DamageSource attributed(ServerLevel level, ResourceKey<DamageType> key, Entity culprit) {
        Holder.Reference<DamageType> holder = level.registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(key);
        return new DamageSource(holder, culprit, culprit);
    }

    private static void drainLife(LivingEntity attacker, float amount, float absorptionCap) {
        float current = attacker.getHealth();
        float max = attacker.getMaxHealth();
        float overflow = amount;
        if (current < max) {
            float healed = Math.min(amount, max - current);
            attacker.heal(healed);
            overflow -= healed;
        }
        if (overflow > 0.0F && absorptionCap > 0.0F) {
            float absorption = Math.min(attacker.getAbsorptionAmount() + overflow, absorptionCap);
            attacker.setAbsorptionAmount(absorption);
        }
    }

    private static int kindlingOf(LivingEntity attacker, UUID victimId) {
        Map<UUID, Integer> inner = KINDLING.get(attacker.getUUID());
        return inner == null ? 0 : inner.getOrDefault(victimId, 0);
    }

    private static int chillOf(LivingEntity attacker, UUID victimId) {
        if (attacker == null) {
            int best = 0;
            for (Map<UUID, Integer> inner : CHILL.values()) {
                best = Math.max(best, inner.getOrDefault(victimId, 0));
            }
            return best;
        }
        Map<UUID, Integer> inner = CHILL.get(attacker.getUUID());
        return inner == null ? 0 : inner.getOrDefault(victimId, 0);
    }

    private static void stackFor(Map<UUID, Map<UUID, Integer>> outer, UUID attackerId, UUID victimId, int value) {
        outer.computeIfAbsent(attackerId, k -> new HashMap<>()).put(victimId, value);
    }

    private static void breakShield(ServerLevel level, LivingEntity victim, ItemStack weapon) {
        if (!victim.isBlocking()) return;
        ItemStack blocking = victim.getItemBlockingWith();
        if (blocking == null || blocking.isEmpty()) return;
        BlocksAttacks blocks = blocking.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks != null) {
            blocks.disable(level, victim, 5.0F, weapon);
        }
    }
}
