package com.aura.lamplight.puppeteer;

import com.aura.lamplight.puppeteer.animation.AnimationDefinition;
import com.aura.lamplight.puppeteer.animation.AnimationDefinitions;
import com.aura.lamplight.puppeteer.animation.AnimationDefinition.EventKind;
import com.aura.lamplight.puppeteer.network.AnimationStatePayload;
import com.aura.lamplight.puppeteer.network.PlayerActionPayload;
import com.aura.lamplight.puppeteer.trigger.HeldItemGroupCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class PlayerAnimationTracker {
    private PlayerAnimationTracker() {}

    public record Layer(Identifier animationId, long startGameTime) {}

    private record EquipSnapshot(Item main, Item off, Item helmet, Item chest, Item legs, Item feet) {}

    private record State(
        List<Layer> continuous,
        List<Layer> overlays,
        List<Layer> lastBroadcast,
        float broadcastedRate,
        float measuredSpeed,
        float gateSpeed,
        float lastInstant,
        float previousInstant,
        long nextAmbientAt,
        double lastX,
        double lastZ
    ) {}

    private static final ConcurrentHashMap<UUID, State> STATES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Map<String, Long>> LAST_EVENT_AT = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> WAS_IN_WATER = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, EquipSnapshot> EQUIP_SNAPSHOTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Identifier> SIT_ROLL = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> ATTACK_PARITY = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<UUID, Identifier> LAST_CRAWL = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> CRAWL_HOLDING = new ConcurrentHashMap<>();

    private static final Set<Identifier> SIT_FAMILY = Set.of(
        Puppeteer.id("sit"), Puppeteer.id("sit2"), Puppeteer.id("sit3"));
    private static final Random RANDOM = new Random();

    private static final float SPEED_SMOOTHING = 0.35F;
    private static final float GATE_SMOOTHING = 0.8F;
    private static final float RATE_BROADCAST_DELTA = 0.05F;
    private static final int EVENT_COOLDOWN_TICKS = 10;

    public static void initialize() {
        PayloadTypeRegistry.serverboundPlay().register(PlayerActionPayload.TYPE, PlayerActionPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlayerActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleClientAction(player, payload.action()));
        });
        ServerTickEvents.END_SERVER_TICK.register(PlayerAnimationTracker::onServerTick);
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> {
                STATES.remove(handler.getPlayer().getUUID());
                LAST_EVENT_AT.remove(handler.getPlayer().getUUID());
                WAS_IN_WATER.remove(handler.getPlayer().getUUID());
                EQUIP_SNAPSHOTS.remove(handler.getPlayer().getUUID());
                SIT_ROLL.remove(handler.getPlayer().getUUID());
                LAST_CRAWL.remove(handler.getPlayer().getUUID());
                CRAWL_HOLDING.remove(handler.getPlayer().getUUID());
                ATTACK_PARITY.remove(handler.getPlayer().getUUID());
            }
        );
        UseBlockCallback.EVENT.register(PlayerAnimationTracker::onUseBlock);
        UseItemCallback.EVENT.register(PlayerAnimationTracker::onUseItem);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(PlayerAnimationTracker::onDamaged);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            AnimationCommand.register(dispatcher)
        );
    }

    private static void handleClientAction(final ServerPlayer player, final String action) {
        switch (action) {
            case "jump" -> playEvent(player, EventKind.JUMP);
            case "land" -> playEvent(player, EventKind.LAND);
            case "attack" -> {
                ATTACK_PARITY.put(player.getUUID(), !attackParity(player));
                playEvent(player, EventKind.ATTACK);
            }
            case "attack_hold" -> playEvent(player, EventKind.ATTACK_HOLD);
            case "swap_offhand" -> {
                playEvent(player, EventKind.SWAP, Puppeteer.id("item_switch"));
            }
            default -> { }
        }
    }

    public static boolean attackParity(final ServerPlayer player) {
        return ATTACK_PARITY.getOrDefault(player.getUUID(), true);
    }

    private static void onDamaged(final LivingEntity entity, final DamageSource source, final float amount, final float newHealth, final boolean cancelled) {
        if (cancelled || !(entity instanceof ServerPlayer player)) {
            return;
        }

        boolean backward = false;
        if (source.getSourcePosition() != null) {
            double toSourceX = source.getSourcePosition().x - player.getX();
            double toSourceZ = source.getSourcePosition().z - player.getZ();
            if (toSourceX * toSourceX + toSourceZ * toSourceZ > 0.25D) {
                double yawRad = Math.toRadians(player.getYRot());
                double forwardDot = toSourceX * -Math.sin(yawRad) + toSourceZ * Math.cos(yawRad);
                forwardDot /= Math.hypot(toSourceX, toSourceZ);
                backward = forwardDot < 0.0D;
            }
        }

        boolean blocking = player.isBlocking()
            && (HeldItemGroupCondition.Group.SHIELD.matches(player.getMainHandItem())
                || HeldItemGroupCondition.Group.SHIELD.matches(player.getOffhandItem()));
        Identifier id;
        if (blocking) {
            String side = HeldItemGroupCondition.Group.SHIELD.matches(player.getMainHandItem())
                ? "right" : "left";
            id = Puppeteer.id("shield_impact_" + side);
        } else {
            id = Puppeteer.id(backward ? "hurt_backward" : "hurt_forward");
        }
        playEvent(player, EventKind.HURT, id);
    }

    private static void playEvent(final ServerPlayer player, final EventKind kind) {
        playEvent(player, kind, null);
    }

    private static void playEvent(final ServerPlayer player, final EventKind kind, final @Nullable Identifier preferredForHurt) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();
        Map<String, Long> cooldowns = LAST_EVENT_AT.computeIfAbsent(uuid, id -> new ConcurrentHashMap<>());
        Long lastAt = cooldowns.get(kind.name());
        if (lastAt != null && gameTime - lastAt < EVENT_COOLDOWN_TICKS) {
            return;
        }

        AnimationDefinition best = preferredForHurt == null ? null : AnimationDefinitions.byId(preferredForHurt);
        if (best == null) {
            for (AnimationDefinition definition : AnimationDefinitions.all()) {
                if (definition.eventKind().isPresent()
                    && definition.eventKind().get() == kind
                    && definition.trigger().test(player)
                    && (best == null || definition.priority() > best.priority())) {
                    best = definition;
                }
            }
        }
        if (best == null) {
            return;
        }

        cooldowns.put(kind.name(), gameTime);
        startOverlay(player, best, gameTime);
        puppeteer$startCompanions(player, best, gameTime);
    }

    private static void puppeteer$startCompanions(final ServerPlayer player, final AnimationDefinition definition, final long gameTime) {
        String path = definition.id().getPath();
        String suffixSide = null;
        if (path.endsWith("_left")) {
            suffixSide = "left";
        } else if (path.endsWith("_right")) {
            suffixSide = "right";
        }
        if (suffixSide == null) {
            return;
        }

        String base = path.substring(0, path.length() - suffixSide.length() - 1);
        for (String companion : new String[] {"_add_", "_effect_"}) {
            Identifier id = Puppeteer.id(base + companion + suffixSide);
            if (AnimationDefinitions.byId(id) != null) {
                startOverlay(player, AnimationDefinitions.byId(id), gameTime);
            }
        }
    }

    public static void playOnce(final ServerPlayer player, final Identifier animationId) {
        AnimationDefinition definition = AnimationDefinitions.byId(animationId);
        if (definition == null) {
            Puppeteer.LOGGER.warn("playOnce() called with unknown animation id {}", animationId);
            return;
        }
        startOverlay(player, definition, player.level().getGameTime());
    }

    private static void startOverlay(final ServerPlayer player, final AnimationDefinition definition, final long gameTime) {
        UUID uuid = player.getUUID();
        State state = STATES.get(uuid);
        List<Layer> overlays = state == null ? List.of() : state.overlays();

        List<Layer> updated = new ArrayList<>();
        for (Layer layer : overlays) {
            if (!layer.animationId().equals(definition.id())) {
                updated.add(layer);
            }
        }
        updated.add(new Layer(definition.id(), gameTime));
        if (state == null) {
            STATES.put(uuid, new State(List.of(), updated, List.of(), 1.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                gameTime + ambientDelay(new Random(), null), player.getX(), player.getZ()));
        } else {
            double dxo = player.getX() - state.lastX();
            double dzo = player.getZ() - state.lastZ();
            float instantNow = (float) Math.sqrt(dxo * dxo + dzo * dzo);
            STATES.put(uuid, new State(state.continuous(), updated, state.lastBroadcast(),
                state.broadcastedRate(), state.measuredSpeed(), state.gateSpeed(),
                instantNow, state.lastInstant(), state.nextAmbientAt(), state.lastX(), state.lastZ()));
        }
    }

    private static void onServerTick(final MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static float puppeteer$median3(final float a, final float b, final float c) {
        return Math.max(Math.min(a, b), Math.min(Math.max(a, b), c));
    }

    private static void tickPlayer(final ServerPlayer player) {
        UUID uuid = player.getUUID();
        long gameTime = player.level().getGameTime();

        boolean inWater = player.isInWater();
        boolean wasInWater = WAS_IN_WATER.getOrDefault(uuid, inWater);
        if (inWater && !wasInWater) {
            playEvent(player, EventKind.ENTER_WATER);
        }
        WAS_IN_WATER.put(uuid, inWater);

        puppeteer$detectEquips(player, uuid);
        final Identifier sitRoll = puppeteer$refreshSitRoll(player, uuid);
        State state = STATES.get(uuid);

        double x = player.getX();
        double z = player.getZ();
        float instant = 0.0F;
        if (state != null) {
            double dx = x - state.lastX();
            double dz = z - state.lastZ();
            double squared = dx * dx + dz * dz;
            instant = squared > 4.0 ? 0.0F : (float) Math.sqrt(squared);
        }
        float speed = (state == null) ? instant
            : state.measuredSpeed() + SPEED_SMOOTHING * (instant - state.measuredSpeed());
        float filteredInstant = instant;
        if (state != null) {
            filteredInstant = puppeteer$median3(instant, state.lastInstant(), state.previousInstant());
        }
        float gate = (state == null) ? filteredInstant
            : state.gateSpeed() + GATE_SMOOTHING * (filteredInstant - state.gateSpeed());

        AnimationDefinition baseDef = null;
        List<AnimationDefinition> additiveDefs = new ArrayList<>();
        for (AnimationDefinition definition : AnimationDefinitions.all()) {
            if (definition.eventKind().isPresent() || !definition.trigger().test(player)) {
                continue;
            }
            if (SIT_FAMILY.contains(definition.id()) && sitRoll != null && !sitRoll.equals(definition.id())) {
                continue;
            }
            if (definition.additive()) {
                additiveDefs.add(definition);
            } else if (baseDef == null || definition.priority() > baseDef.priority()) {
                baseDef = definition;
            }
        }
        additiveDefs.sort(java.util.Comparator.comparingInt(AnimationDefinition::priority).reversed());

        boolean suppressBase = false;
        List<Layer> previousOverlaysForSuppression = state == null ? List.of() : state.overlays();
        for (Layer layer : previousOverlaysForSuppression) {
            AnimationDefinition od = AnimationDefinitions.byId(layer.animationId());
            if (od != null && od.overridesBase()) {
                suppressBase = true;
                break;
            }
        }

        boolean crawlingPose = player.isVisuallySwimming() && !player.isInWater();
        boolean holding = false;
        if (baseDef != null && baseDef.id().getPath().startsWith("crawling")) {
            LAST_CRAWL.put(uuid, baseDef.id());
        } else if (baseDef == null && crawlingPose && LAST_CRAWL.get(uuid) != null) {
            AnimationDefinition held = AnimationDefinitions.byId(LAST_CRAWL.get(uuid));
            if (held != null) {
                baseDef = held;
                holding = true;
            }
        }
        CRAWL_HOLDING.put(uuid, holding);
        List<Identifier> desired = new ArrayList<>();
        if (baseDef != null && !suppressBase) {
            desired.add(baseDef.id());
        }
        for (AnimationDefinition definition : additiveDefs) {
            desired.add(definition.id());
        }

        List<Layer> previous = state == null ? List.of() : state.continuous();
        List<Layer> continuous = new ArrayList<>();
        for (Identifier id : desired) {
            continuous.add(new Layer(id, startOf(previous, id, gameTime)));
        }

        List<Layer> previousOverlays = state == null ? List.of() : state.overlays();
        List<Layer> overlays = new ArrayList<>();
        for (Layer layer : previousOverlays) {
            AnimationDefinition definition = AnimationDefinitions.byId(layer.animationId());
            if (definition == null) {
                continue;
            }
            if (puppeteer$isStaleHold(layer.animationId(), player)) {
                continue;
            }
            long elapsed = gameTime - layer.startGameTime();
            if (!definition.holdLastFrame() && elapsed > definition.lengthTicks()) {
                continue;
            }
            overlays.add(layer);
        }

        long nextAmbientAt = scheduleAmbient(player, state == null ? 0L : state.nextAmbientAt(),
            overlays, gameTime);

        float rate = 1.0F;
        if (baseDef != null && baseDef.playbackRate().isPresent()) {
            rate = baseDef.playbackRate().get().clamp(speed);
        }

        List<Layer> entries = new ArrayList<>(continuous);
        entries.addAll(overlays);
        float lastRate = state == null ? rate : state.broadcastedRate();
        float broadcastedRate = Math.abs(lastRate - rate) > RATE_BROADCAST_DELTA ? rate : lastRate;
        List<Layer> lastBroadcast = state == null ? List.of() : state.lastBroadcast();

        if (!sameLayers(lastBroadcast, entries) || Float.floatToIntBits(broadcastedRate) != Float.floatToIntBits(lastRate)) {
            List<AnimationStatePayload.Entry> payloadEntries = new ArrayList<>();
            for (Layer layer : entries) {
                payloadEntries.add(new AnimationStatePayload.Entry(layer.animationId(), layer.startGameTime()));
            }
            AnimationStatePayload payload = new AnimationStatePayload(player.getId(), gameTime, broadcastedRate, speed, payloadEntries);
            ServerPlayNetworking.send(player, payload);
            for (ServerPlayer viewer : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(viewer, payload);
            }
        }

        STATES.put(uuid, new State(continuous, overlays, entries, broadcastedRate, speed, gate, instant, state == null ? 0.0F : state.lastInstant(), nextAmbientAt, x, z));
    }

    private static long startOf(final List<Layer> previous, final Identifier id, final long now) {
        for (Layer layer : previous) {
            if (layer.animationId().equals(id)) {
                return layer.startGameTime();
            }
        }
        return now;
    }

    private static boolean sameLayers(final List<Layer> a, final List<Layer> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static long scheduleAmbient(
        final ServerPlayer player,
        final long previousNextAmbientAt,
        final List<Layer> overlays,
        final long gameTime
    ) {
        long next = previousNextAmbientAt == 0L ? gameTime + ambientDelay(RANDOM, null) : previousNextAmbientAt;
        if (gameTime < next) {
            return next;
        }

        AnimationDefinition best = null;
        for (AnimationDefinition definition : AnimationDefinitions.all()) {
            if (definition.eventKind().isPresent()
                && definition.eventKind().get() == EventKind.AMBIENT
                && definition.ambience().isPresent()
                && definition.trigger().test(player)
                && (best == null || definition.priority() > best.priority())) {
                best = definition;
            }
        }

        long runningAmbientDelay = 0L;
        if (best != null) {
            boolean alreadyRunning = false;
            for (Layer layer : overlays) {
                if (layer.animationId().equals(best.id())) {
                    alreadyRunning = true;
                    break;
                }
            }
            if (!alreadyRunning) {
                startOverlay(player, best, gameTime);
            }
            runningAmbientDelay = ambientDelay(RANDOM, best);
        }

        return gameTime + (runningAmbientDelay == 0L ? ambientDelay(RANDOM, best) : runningAmbientDelay);
    }

    private static long ambientDelay(final Random random, final @Nullable AnimationDefinition definition) {
        if (definition == null || definition.ambience().isEmpty()) {
            return 160L + random.nextInt(140);
        }
        AnimationDefinition.Ambience ambience = definition.ambience().get();
        int span = Math.max(1, ambience.maxIntervalTicks() - ambience.minIntervalTicks());
        return ambience.minIntervalTicks() + random.nextInt(span + 1);
    }

    public static float measuredSpeed(final ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0.0F : state.gateSpeed();
    }

    public static double lastDeltaX(final ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0.0D : player.getX() - state.lastX();
    }

    public static double lastDeltaZ(final ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state == null ? 0.0D : player.getZ() - state.lastZ();
    }

    private static boolean puppeteer$isStaleHold(final Identifier id, final ServerPlayer player) {
        String path = id.getPath();
        if (!path.startsWith("torch_item_in")) {
            return false;
        }
        boolean holds = path.endsWith("_right")
            ? HeldItemGroupCondition.Group.TORCH.matches(player.getMainHandItem())
            : HeldItemGroupCondition.Group.TORCH.matches(player.getOffhandItem());
        return !holds;
    }

    private static void puppeteer$detectEquips(final ServerPlayer player, final UUID uuid) {
        Item main = player.getMainHandItem().getItem();
        Item off = player.getOffhandItem().getItem();
        Item helmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
        Item chest = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
        Item legs = player.getItemBySlot(EquipmentSlot.LEGS).getItem();
        Item feet = player.getItemBySlot(EquipmentSlot.FEET).getItem();

        EquipSnapshot previous = EQUIP_SNAPSHOTS.get(uuid);
        if (previous == null) {
            EQUIP_SNAPSHOTS.put(uuid, new EquipSnapshot(main, off, helmet, chest, legs, feet));
            return;
        }

        Identifier preferred = null;
        // Shield snapped: its slot went from holding a shield to empty. Play the
        // break one-shot for that hand plus the add companion.
        if (previous.main() instanceof net.minecraft.world.item.ShieldItem && main == net.minecraft.world.item.Items.AIR) {
            puppeteer$playShieldBreak(player, "right");
        } else if (previous.off() instanceof net.minecraft.world.item.ShieldItem && off == net.minecraft.world.item.Items.AIR) {
            puppeteer$playShieldBreak(player, "left");
        }
        if (helmet != previous.helmet()) {
            preferred = Puppeteer.id("armor_in_helmet");
        } else if (chest != previous.chest() || legs != previous.legs() || feet != previous.feet()) {
            preferred = Puppeteer.id("armor_in");
        } else if (main != previous.main()) {
            if (HeldItemGroupCondition.Group.TORCH.matches(player.getMainHandItem())) {
                preferred = Puppeteer.id("torch_item_in_right");
            }
        } else if (off != previous.off()
            && HeldItemGroupCondition.Group.TORCH.matches(player.getOffhandItem())) {
            preferred = Puppeteer.id("torch_item_in_left");
        }

        EQUIP_SNAPSHOTS.put(uuid, new EquipSnapshot(main, off, helmet, chest, legs, feet));
        if (preferred != null) {
            playEvent(player, EventKind.EQUIP, preferred);
        }
    }

    private static final int SIT_ROLL_WEIGHTS = 10;

    private static @Nullable Identifier puppeteer$refreshSitRoll(final ServerPlayer player, final UUID uuid) {
        if (!player.isPassenger()) {
            SIT_ROLL.remove(uuid);
            return null;
        }
        Identifier existing = SIT_ROLL.get(uuid);
        if (existing != null) {
            return existing;
        }
        int roll = RANDOM.nextInt(SIT_ROLL_WEIGHTS);
        Identifier chosen;
        if (roll < 6) {
            chosen = Puppeteer.id("sit");
        } else if (roll < 8) {
            chosen = Puppeteer.id("sit2");
        } else {
            chosen = Puppeteer.id("sit3");
        }
        SIT_ROLL.put(uuid, chosen);
        return chosen;
    }

    private static InteractionResult onUseItem(
        final net.minecraft.world.entity.player.Player player,
        final Level level,
        final InteractionHand hand
    ) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()
            && HeldItemGroupCondition.Group.FLINT.matches(serverPlayer.getItemInHand(hand))) {
            puppeteer$fireFlintUse(serverPlayer, hand);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult onUseBlock(
        final net.minecraft.world.entity.player.Player player,
        final Level level,
        final InteractionHand hand,
        final BlockHitResult hitResult
    ) {
        if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()
            && HeldItemGroupCondition.Group.FLINT.matches(serverPlayer.getItemInHand(hand))) {
            puppeteer$fireFlintUse(serverPlayer, hand);
        }
        return InteractionResult.PASS;
    }

    private static void puppeteer$playShieldBreak(final ServerPlayer player, final String side) {
        final long gameTime = player.level().getGameTime();
        AnimationDefinition definition = AnimationDefinitions.byId(Puppeteer.id("shield_break_" + side));
        if (definition != null) {
            startOverlay(player, definition, gameTime);
            puppeteer$startCompanions(player, definition, gameTime);
        }
    }

    private static void puppeteer$fireFlintUse(final ServerPlayer serverPlayer, final InteractionHand hand) {
        String side = hand == InteractionHand.OFF_HAND ? "left" : "right";
        Identifier primary = Puppeteer.id(
            serverPlayer.isShiftKeyDown() ? "flint_use_" + side + "_alt" : "flint_use_" + side);
        playEvent(serverPlayer, EventKind.USE_ITEM, primary);
        AnimationDefinition head = AnimationDefinitions.byId(Puppeteer.id("flint_use_head_" + side));
        if (head != null) {
            startOverlay(serverPlayer, head, serverPlayer.level().getGameTime());
        }
    }

    private static final class AnimationCommand {
        private static void register(final com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(Commands.literal("animation")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("play")
                    .then(Commands.argument("animation", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for (AnimationDefinition definition : AnimationDefinitions.all()) {
                                builder.suggest(definition.id().toString());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            Identifier requested = Identifier.tryParse(StringArgumentType.getString(context, "animation"));
                            if (requested == null) {
                                context.getSource().sendFailure(Component.literal("Not a valid animation id"));
                                return 0;
                            }
                            Identifier id = requested.getNamespace().equals("minecraft")
                                ? Puppeteer.id(requested.getPath()) : requested;
                            if (AnimationDefinitions.byId(id) == null) {
                                context.getSource().sendFailure(Component.literal("Unknown animation " + id));
                                return 0;
                            }
                            playOnce(player, id);
                            context.getSource().sendSuccess(
                                () -> Component.literal("Playing animation " + id), false);
                            return 1;
                        })
                    )
                )
            );
        }
    }
}
