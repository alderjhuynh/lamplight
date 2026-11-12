package com.aura.lamplight.arcanum.server;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("all")
public final class RebuildHandler {
    private RebuildHandler() {}

    private static final ThreadLocal<Boolean> RECURSING = ThreadLocal.withInitial(() -> false);

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(RebuildHandler::tick);
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> clearPlayer(handler.getPlayer().getUUID()));
    }

    private static final Set<UUID> PENDING_PLUNDER = new HashSet<>();
    private static final Map<UUID, Integer> PENDING_TUITION = new HashMap<>();

    public static void clearPlayer(UUID uuid) {

        endBleed(uuid, null);

        STATIC_LAST_VICTIM.remove(uuid);
        STATIC_STREAK.remove(uuid);

        FLURRY_LAST_HIT.remove(uuid);
        FLURRY_STREAK.remove(uuid);

        FIRST_BLOOD_HIT.remove(uuid);

        LAST_MELEE_HIT.remove(uuid);
        FEINT_ARMED.remove(uuid);
        DIGGING.remove(uuid);
        SWING_SCALE.remove(uuid);

        RIPOSTE_WINDOW.remove(uuid);

        RIPTIDE_AIR.remove(uuid);

        DEADEYE_SHOT.remove(uuid);
        MARKS.remove(uuid);
        CHAIN_LOAD_SHOTS.remove(uuid);
        CHAIN_LOAD_SNAPSHOT.remove(uuid);

        STORM_COIL_HITS.remove(uuid);
        STORM_COIL_CHARGES.remove(uuid);
        RAMPART_STILL.remove(uuid);
        RAMPART_POS.remove(uuid);
        SPLINTER_FREEZE.remove(uuid);
        ADRENALINE_COOLDOWN.remove(uuid);
        ADRENALINE_TRIPPED.remove(uuid);
        JOCKEY_LAST_MOUNT.remove(uuid);
    }

    public static boolean isRecursing() {
        return RECURSING.get();
    }

    public static boolean directHurt(ServerLevel level, LivingEntity victim, DamageSource source, float amount) {
        RECURSING.set(true);
        try {
            return victim.hurtServer(level, source, amount);
        } finally {
            RECURSING.set(false);
        }
    }

    public static int armorLvl(LivingEntity entity, ResourceKey<Enchantment> key) {
        int best = 0;
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            best = Math.max(best, lvl(entity.getItemBySlot(slot), key));
        }
        return best;
    }

    public static float modifyIncoming(ServerLevel level, LivingEntity victim, DamageSource source, float amount) {
        if (amount <= 0.0F) return amount;
        float reduced = amount;

        if (armorLvl(victim, ArcanumEnchantments.KILN) > 0 && victim.isOnFire()
                && source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            reduced *= 0.75F;
        }

        int severingLvl = lvl(source.getWeaponItem(), ArcanumEnchantments.SEVERING);
        if (severingLvl > 0 && source.isDirect() && victim.getArmorValue() > 0
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)) {
            reduced = severingAmount(victim, source, reduced, severingLvl);
        }
        if (victim instanceof Player player) {

            int rampartLvl = armorLvl(player, ArcanumEnchantments.RAMPART);
            if (rampartLvl > 0 && RAMPART_STILL.getOrDefault(player.getUUID(), 0) >= 40) {
                reduced *= 1.0F - 0.08F * rampartLvl;
            }

            if (armorLvl(player, ArcanumEnchantments.LAST_STAND) > 0
                    && player.getHealth() < player.getMaxHealth() * 0.4F) {
                reduced *= 0.85F;
            }

            if (armorLvl(player, ArcanumEnchantments.STICKY_LANDING) > 0
                    && source.is(net.minecraft.tags.DamageTypeTags.IS_FALL) && reduced < 4.0F) {
                reduced = 0.0F;
            }
        }
        return reduced;
    }

    private static float severingAmount(LivingEntity victim, DamageSource source, float amount, int lvl) {
        float armor = victim.getArmorValue();
        if (armor <= 0.0F) return amount;
        float reducedArmor = Math.max(0.0F, armor * (1.0F - 0.10F * lvl));
        float target = postArmorDamage(victim, source, amount, reducedArmor);
        if (target <= postArmorDamage(victim, source, amount, armor)) return amount;
        float lo = 0.0F;
        float hi = amount * 2.5F;
        for (int i = 0; i < 24; i++) {
            float mid = (lo + hi) * 0.5F;
            if (postArmorDamage(victim, source, mid, armor) < target) {
                lo = mid;
            } else {
                hi = mid;
            }
        }
        return (lo + hi) * 0.5F;
    }

    private static float postArmorDamage(LivingEntity victim, DamageSource source, float damage, float armor) {
        float toughness = (float) victim.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        return CombatRules.getDamageAfterAbsorb(victim, damage, source, armor, toughness);
    }

    public static int lvl(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack == null || stack.isEmpty()) return 0;
        ItemEnchantments enchants = ArcanumEnchantmentHelper.getEnchantments(stack);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (holder.unwrapKey().map(k -> k.equals(key)).orElse(false)) {
                return enchants.getLevel(holder);
            }
        }
        return 0;
    }

    public static void bonusDamage(ServerLevel level, LivingEntity target, LivingEntity attacker, float amount) {
        if (RECURSING.get() || target == null || !target.isAlive()) return;
        RECURSING.set(true);
        try {
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().indirectMagic(attacker, attacker), amount);
        } finally {
            RECURSING.set(false);
        }
    }

     public static void onHurt(ServerLevel level, LivingEntity victim, DamageSource source, float amount, boolean dealt) {
        if (RECURSING.get() || victim == null || !dealt) return;
        if (source.getEntity() instanceof LivingEntity attacker && attacker != victim) {
            handleAttackSide(level, attacker, source.getWeaponItem(), victim, source, amount);
        }
        handleVictimSide(level, victim, source, amount, dealt);
    }

    public static void onDeath(ServerLevel level, LivingEntity dead, DamageSource source) {
        if (RECURSING.get() || dead == null) return;
        handleDeath(level, dead, source);
    }

    private static void tick(MinecraftServer server) {
        finaleTick();
        singeTick();
        rendTick(server);
        marksTick(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() instanceof ServerLevel level) tickPerPlayer(level, player);
        }
    }

    private static LivingEntity findLiving(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) return living;
        }
        return null;
    }

    private static final int REND_DURATION = 80;
    private static final Map<UUID, Integer> BLEED = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> BLEED_BARS = new HashMap<>();

    private static void rendTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = BLEED.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            UUID victimId = entry.getKey();
            int ticks = entry.getValue() - 1;
            LivingEntity victim = findLiving(server, victimId);
            if (victim == null) {
                it.remove();
                endBleed(victimId, null);
                continue;
            }
            if (ticks <= 0) {
                bleedPulse(victim);
                it.remove();
                endBleed(victimId, null);
                continue;
            }
            entry.setValue(ticks);
            int elapsed = REND_DURATION - ticks;
            if (elapsed > 0 && elapsed % 40 == 0) {
                bleedPulse(victim);
                ServerBossEvent steppedBar = BLEED_BARS.get(victimId);
                if (steppedBar != null) steppedBar.setProgress(ticks / (float) REND_DURATION);
            }
            if (ticks % 20 == 0 && victim.level() instanceof ServerLevel level) {
                level.sendParticles(DustParticleOptions.REDSTONE, victim.getX(),
                        victim.getY(0.5D), victim.getZ(), 4, 0.25D, 0.3D, 0.25D, 0.0D);
            }
        }
    }

    private static void bleedPulse(LivingEntity victim) {
        if (!(victim.level() instanceof ServerLevel level)) return;
        bonusDamage(level, victim, victim, 1.0F);
    }

    private static void startBleed(LivingEntity victim) {
        UUID id = victim.getUUID();
        BLEED.put(id, REND_DURATION);
        if (victim instanceof ServerPlayer player && !BLEED_BARS.containsKey(id)) {
            ServerBossEvent bar = new ServerBossEvent(id,
                    Component.translatable("lamplight.rend.title"),
                    net.minecraft.world.BossEvent.BossBarColor.RED, net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS);
            bar.setProgress(1.0F);
            bar.addPlayer(player);
            BLEED_BARS.put(id, bar);
        }
    }

    private static void endBleed(UUID id, ServerLevel level) {
        BLEED.remove(id);
        ServerBossEvent bar = BLEED_BARS.remove(id);
        if (bar != null) bar.removeAllPlayers();
    }

    private static void undertow(LivingEntity attacker, LivingEntity victim) {
        if (victim.isPassenger() || victim.getBbWidth() > 1.0F) return;
        double dx = attacker.getX() - victim.getX();
        double dz = attacker.getZ() - victim.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.01D) return;
        victim.push(dx / len * 0.7D, 0.0D, dz / len * 0.7D);
        victim.hurtMarked = true;
    }

    private static void spite(ServerLevel level, LivingEntity attacker, LivingEntity victim, int lvl) {
        if (attacker.getHealth() >= attacker.getMaxHealth() * 0.5F) return;
        bonusDamage(level, victim, attacker, 1.5F * lvl);
    }

    private static final Map<UUID, Integer> RIPOSTE_WINDOW = new HashMap<>();

    private static void riposte(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        UUID id = attacker.getUUID();
        Integer marked = RIPOSTE_WINDOW.get(id);
        int now = attacker.tickCount;
        if (marked == null || now - marked > 40) {
            if (marked != null) RIPOSTE_WINDOW.remove(id);
            return;
        }
        RIPOSTE_WINDOW.remove(id);
        bonusDamage(level, victim, attacker, amount * (0.30F * lvl));
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, victim.getX(), victim.getY(1.0D), victim.getZ(),
                10, 0.3D, 0.3D, 0.3D, 0.2D);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.NOTE_BLOCK_HAT.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.2F);
    }

    private static void reap(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        double dx = victim.getX() - attacker.getX();
        double dz = victim.getZ() - attacker.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.01D) return;
        double px = victim.getX() + dx / len * 1.0D;
        double pz = victim.getZ() + dz / len * 1.0D;
        LivingEntity behind = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(1.5D),
                e -> e != victim && e != attacker && e.isAlive())) {
            double dist = candidate.distanceToSqr(px, victim.getY(0.5D), pz);
            if (dist < best) {
                best = dist;
                behind = candidate;
            }
        }
        if (behind == null) return;
        bonusDamage(level, behind, attacker, amount * 0.5F);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, behind.getX(), behind.getY(0.9D), behind.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private static final Map<UUID, Integer> SPLINTER_FREEZE = new HashMap<>();

    private static void splinter(ServerLevel level, LivingEntity attacker, LivingEntity victim) {
        if (!(victim instanceof Player blocker)) return;
        if (!blocker.isBlocking() || !(blocker.getUseItem().getItem() instanceof net.minecraft.world.item.ShieldItem)) return;
        ItemStack shield = blocker.getUseItem();
        blocker.stopUsingItem();
        blocker.getCooldowns().addCooldown(shield, 20);
        blocker.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 5, true, false, true));
        AttributeInstance move = blocker.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            Identifier freezeId = com.aura.lamplight.arcanum.Arcanum.id("splinter_freeze");
            move.addOrUpdateTransientModifier(new AttributeModifier(freezeId, -1.0D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            SPLINTER_FREEZE.put(blocker.getUUID(), blocker.tickCount + 10);
        }
        level.playSound(null, blocker.getX(), blocker.getY(), blocker.getZ(),
                SoundEvents.SHIELD_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void splinterFreezeTick(ServerPlayer player) {
        Integer expiry = SPLINTER_FREEZE.get(player.getUUID());
        if (expiry == null) return;
        if (player.tickCount < expiry) {
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            player.hurtMarked = true;
            player.setNoGravity(true);
            return;
        }
        SPLINTER_FREEZE.remove(player.getUUID());
        player.setNoGravity(false);
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        Identifier freezeId = com.aura.lamplight.arcanum.Arcanum.id("splinter_freeze");
        if (move != null && move.hasModifier(freezeId)) move.removeModifier(freezeId);
    }

    private static boolean stormbrandDamp(ServerLevel level, LivingEntity attacker, LivingEntity victim) {
        if (attacker.isInWaterOrRain() || victim.isInWaterOrRain()) return true;
        return level.isRainingAt(attacker.blockPosition()) || level.isRainingAt(victim.blockPosition());
    }

    private static final Set<UUID> RIPTIDE_AIR = new HashSet<>();

    private static void cometfallTick(ServerLevel level, ServerPlayer player) {
        int cometLvl = lvl(player.getMainHandItem(), ArcanumEnchantments.COMETFALL);
        UUID id = player.getUUID();
        if (cometLvl <= 0) {
            RIPTIDE_AIR.remove(id);
            return;
        }
        boolean spinning = player.isAutoSpinAttack();
        if (spinning && !player.onGround()) {
            RIPTIDE_AIR.add(id);
            return;
        }
        if (RIPTIDE_AIR.remove(id)) {
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(2.5D),
                    e -> e != player && e.isAlive())) {
                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0.01D) {
                    target.push(dx / len * 0.8D, 0.3D, dz / len * 0.8D);
                    target.hurtMarked = true;
                }
                bonusDamage(level, target, player, 4.0F);
            }
            slamParticles(level, player.getX(), player.getY(), player.getZ(), 4.0D, 1.6D, 1.0D);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.MACE_SMASH_GROUND, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.1F);
        }
    }

    private static final Map<UUID, Integer> LAST_MELEE_HIT = new HashMap<>();
    private static final Map<UUID, Integer> FEINT_ARMED = new HashMap<>();
    private static final Set<UUID> DIGGING = new HashSet<>();

    public static void onSwing(Player player, net.minecraft.world.InteractionHand hand) {
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        UUID id = player.getUUID();
        Integer lastHit = LAST_MELEE_HIT.get(id);
        if (lastHit != null && lastHit == player.tickCount) return;
        if (DIGGING.contains(id)) return;
        FEINT_ARMED.put(id, player.tickCount);
    }

    public static void onPlayerAction(Player player, ServerboundPlayerActionPacket.Action action) {
        UUID id = player.getUUID();
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) DIGGING.add(id);
        else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK
                || action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) DIGGING.remove(id);
    }

    private static void feint(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        UUID id = attacker.getUUID();
        Integer armed = FEINT_ARMED.get(id);
        int now = attacker.tickCount;
        if (armed == null || now - armed > 40) {
            if (armed != null) FEINT_ARMED.remove(id);
            return;
        }
        if (armed >= now) {
            FEINT_ARMED.remove(id);
            return;
        }
        FEINT_ARMED.remove(id);
        bonusDamage(level, victim, attacker, amount * 0.40F);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, victim.getX(), victim.getY(1.0D), victim.getZ(),
                10, 0.3D, 0.3D, 0.3D, 0.2D);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.NOTE_BLOCK_HAT.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.4F);
    }

    private static double heavyFallBonus(double fall) {
        if (fall <= 3.0D) return 2.0D * fall;
        if (fall <= 8.0D) return 6.0D + (fall - 3.0D);
        return 11.0D + 0.5D * (fall - 8.0D);
    }

    private static void slamParticles(ServerLevel level, double x, double y, double z, double fall,
                                  double scale, double spread) {
        BlockPos below = BlockPos.containing(x, y - 0.5D, z);
        BlockState ground = level.getBlockState(below);
        if (ground.isAir()) return;

        BlockParticleOption pillar = new BlockParticleOption(ParticleTypes.DUST_PILLAR, ground);
        RandomSource rand = level.getRandom();
        int count = (int) Mth.clamp(60.0D * fall * scale, 30.0D, 400.0D);
        double radius = 1.5D + 0.5D * Math.min(fall * scale, 4.0D);
        double gy = below.getY() + 1.0D;

        for (int i = 0; i < count / 3; i++) {
            level.sendParticles(pillar, x + rand.nextGaussian() * 0.5D, gy, z + rand.nextGaussian() * 0.5D,
                    0, rand.nextGaussian() * 0.2D, rand.nextGaussian() * 0.2D, rand.nextGaussian() * 0.2D, 1.0D);
        }

        for (int i = 0; i < count / 1.5D; i++) {
            level.sendParticles(pillar,
                    x + radius * Math.cos(i) + rand.nextGaussian() * 0.35D, gy,
                    z + radius * Math.sin(i) + rand.nextGaussian() * 0.35D,
                    0, rand.nextGaussian() * 0.05D, rand.nextGaussian() * 0.05D, rand.nextGaussian() * 0.05D, 1.0D);
        }
    }

    private static void heavy(ServerLevel level, LivingEntity attacker, LivingEntity victim, int lvl) {
        double fall = attacker.fallDistance;
        bonusDamage(level, victim, attacker, (float) heavyFallBonus(fall));
        victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10, 0, true, false, true));
       slamParticles(level, victim.getX(), victim.getY(), victim.getZ(), fall, 1.0D, 0.3D);
        level.playSound(null, victim.getX(), victim.getY(), victim.getZ(),
                SoundEvents.MACE_SMASH_GROUND, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    private static final Map<UUID, Integer> FLURRY_LAST_HIT = new HashMap<>();
    private static final Map<UUID, Integer> FLURRY_STREAK = new HashMap<>();

    private static void flurry(LivingEntity attacker, int lvl) {
        UUID id = attacker.getUUID();
        int now = attacker.tickCount;
        Integer last = FLURRY_LAST_HIT.get(id);
        int streak = (last != null && now - last <= 60) ? FLURRY_STREAK.getOrDefault(id, 0) + 1 : 1;
        boolean activating = streak >= 2 && FLURRY_STREAK.getOrDefault(id, 0) < 2;
        FLURRY_LAST_HIT.put(id, now);
        FLURRY_STREAK.put(id, streak);
        if (activating && attacker.level() instanceof ServerLevel sLevel && attacker instanceof Player player) {
            sLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY(1.2D), player.getZ(),
                    6, 0.4D, 0.3D, 0.4D, 0.4D);
        }
    }

    private static void flurryTick(ServerLevel level, Player player) {
        UUID id = player.getUUID();
        int lvl = lvl(player.getMainHandItem(), ArcanumEnchantments.FLURRY);
        AttributeInstance speed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speed == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("flurry_attack_speed");
        Integer last = FLURRY_LAST_HIT.get(id);
        int streak = FLURRY_STREAK.getOrDefault(id, 0);
        boolean active = lvl > 0 && streak >= 2 && last != null && player.tickCount - last <= 60;
        if (active) {
            if (!speed.hasModifier(modId)) {
                speed.addOrUpdateTransientModifier(new AttributeModifier(modId, 0.20D * lvl,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else if (speed.hasModifier(modId)) {
            speed.removeModifier(modId);
        }
    }

    private static final Map<UUID, Set<UUID>> FIRST_BLOOD_HIT = new HashMap<>();

    private static void firstBlood(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        Set<UUID> hit = FIRST_BLOOD_HIT.computeIfAbsent(attacker.getUUID(), k -> new HashSet<>());
        if (hit.contains(victim.getUUID())) return;
        hit.add(victim.getUUID());
        if (hit.size() > 256) hit.clear();
        bonusDamage(level, victim, attacker, 3.0F * lvl);
    }

    private static void firstBloodGlowPass(ServerLevel level, Player player) {
        int lvl = lvl(player.getMainHandItem(), ArcanumEnchantments.FIRST_BLOOD);
        if (lvl <= 0) return;
        Set<UUID> hit = FIRST_BLOOD_HIT.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(8.0D),
                e -> e != player && e.isAlive() && !hit.contains(e.getUUID()))) {
            other.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, true, false, true));
        }
    }

    private static void momentum(LivingEntity attacker, LivingEntity victim, int lvl) {
        if (victim.isPassenger()) return;
        double dx = victim.getX() - attacker.getX();
        double dz = victim.getZ() - attacker.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.01D) return;
        double push = 0.7D * lvl;
        victim.push(dx / len * push, 0.2D, dz / len * push);
        victim.hurtMarked = true;
    }

    private static void echo(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        bonusDamage(level, victim, attacker, amount * 0.5F);
        level.sendParticles(ParticleTypes.SCULK_SOUL, victim.getX(), victim.getY(0.75D), victim.getZ(),
                10, 0.3D, 0.4D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, victim.getX(), victim.getY(0.9D), victim.getZ(),
                6, 0.3D, 0.3D, 0.3D, 0.1D);
        victim.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 0.7F);
    }

    private static final Map<UUID, UUID> STATIC_LAST_VICTIM = new HashMap<>();
    private static final Map<UUID, Integer> STATIC_STREAK = new HashMap<>();

    private static void staticCharge(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        UUID attackerId = attacker.getUUID();
        UUID victimId = victim.getUUID();
        if (!victimId.equals(STATIC_LAST_VICTIM.get(attackerId))) {
            STATIC_LAST_VICTIM.put(attackerId, victimId);
            STATIC_STREAK.put(attackerId, 0);
        }
        int streak = STATIC_STREAK.merge(attackerId, 1, Integer::sum);
        if (streak < 3) return;
        STATIC_STREAK.put(attackerId, 0);

        LightningBolt bolt = new LightningBolt(net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT, victim.level());
        bolt.setVisualOnly(true);
        bolt.snapTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), 0.0F);
        victim.level().addFreshEntity(bolt);

        bonusDamageLightning(level, victim, attacker, 2.0F + 2.0F * lvl);
    }

    private static void bonusDamageLightning(ServerLevel level, LivingEntity target, LivingEntity attacker, float dmg) {
        if (RECURSING.get() || target == null || !target.isAlive()) return;
        RECURSING.set(true);
        try {
            target.invulnerableTime = 0;
            target.hurtServer(level, level.damageSources().lightningBolt(), dmg);
        } finally {
            RECURSING.set(false);
        }
    }

    public static void onSweep(Player attacker, Entity target, DamageSource source) {
        int whirlLvl = lvl(source.getWeaponItem(), ArcanumEnchantments.WHIRLWIND);
        if (whirlLvl <= 0 || !(attacker.level() instanceof ServerLevel level)) return;
        for (LivingEntity swept : level.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(0.75D),
                e -> e != target && e != attacker && e.isAlive())) {
            whirlwindSweep(level, attacker, swept, whirlLvl);
        }
    }

    private static void handleAttackSide(ServerLevel level, LivingEntity attacker, ItemStack weapon,                                         LivingEntity victim, DamageSource source, float amount) {
        if (!source.isDirect()) {
            if (source.getDirectEntity() instanceof AbstractArrow arrow
                    && source.getEntity() instanceof LivingEntity shooter && shooter != victim) {
                handleRangedHit(level, shooter, arrow, victim, amount);
            }
            return;
        }
        if (attacker instanceof Player) LAST_MELEE_HIT.put(attacker.getUUID(), attacker.tickCount);
        if (attacker instanceof Player coilAttacker) stormCoilSpend(level, coilAttacker, victim);
        int feintLvl = lvl(weapon, ArcanumEnchantments.FEINT);
        if (feintLvl > 0) feint(level, attacker, victim, amount, feintLvl);
        int heavyLvl = lvl(weapon, ArcanumEnchantments.HEAVY);
        if (heavyLvl > 0 && attacker.fallDistance > 0.0F) heavy(level, attacker, victim, heavyLvl);
        int riposteLvl = lvl(weapon, ArcanumEnchantments.RIPOSTE);
        if (riposteLvl > 0) riposte(level, attacker, victim, amount, riposteLvl);
        int reapLvl = lvl(weapon, ArcanumEnchantments.REAP);
        if (reapLvl > 0) reap(level, attacker, victim, amount, reapLvl);
        if (lvl(weapon, ArcanumEnchantments.SPLINTER) > 0) splinter(level, attacker, victim);
        int stormbrandLvl = lvl(weapon, ArcanumEnchantments.STORMBRAND);
        if (stormbrandLvl > 0 && stormbrandDamp(level, attacker, victim)) {
            bonusDamage(level, victim, attacker, 2.0F * stormbrandLvl);
        }
        int baneLvl = lvl(weapon, ArcanumEnchantments.BANE_OF_ILLAGERS);
        if (baneLvl > 0 && victim.getType().builtInRegistryHolder().is(EntityTypeTags.ILLAGER)) {
            bonusDamage(level, victim, attacker, 2.5F * baneLvl);
        }
        if (lvl(weapon, ArcanumEnchantments.RHYTHM) > 0) rhythm(level, attacker, victim, amount, lvl(weapon, ArcanumEnchantments.RHYTHM));
        if (lvl(weapon, ArcanumEnchantments.REND) > 0) startBleed(victim);
        int vultureLvl = lvl(weapon, ArcanumEnchantments.VULTURE);
        if (vultureLvl > 0 && victim.getHealth() < victim.getMaxHealth() * 0.5F) {
            bonusDamage(level, victim, attacker, amount * (0.25F * vultureLvl));
        }
        int staticLvl = lvl(weapon, ArcanumEnchantments.STATIC_CHARGE);
        if (staticLvl > 0) staticCharge(level, attacker, victim, amount, staticLvl);
        if (lvl(weapon, ArcanumEnchantments.UNDERTOW) > 0) undertow(attacker, victim);
        int flurryLvl = lvl(weapon, ArcanumEnchantments.FLURRY);
        if (flurryLvl > 0) flurry(attacker, flurryLvl);
        int firstBloodLvl = lvl(weapon, ArcanumEnchantments.FIRST_BLOOD);
        if (firstBloodLvl > 0) firstBlood(level, attacker, victim, amount, firstBloodLvl);
        int momentumLvl = lvl(weapon, ArcanumEnchantments.MOMENTUM);
        if (momentumLvl > 0 && attacker.isSprinting()) momentum(attacker, victim, momentumLvl);
        int echoLvl = lvl(weapon, ArcanumEnchantments.ECHO);
        if (echoLvl > 0 && attacker.getRandom().nextFloat() < 0.05F + 0.10F * echoLvl) {
            echo(level, attacker, victim, amount, echoLvl);
        }
        int spiteLvl = lvl(weapon, ArcanumEnchantments.SPITE);
        if (spiteLvl > 0) spite(level, attacker, victim, spiteLvl);
    }

    private static final Map<UUID, Integer> STORM_COIL_HITS = new HashMap<>();
    private static final Map<UUID, Integer> STORM_COIL_CHARGES = new HashMap<>();
    private static final DustParticleOptions COIL_DUST = new DustParticleOptions(0x55FFFF, 1.0F);

    private static void stormCoilTakeHit(ServerLevel level, Player victim, DamageSource source) {
        UUID id = victim.getUUID();
        int coilLvl = armorLvl(victim, ArcanumEnchantments.STORM_COIL);
        if (coilLvl <= 0) return;
        int hits = STORM_COIL_HITS.merge(id, 1, Integer::sum);
        if (hits >= 4) {
            STORM_COIL_HITS.put(id, 0);
            int maxCharges = 1 + coilLvl;
            int charges = STORM_COIL_CHARGES.getOrDefault(id, 0);
            if (charges < maxCharges) {
                STORM_COIL_CHARGES.put(id, charges + 1);
                victim.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.7F, 1.4F);
            }
        }
    }

    private static void stormCoilSpend(ServerLevel level, Player attacker, LivingEntity victim) {
        UUID id = attacker.getUUID();
        int charges = STORM_COIL_CHARGES.getOrDefault(id, 0);
        if (charges <= 0) return;
        STORM_COIL_CHARGES.put(id, 0);
        bonusDamage(level, victim, attacker, 1.5F * charges);

        LightningBolt bolt = new LightningBolt(net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT, victim.level());
        bolt.setVisualOnly(true);
        bolt.snapTo(victim.getX(), victim.getY(), victim.getZ(), victim.getYRot(), 0.0F);
        victim.level().addFreshEntity(bolt);
        level.sendParticles(COIL_DUST, victim.getX(), victim.getY(1.0D), victim.getZ(),
                10 + charges * 4, 0.3D, 0.3D, 0.3D, 0.05D);
        victim.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8F, 0.8F);
    }

    private static void kilnTick(Player player) {
        UUID id = player.getUUID();
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("kiln_burning");
        if (armorLvl(player, ArcanumEnchantments.KILN) > 0 && player.isOnFire()) {
            if (!attack.hasModifier(modId)) {
                attack.addOrUpdateTransientModifier(new AttributeModifier(modId, 3.0D,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (attack.hasModifier(modId)) {
            attack.removeModifier(modId);
        }
    }

    private static final Map<UUID, Integer> RAMPART_STILL = new HashMap<>();
    private record LastPos(double x, double y, double z) {}
    private static final Map<UUID, LastPos> RAMPART_POS = new HashMap<>();

    private static void rampartTick(Player player) {
        UUID id = player.getUUID();
        LastPos last = RAMPART_POS.get(id);
        LastPos now = new LastPos(player.getX(), player.getY(), player.getZ());
        RAMPART_POS.put(id, now);
        int still = RAMPART_STILL.getOrDefault(id, 0);
        boolean stationary = last != null
                && Math.abs(now.x() - last.x()) < 0.01D
                && Math.abs(now.z() - last.z()) < 0.01D;
        RAMPART_STILL.put(id, stationary ? still + 1 : 0);

        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move != null) {
            Identifier lastId = com.aura.lamplight.arcanum.Arcanum.id("last_stand_slow");
            if (armorLvl(player, ArcanumEnchantments.LAST_STAND) > 0
                    && player.getHealth() < player.getMaxHealth() * 0.4F) {
                if (!move.hasModifier(lastId)) {
                    move.addOrUpdateTransientModifier(new AttributeModifier(lastId, -0.10D,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
            } else if (move.hasModifier(lastId)) {
                move.removeModifier(lastId);
            }
        }
    }

    public static void onFallLanded(LivingEntity self, float fallDistance) {
        if (RECURSING.get() || !(self instanceof ServerPlayer player)) return;
        if (armorLvl(player, ArcanumEnchantments.STICKY_LANDING) <= 0) return;
        if (fallDistance <= 1.0F) return;
        player.push(0.0D, 0.35D, 0.0D);
        player.hurtMarked = true;
    }

    private static void magnetismTick(ServerLevel level, ServerPlayer player) {
        int magnetLvl = lvl(player.getMainHandItem(), ArcanumEnchantments.MAGNETISM);
        if (magnetLvl <= 0) return;
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                player.getBoundingBox().inflate(1.0D + magnetLvl))) {
            if (!item.isAlive() || item.hasPickUpDelay()) continue;
            double dx = player.getX() - item.getX();
            double dy = player.getY(0.5D) - item.getY();
            double dz = player.getZ() - item.getZ();
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.3D) continue;
            item.push(dx / len * 0.08D, dy / len * 0.05D, dz / len * 0.08D);
        }
    }

    private static void whirlwindReachTick(ServerPlayer player) {
        AttributeInstance reach = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (reach == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("whirlwind_reach");
        if (lvl(player.getMainHandItem(), ArcanumEnchantments.WHIRLWIND) > 0) {
            if (!reach.hasModifier(modId)) {
                reach.addOrUpdateTransientModifier(new AttributeModifier(modId, 0.3D,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (reach.hasModifier(modId)) {
            reach.removeModifier(modId);
        }
    }

    private static void sureFootedTick(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        AttributeInstance friction = player.getAttribute(Attributes.FRICTION_MODIFIER);
        if (friction == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("sure_footed_grip");
        float blockFriction = level.getBlockState(player.blockPosition().below())
                .getBlock().getFriction();
        boolean onIce = player.onGround() && blockFriction > 0.6F
                && armorLvl(player, ArcanumEnchantments.SURE_FOOTED) > 0;
        if (onIce) {

            double m = 0.4D / Math.max(0.02D, 1.0D - blockFriction) - 1.0D;
            friction.addOrUpdateTransientModifier(new AttributeModifier(modId, Math.min(m, 20.0D),
                    AttributeModifier.Operation.ADD_VALUE));
        } else if (friction.hasModifier(modId)) {
            friction.removeModifier(modId);
        }
    }

    private static final ThreadLocal<BlockState> LAMPLIGHT_BREAK_STATE = new ThreadLocal<>();

    public static void onBlockBreak(ServerPlayer player, BlockPos pos) {
        if (player.level() instanceof ServerLevel level) {
            LAMPLIGHT_BREAK_STATE.set(level.getBlockState(pos));
        }
    }

    public static void afterBlockBreak(ServerPlayer player, BlockPos pos, boolean destroyed) {
        BlockState broken = LAMPLIGHT_BREAK_STATE.get();
        LAMPLIGHT_BREAK_STATE.remove();
        if (!destroyed || broken == null || !(player.level() instanceof ServerLevel level)) return;
        ItemStack tool = player.getMainHandItem();

        if (lvl(tool, ArcanumEnchantments.FELLER) > 0 && broken.is(BlockTags.LOGS)) {
            BlockPos above = pos.above();
            for (int i = 0; i < 24; i++) {
                if (!level.getBlockState(above).is(broken.getBlock())) break;
                level.destroyBlock(above, true, player, 2);
                above = above.above();
            }
        }

        if (lvl(tool, ArcanumEnchantments.TRENCHER) > 0 && player.isShiftKeyDown()
                && (broken.is(BlockTags.DIRT) || broken.is(BlockTags.SAND))) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos np = pos.offset(dx, 0, dz);
                    if (level.getBlockState(np).is(broken.getBlock())) {
                        level.destroyBlock(np, true, player, 2);
                    }
                }
            }
        }

        if (lvl(tool, ArcanumEnchantments.REPLANTER) > 0
                && broken.getBlock() instanceof net.minecraft.world.level.block.CropBlock crop
                && crop.isMaxAge(broken)) {
            net.minecraft.world.item.Item seed = cropSeed(crop);
            if (seed != null) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.getItem() == seed) {
                        if (!player.getAbilities().instabuild) stack.shrink(1);
                        level.setBlock(pos, crop.getStateForAge(0), 3);
                        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                                SoundEvents.CROP_PLANTED, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }
    }

    private static net.minecraft.world.item.Item cropSeed(net.minecraft.world.level.block.CropBlock crop) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(crop).getPath();
        String seedPath = switch (path) {
            case "wheat" -> "wheat_seeds";
            case "carrots" -> "carrot";
            case "potatoes" -> "potato";
            case "beetroots" -> "beetroot_seeds";
            default -> null;
        };
        if (seedPath == null) return null;
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getOptional(Identifier.withDefaultNamespace(seedPath)).orElse(null);
    }

    private static boolean isOre(BlockState state) {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock())
                .getPath().contains("ore");
    }

    public static void onBlockDrop(ServerLevel level, Player player, BlockPos pos, BlockState state,
                                   ItemStack tool, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (RECURSING.get()) return;

        if (lvl(tool, ArcanumEnchantments.KILN_HANDS) > 0 && isOre(state)
                && player.getRandom().nextFloat() < 0.70F) {
            ItemStack input = new ItemStack(state.getBlock().asItem());
            ItemStack smelted = level.getServer().getRecipeManager()
                    .getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING,
                            new net.minecraft.world.item.crafting.SingleRecipeInput(input), level)
                    .map(holder -> holder.value().assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(input)))
                    .orElse(null);
            if (smelted != null && !smelted.isEmpty()) {
                ci.cancel();
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, smelted));

                level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                        5, 0.25D, 0.25D, 0.25D, 0.01D);
                level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                        3, 0.2D, 0.2D, 0.2D, 0.01D);
                level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.FURNACE_FIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 1.0F);
            }
        }
    }

    public static void afterBlockDrop(ServerLevel level, Player player, BlockPos pos, BlockState state,
                                      ItemStack tool, net.minecraft.world.level.block.entity.BlockEntity blockEntity,
                                      Entity entity) {
        if (RECURSING.get()) return;

        if (lvl(tool, ArcanumEnchantments.BLOOM) > 0 && state.is(BlockTags.LEAVES)
                && player.getRandom().nextFloat() < 0.25F) {
            ItemStack extra = leafBonus(state);
            if (!extra.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, extra));
            }
        }

        if (lvl(tool, ArcanumEnchantments.GLEANER) > 0
                && (state.is(BlockTags.LEAVES) || state.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS)
                        || state.is(net.minecraft.world.level.block.Blocks.TALL_GRASS))
                && player.getRandom().nextFloat() < 0.25F * lvl(tool, ArcanumEnchantments.GLEANER)) {
            RECURSING.set(true);
            try {
                net.minecraft.world.level.block.Block.dropResources(state, level, pos, blockEntity, entity, tool);
            } finally {
                RECURSING.set(false);
            }
        }
    }

    private static ItemStack leafBonus(BlockState state) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        String base = path.endsWith("_leaves") ? path.substring(0, path.length() - "_leaves".length()) : path;
        if (base.equals("mangrove")) base = "mangrove_propagule";
        else if (base.equals("azalea") || base.equals("flowering_azalea")) base = "azalea";
        else base = base + "_sapling";
        Identifier saplingId = Identifier.fromNamespaceAndPath("minecraft", base);
        net.minecraft.world.item.Item sapling = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getOptional(saplingId)
                .orElse(net.minecraft.world.item.Items.STICK);
        return new ItemStack(sapling);
    }

    private static final ThreadLocal<Integer> BONE_MEAL_COUNT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> BONE_MEAL_SAVE = new ThreadLocal<>();

    public static void boneMealHead(ItemStack stack) {
        BONE_MEAL_COUNT.set(stack.getCount());
        BONE_MEAL_SAVE.set(lvl(stack, ArcanumEnchantments.TILLER) > 0
                && java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.20F);
    }

    public static void boneMealTail(ItemStack stack) {
        Integer before = BONE_MEAL_COUNT.get();
        Boolean save = BONE_MEAL_SAVE.get();
        BONE_MEAL_COUNT.remove();
        BONE_MEAL_SAVE.remove();
        if (before == null || save == null || !save) return;
        if (stack.getCount() == before - 1) stack.grow(1);
    }

    private static void handleVictimSide(ServerLevel level, LivingEntity victim, DamageSource source, float amount, boolean dealt) {

        if (victim instanceof Player) RIPOSTE_WINDOW.put(victim.getUUID(), victim.tickCount);

        Integer markExpiry = MARKS.get(victim.getUUID());
        if (markExpiry != null && markExpiry >= victim.tickCount) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity l ? l : victim;
            bonusDamage(level, victim, attacker, amount * 0.20F);
        }

        if (dealt && victim instanceof Player coilVictim) stormCoilTakeHit(level, coilVictim, source);
    }

    private static void handleDeath(ServerLevel level, LivingEntity dead, DamageSource source) {
        if (source.getEntity() instanceof LivingEntity killer) {
            ItemStack weapon = source.getWeaponItem();

            int siphonLvl = lvl(weapon, ArcanumEnchantments.SIPHON);
            if (siphonLvl > 0) killer.heal(1.0F * siphonLvl);

            if (lvl(weapon, ArcanumEnchantments.FANFARE) > 0) {
                killer.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false, true));
            }

            if (lvl(weapon, ArcanumEnchantments.BULWARK) > 0) {
                killer.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 0, true, false, true));
            }

            if (lvl(weapon, ArcanumEnchantments.FINALE) > 0) finale(level, dead);

            if (lvl(weapon, ArcanumEnchantments.GRAVEDIGGER) > 0
                    && killer.getRandom().nextFloat() < 0.10F) gravedigger(level, dead);

            int plunderLvl = lvl(weapon, ArcanumEnchantments.PLUNDER);
            if (plunderLvl > 0 && killer.getRandom().nextFloat() < 0.25F * plunderLvl) {
                PENDING_PLUNDER.add(dead.getUUID());
            }

            int tuitionLvl = lvl(weapon, ArcanumEnchantments.TUITION);
            if (tuitionLvl > 0) PENDING_TUITION.put(dead.getUUID(), tuitionLvl);
        }
    }

    private static final Map<UUID, Integer> CHAIN_LOAD_SHOTS = new HashMap<>();
    private static final Map<UUID, Object> CHAIN_LOAD_SNAPSHOT = new HashMap<>();

    public static void onCrossbowUse(Player player, ItemStack stack) {
        if (!CrossbowItem.isCharged(stack)) CHAIN_LOAD_SHOTS.remove(player.getUUID());
    }

    @SuppressWarnings("unchecked")
    public static void onCrossbowReleaseHead(Player player, ItemStack stack) {
        if (lvl(stack, ArcanumEnchantments.CHAIN_LOAD) <= 0 || !CrossbowItem.isCharged(stack)) return;
        Object projectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (projectiles instanceof net.minecraft.world.item.component.ChargedProjectiles charged && !charged.isEmpty()) {
            CHAIN_LOAD_SNAPSHOT.put(player.getUUID(), charged);
        }
    }

    public static void onCrossbowReleaseTail(Player player, ItemStack stack) {
        Object snapshot = CHAIN_LOAD_SNAPSHOT.remove(player.getUUID());
        if (snapshot == null) return;
        if (lvl(stack, ArcanumEnchantments.CHAIN_LOAD) <= 0 || CrossbowItem.isCharged(stack)) return;
        int shots = CHAIN_LOAD_SHOTS.getOrDefault(player.getUUID(), 0);
        if (shots == 0) {
            stack.set(DataComponents.CHARGED_PROJECTILES,
                    (net.minecraft.world.item.component.ChargedProjectiles) snapshot);
            CHAIN_LOAD_SHOTS.put(player.getUUID(), 1);
            if (player.level() instanceof ServerLevel sLevel) {
                sLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CROSSBOW_LOADING_END.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.2F);
            }
        } else {
            CHAIN_LOAD_SHOTS.put(player.getUUID(), 0);
        }
    }

    private static void kitingTick(ServerPlayer player) {
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("kiting_charge");
        ItemStack useStack = player.getUseItem();
        boolean charging = player.isUsingItem()
                && useStack.getItem() instanceof CrossbowItem
                && !CrossbowItem.isCharged(useStack);
        int kitingLvl = lvl(useStack, ArcanumEnchantments.KITING);
        if (kitingLvl > 0 && charging) {
            if (!move.hasModifier(modId)) {
                move.addOrUpdateTransientModifier(new AttributeModifier(modId, 0.75D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else if (move.hasModifier(modId)) {
            move.removeModifier(modId);
        }
    }

    private static final Map<UUID, Set<UUID>> DEADEYE_SHOT = new HashMap<>();
    private static final Map<UUID, Integer> MARKS = new HashMap<>();
    private static final List<SingeFire> SINGE_FIRES = new ArrayList<>();

    private record SingeFire(ServerLevel level, BlockPos pos, long expire) {}

    public static void onArrowLaunch(ServerLevel level, AbstractArrow arrow) {
        int steadyLvl = lvl(arrow.getWeaponItem(), ArcanumEnchantments.STEADY_AIM);
        if (steadyLvl > 0) {
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(1.0D + 0.20D * steadyLvl));
        }
    }

    public static void onArrowHitBlock(ServerLevel level, AbstractArrow arrow, BlockPos pos) {
        if (RECURSING.get()) return;
        LivingEntity shooter = arrow.getOwner() instanceof LivingEntity l ? l : null;
        if (shooter == null) return;
        ItemStack weapon = arrow.getWeaponItem();

        int scavengerLvl = lvl(weapon, ArcanumEnchantments.SCAVENGER);
        if (scavengerLvl > 0 && shooter.getRandom().nextFloat() < 0.20F * scavengerLvl) {
            level.addFreshEntity(new ItemEntity(level, arrow.getX(), arrow.getY(), arrow.getZ(),
                    new ItemStack(net.minecraft.world.item.Items.ARROW)));
        }

        if (lvl(weapon, ArcanumEnchantments.SMOKE_VEIL) > 0) smokeVeil(level, shooter, arrow);

        if (lvl(weapon, ArcanumEnchantments.SINGE) > 0) singe(level, arrow, pos);
    }

    private static void handleRangedHit(ServerLevel level, LivingEntity shooter, AbstractArrow arrow,
                                        LivingEntity victim, float amount) {
        if (RECURSING.get() || !victim.isAlive()) return;
        ItemStack weapon = arrow.getWeaponItem();

        int tailwindLvl = lvl(weapon, ArcanumEnchantments.TAILWIND);
        if (tailwindLvl > 0 && (!shooter.onGround() || shooter.fallDistance > 0.0F)) {
            bonusDamage(level, victim, shooter, amount * 0.15F);
        }

        int heavyDrawLvl = lvl(weapon, ArcanumEnchantments.HEAVY_DRAW);
        if (heavyDrawLvl > 0 && (arrow.isCritArrow() || weapon.getItem() instanceof CrossbowItem)) {
            bonusDamage(level, victim, shooter, 2.0F * heavyDrawLvl);
        }

        int deadeyeLvl = lvl(weapon, ArcanumEnchantments.DEADEYE);
        if (deadeyeLvl > 0) {
            Set<UUID> shot = DEADEYE_SHOT.computeIfAbsent(shooter.getUUID(), k -> new HashSet<>());
            if (shot.add(victim.getUUID())) {
                bonusDamage(level, victim, shooter, amount * 0.5F);
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY(1.0D), victim.getZ(),
                        8, 0.3D, 0.3D, 0.3D, 0.2D);
            }
        }

        if (lvl(weapon, ArcanumEnchantments.RICOCHET) > 0 && shooter.getRandom().nextFloat() < 0.5F) {
            ricochet(level, shooter, victim, amount);
        }

        int knockshotLvl = lvl(weapon, ArcanumEnchantments.KNOCKSHOT);
        if (knockshotLvl > 0) knockshot(level, victim, knockshotLvl);

        if (lvl(weapon, ArcanumEnchantments.MARKED_QUARRY) > 0) {
            boolean alreadyMarked = MARKS.containsKey(victim.getUUID())
                    && MARKS.get(victim.getUUID()) > victim.tickCount;
            MARKS.put(victim.getUUID(), victim.tickCount + 200);
            if (!alreadyMarked && victim.level() instanceof ServerLevel markLevel) {
                markLevel.sendParticles(new DustParticleOptions(0x9B59B6, 1.0F),
                        victim.getX(), victim.getY(1.0D), victim.getZ(),
                        10, 0.25D, 0.4D, 0.25D, 0.02D);
                markLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        victim.getX(), victim.getY(1.2D), victim.getZ(),
                        4, 0.2D, 0.3D, 0.2D, 0.05D);
                victim.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6F, 0.7F);
            }
        }
    }

    private static void ricochet(ServerLevel level, LivingEntity shooter, LivingEntity victim, float amount) {
        LivingEntity next = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(4.0D),
                e -> e != victim && e != shooter && e.isAlive())) {
            double dist = candidate.distanceToSqr(victim);
            if (dist < best) {
                best = dist;
                next = candidate;
            }
        }
        if (next == null) return;
        bonusDamage(level, next, shooter, amount);
        level.sendParticles(ParticleTypes.CRIT, next.getX(), next.getY(0.9D), next.getZ(),
                6, 0.2D, 0.2D, 0.2D, 0.2D);
    }

    private static void knockshot(ServerLevel level, LivingEntity victim, int lvl) {
        List<LivingEntity> around = level.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(2.5D), e -> e != victim && e.isAlive());
        for (LivingEntity pushed : around) {
            double dx = pushed.getX() - victim.getX();
            double dz = pushed.getZ() - victim.getZ();
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.01D) continue;
            pushed.push(dx / len * (0.7D * lvl), 0.25D, dz / len * (0.7D * lvl));
            pushed.hurtMarked = true;
        }
        level.sendParticles(ParticleTypes.SMALL_GUST, victim.getX(), victim.getY(0.5D), victim.getZ(),
                6, 0.8D, 0.2D, 0.8D, 0.0D);
    }

    private static void smokeVeil(ServerLevel level, LivingEntity shooter, AbstractArrow arrow) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, arrow.getX(), arrow.getY(), arrow.getZ());
        cloud.setOwner(shooter);
        cloud.setRadius(2.5F);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(0.0F);
        cloud.setDuration(80);
        cloud.setCustomParticle(ParticleTypes.SMOKE);
        cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        level.addFreshEntity(cloud);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, arrow.getX(), arrow.getY() + 0.5D, arrow.getZ(),
                12, 0.5D, 0.3D, 0.5D, 0.02D);
    }

    private static void singe(ServerLevel level, AbstractArrow arrow, BlockPos pos) {
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir()) return;
        level.setBlockAndUpdate(above, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
        level.sendParticles(ParticleTypes.FLAME, above.getX() + 0.5D, above.getY() + 0.5D, above.getZ() + 0.5D,
                8, 0.3D, 0.3D, 0.3D, 0.01D);
        SINGE_FIRES.add(new SingeFire(level, above, level.getGameTime() + 40L));
    }

    private static void singeTick() {
        Iterator<SingeFire> it = SINGE_FIRES.iterator();
        while (it.hasNext()) {
            SingeFire fire = it.next();
            if (fire.level().getGameTime() < fire.expire()) continue;
            it.remove();
            if (fire.level().getBlockState(fire.pos()).is(net.minecraft.world.level.block.Blocks.FIRE)) {
                fire.level().setBlockAndUpdate(fire.pos(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void marksTick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = MARKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            LivingEntity marked = findLiving(server, entry.getKey());
            if (marked == null || marked.tickCount > entry.getValue()) {
                it.remove();
                continue;
            }
            if (marked.tickCount % 20 == 0 && marked.level() instanceof ServerLevel level) {
                level.sendParticles(new DustParticleOptions(0x9B59B6, 1.0F),
                        marked.getX(), marked.getY(1.0D), marked.getZ(), 3, 0.2D, 0.3D, 0.2D, 0.0D);
            }
        }
    }

    private static void lamplightTick(ServerPlayer player) {
        if (armorLvl(player, ArcanumEnchantments.LAMPLIGHT) <= 0) return;
        if (player.hasEffect(MobEffects.DARKNESS)) player.removeEffect(MobEffects.DARKNESS);
        boolean dark = player.level() instanceof ServerLevel pLevel
                && pLevel.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,
                player.blockPosition()) < 5;
        if (dark && !player.hasEffect(MobEffects.NIGHT_VISION)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
        }
    }

    private static void deepGazeTick(ServerPlayer player) {
        int lvl = armorLvl(player, ArcanumEnchantments.DEEP_GAZE);
        boolean submerged = player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER);
        if (lvl > 0 && submerged) {
            if (!player.hasEffect(MobEffects.NIGHT_VISION)) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, true, false, true));
            }
        }
    }

    private static final Map<UUID, Integer> ADRENALINE_COOLDOWN = new HashMap<>();
    private static final Map<UUID, Boolean> ADRENALINE_TRIPPED = new HashMap<>();

    private static void adrenalineTick(ServerPlayer player) {
        UUID id = player.getUUID();
        int lvl = armorLvl(player, ArcanumEnchantments.ADRENALINE);
        boolean below = lvl > 0 && player.getHealth() < player.getMaxHealth() * 0.5F;
        boolean tripped = ADRENALINE_TRIPPED.getOrDefault(id, false);
        if (below && !tripped) {
            ADRENALINE_TRIPPED.put(id, true);
            int cooldownUntil = ADRENALINE_COOLDOWN.getOrDefault(id, 0);
            if (player.tickCount >= cooldownUntil) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 0, true, false, true));
                ADRENALINE_COOLDOWN.put(id, player.tickCount + 600);
                if (player.level() instanceof ServerLevel pLevel) {
                    pLevel.sendParticles(ParticleTypes.ENCHANT,
                            player.getX(), player.getY(1.1D), player.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.4D);
                }
            }
        } else if (!below && tripped) {
            ADRENALINE_TRIPPED.put(id, false);
        }
    }

    private static void pilgrimTick(ServerPlayer player) {
        UUID id = player.getUUID();
        AttributeInstance move = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (move == null) return;
        Identifier modId = com.aura.lamplight.arcanum.Arcanum.id("pilgrim_path");
        boolean onPath = player.onGround()
                && player.level().getBlockState(player.blockPosition().below()).is(net.minecraft.world.level.block.Blocks.DIRT_PATH);
        int pilgrimLvl = armorLvl(player, ArcanumEnchantments.PILGRIM);
        if (pilgrimLvl > 0 && onPath) {
            if (!move.hasModifier(modId)) {
                move.addOrUpdateTransientModifier(new AttributeModifier(modId, 0.15D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else if (move.hasModifier(modId)) {
            move.removeModifier(modId);
        }
    }

    private static final Map<UUID, UUID> JOCKEY_LAST_MOUNT = new HashMap<>();

    private static void jockeyTick(ServerPlayer player) {
        UUID id = player.getUUID();
        AttributeInstance mountMove = null;
        AttributeInstance mountJump = null;
        net.minecraft.world.entity.Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity mount) {
            mountMove = mount.getAttribute(Attributes.MOVEMENT_SPEED);
            mountJump = mount.getAttribute(Attributes.JUMP_STRENGTH);
        }
        Identifier moveId = com.aura.lamplight.arcanum.Arcanum.id("jockey_speed");
        Identifier jumpId = com.aura.lamplight.arcanum.Arcanum.id("jockey_jump");

        UUID lastMount = JOCKEY_LAST_MOUNT.get(id);
        if (lastMount != null && (vehicle == null || !vehicle.getUUID().equals(lastMount))) {
            net.minecraft.world.entity.Entity previous = player.level() instanceof ServerLevel pLevel
                    ? pLevel.getEntity(lastMount) : null;
            if (previous instanceof LivingEntity oldMount) {
                removeTransient(oldMount, Attributes.MOVEMENT_SPEED, moveId);
                removeTransient(oldMount, Attributes.JUMP_STRENGTH, jumpId);
            }
            JOCKEY_LAST_MOUNT.remove(id);
        }
        if (armorLvl(player, ArcanumEnchantments.JOCKEY) <= 0 || mountMove == null) return;
        JOCKEY_LAST_MOUNT.put(id, vehicle.getUUID());
        if (!mountMove.hasModifier(moveId)) {
            mountMove.addOrUpdateTransientModifier(new AttributeModifier(moveId, 0.15D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (mountJump != null && !mountJump.hasModifier(jumpId)) {
            mountJump.addOrUpdateTransientModifier(new AttributeModifier(jumpId, 0.1D,
                    AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeTransient(LivingEntity entity, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, Identifier modId) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && instance.hasModifier(modId)) instance.removeModifier(modId);
    }

    private static void verdantPulseTick(ServerLevel level, ServerPlayer player) {
        if (player.tickCount % 5 != 0) return;
        if (armorLvl(player, ArcanumEnchantments.VERDANT_PULSE) <= 0) return;

            if (player.getDeltaMovement().horizontalDistanceSqr() < 0.01D) return;
        BlockPos center = player.blockPosition();
        net.minecraft.util.RandomSource random = level.getRandom();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
            if (!state.is(BlockTags.CROPS)) continue;

            if (random.nextFloat() < 0.15F) {
                state.randomTick(level, pos, random);
            }
        }
    }

    private static final int FINALE_RING_TICKS = 10;
    private record FlameRing(ServerLevel level, double x, double y, double z, int age) {}
    private static final List<FlameRing> FINALE_RINGS = new ArrayList<>();

    private static void finale(ServerLevel level, LivingEntity dead) {
        double x = dead.getX(), y = dead.getY(0.5D), z = dead.getZ();
        for (LivingEntity neighbor : level.getEntitiesOfClass(LivingEntity.class,
                dead.getBoundingBox().inflate(2.0D),
                e -> e != dead && e.isAlive())) {
            neighbor.igniteForSeconds(2.0F);
        }
        FINALE_RINGS.add(new FlameRing(level, x, y, z, 0));
    }

    private static void finaleTick() {
        if (FINALE_RINGS.isEmpty()) return;

        List<FlameRing> next = new ArrayList<>(FINALE_RINGS.size());
        for (FlameRing ring : FINALE_RINGS) {
            int age = ring.age() + 1;
            if (age > FINALE_RING_TICKS) continue;
            next.add(new FlameRing(ring.level(), ring.x(), ring.y(), ring.z(), age));

            double radius = 0.4D + 2.0D * (age / (double) FINALE_RING_TICKS);
            int points = 14;
            for (int i = 0; i < points; i++) {
                double angle = (i / (double) points) * Math.PI * 2.0D + age * 0.2D;
                ring.level().sendParticles(ParticleTypes.FLAME,
                        ring.x() + Math.cos(angle) * radius, ring.y() + 0.1D,
                        ring.z() + Math.sin(angle) * radius,
                        1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
            if (age == 1) {
                ring.level().sendParticles(ParticleTypes.LAVA,
                        ring.x(), ring.y(), ring.z(), 6, 0.4D, 0.2D, 0.4D, 0.0D);
            }
        }
        FINALE_RINGS.clear();
        FINALE_RINGS.addAll(next);
    }

    private static void gravedigger(ServerLevel level, LivingEntity dead) {
        ItemStack head = null;
        if (dead.getType() == net.minecraft.world.entity.EntityTypes.WITHER_SKELETON) {
            head = new ItemStack(net.minecraft.world.item.Items.WITHER_SKELETON_SKULL);
        } else if (dead.getType() == net.minecraft.world.entity.EntityTypes.ZOMBIE) {
            head = new ItemStack(net.minecraft.world.item.Items.ZOMBIE_HEAD);
        } else if (dead.getType() == net.minecraft.world.entity.EntityTypes.SKELETON) {
            head = new ItemStack(net.minecraft.world.item.Items.SKELETON_SKULL);
        } else if (dead.getType() == net.minecraft.world.entity.EntityTypes.CREEPER) {
            head = new ItemStack(net.minecraft.world.item.Items.CREEPER_HEAD);
        } else if (dead.getType() == net.minecraft.world.entity.EntityTypes.PIGLIN) {
            head = new ItemStack(net.minecraft.world.item.Items.PIGLIN_HEAD);
        } else if (dead.getType() == net.minecraft.world.entity.EntityTypes.PIGLIN_BRUTE) {
            head = new ItemStack(net.minecraft.world.item.Items.PIGLIN_HEAD);
        } else if (dead instanceof ServerPlayer player) {
            head = new ItemStack(net.minecraft.world.item.Items.PLAYER_HEAD);
            head.set(net.minecraft.core.component.DataComponents.PROFILE,
                    net.minecraft.world.item.component.ResolvableProfile.createResolved(player.getGameProfile()));
        }
        if (head != null) dead.spawnAtLocation(level, head);
    }

    public static boolean consumePlunder(UUID deadId) {
        return PENDING_PLUNDER.remove(deadId);
    }

    public static int consumeTuition(UUID deadId) {
        Integer lvl = PENDING_TUITION.remove(deadId);
        return lvl == null ? 0 : lvl;
    }

    private static void tickPerPlayer(ServerLevel level, ServerPlayer player) {

        if (ArcanumEnchantmentHelper.wearsEnchantment(player, ArcanumEnchantments.SPITE)
                && player.getHealth() < player.getMaxHealth() * 0.5F) {
            level.sendParticles(DustParticleOptions.REDSTONE, player.getX(), player.getY(0.75D),
                    player.getZ(), 3, 0.3D, 0.3D, 0.3D, 0.0D);
        }

        flurryTick(level, player);

        firstBloodGlowPass(level, player);

        cometfallTick(level, player);

        kitingTick(player);

        whirlwindReachTick(player);

        sureFootedTick(player);

        kilnTick(player);
        rampartTick(player);
        splinterFreezeTick(player);

        int coilCharges = STORM_COIL_CHARGES.getOrDefault(player.getUUID(), 0);
        if (coilCharges > 0 && player.tickCount % 10 == 0) {
            level.sendParticles(COIL_DUST, player.getX(), player.getY(2.2D), player.getZ(),
                    coilCharges * 4, 0.3D, 0.0D, 0.3D, 0.0D);
        }

        lamplightTick(player);
        deepGazeTick(player);
        adrenalineTick(player);
        pilgrimTick(player);
        jockeyTick(player);
        verdantPulseTick(level, player);

        magnetismTick(level, player);
    }

    private static void whirlwindSweep(ServerLevel level, Player attacker, LivingEntity swept, int lvl) {
        if (swept.isPassenger()) return;
        double dx = swept.getX() - attacker.getX();
        double dz = swept.getZ() - attacker.getZ();
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.01D) return;
        double push = 0.6D * lvl;
        swept.push(dx / len * push, 0.15D, dz / len * push);
        swept.hurtMarked = true;
    }

    private static final Map<UUID, Float> SWING_SCALE = new HashMap<>();

    public static void onSwingScale(Player player, float scale) {
        SWING_SCALE.put(player.getUUID(), scale);
    }

    private static void rhythm(ServerLevel level, LivingEntity attacker, LivingEntity victim, float amount, int lvl) {
        if (!(attacker instanceof Player player)) return;

        Float scale = SWING_SCALE.get(player.getUUID());
        if (scale == null || scale < 0.999F) return;
        bonusDamage(level, victim, attacker, amount * (0.2F * lvl));
    }
}