package com.aura.lamplight.menagerie.crow;

import com.aura.lamplight.menagerie.crow.ai.CrowFollowOwnerGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowForageGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowMeleeGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowShoulderGoal;
import com.aura.lamplight.menagerie.crow.ai.DirectNavigator;
import com.aura.lamplight.menagerie.crow.ai.FlightMoveControl;
import com.aura.lamplight.menagerie.registry.MenagerieEntities;
import com.aura.lamplight.menagerie.registry.MenagerieSounds;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@SuppressWarnings("all")
public class CrowEntity extends ShoulderRidingEntity {
    private static final float TAMING_CHANCE = 0.30F;
    private static final int EAT_DELAY = 60;
    private static final float FEED_HEAL = 4.0F;
    private static final int DELIVERY_LEAVE_TICKS = 60;
    private static final int DELIVERY_WAIT_TICKS = 100;
    private static final int DELIVERY_TARGET_WAIT_TICKS = 20 * 30;
    private static final int DELIVERY_DROP_TICKS = 20;
    private static final int DELIVERY_RETURN_LEAVE_TICKS = 60;
    private static final long DELIVERY_MAX_TICKS = 20L * 60L * 5L;
    private static final TicketType CROW_CHUNK_TICKET = new TicketType(TicketType.NO_TIMEOUT,
            TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE);
    private static final TicketType CROW_PRELOAD_TICKET = new TicketType(20L * 60L * 5L,
            TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE
                    | TicketType.FLAG_CAN_EXPIRE_IF_UNLOADED);
    private static final Map<ServerLevel, Map<Long, Integer>> FORCED_CROW_CHUNKS = new WeakHashMap<>();
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.INT);

    public float prevFlyProgress;
    public float flyProgress;
    public float prevAttackProgress;
    public float attackProgress;
    public float prevSitProgress;
    public float sitProgress;

    private boolean landNavigator;
    private int timeFlying;
    private int heldItemTime;
    @Nullable
    private UUID feederId;

    private DeliveryState deliveryState = DeliveryState.IDLE;
    @Nullable
    private UUID deliveryTarget;
    private int deliveryTicks;
    private long deliveryDeadline;
    @Nullable
    private ResourceKey<Level> deliveryHomeDimension;
    private Vec3 deliveryHomePos = Vec3.ZERO;
    private boolean fetching;
    @Nullable
    private ServerLevel forcedLevel;
    private long forcedChunk = Long.MIN_VALUE;

    public CrowEntity(EntityType<? extends CrowEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.FIRE, -1.0F);
        setPathfindingMalus(PathType.WATER, -1.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        setDropChance(EquipmentSlot.MAINHAND, 1.0F);
        switchNavigator(false);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    public static MobCategory category() {
        return MobCategory.CREATURE;
    }

    public static Heightmap.Types heightmap() {
        return Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(-1, new DeliveryGoal());
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new FetchBundleGoal());
        goalSelector.addGoal(2, new CrowMeleeGoal(this));
        goalSelector.addGoal(2, new CrowShoulderGoal(this));
        goalSelector.addGoal(3, new CrowFollowOwnerGoal(this, true));
        goalSelector.addGoal(4, new ScatterGoal());
        goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(6, new CrowForageGoal(this));
        goalSelector.addGoal(7, new WalkIdleGoal());
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, PathfinderMob.class, 6.0F));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this, Player.class).setAlertOthers());
    }

    public static <T extends Mob> boolean canCrowSpawn(EntityType<CrowEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return AnimalCheck.isBrightEnough(level, pos);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (isTame()) {
            LivingEntity owner = getOwner();
            if (other == owner) {
                return true;
            }
            if (other instanceof TamableAnimal tamable && tamable.isOwnedBy(owner)) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(other);
            }
        }
        return super.considersEntityAsAlly(other);
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveControl = new MoveControl(this);
            this.navigation = new GroundPathNavigation(this, level());
            this.landNavigator = true;
        } else {
            this.moveControl = new FlightMoveControl(this, 0.7F, false);
            this.navigation = new DirectNavigator(this, level());
            this.landNavigator = false;
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableTo(level, source)) {
            return false;
        }
        setOrderedToSit(false);
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MenagerieTags.CROW_BREEDABLES) && isTame();
    }

    @Override
    public boolean canMate(Animal other) {
        return !isSitting() && super.canMate(other);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        InteractionResult use = held.interactLivingEntity(player, this, hand);
        if (use.consumesAction()) {
            return use;
        }

        if (isTame()) {
            if (!isOwnedBy(player)) {
                return InteractionResult.PASS;
            }
            if (isDelivering()) {
                return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            if (held.isEmpty()) {
                if (level().isClientSide()) {
                    return InteractionResult.SUCCESS;
                }
                if (player.isShiftKeyDown()) {
                    toggleCommand();
                } else if (!getMainHandItem().isEmpty()) {
                    takeHeldItem(player);
                } else {
                    toggleCommand();
                }
                return InteractionResult.SUCCESS;
            }

            if (!isBaby() && !isSitting() && getHealth() >= getMaxHealth()
                    && !isInLove() && held.is(MenagerieTags.CROW_BREEDABLES)) {
                ItemStack before = held.copy();
                InteractionResult breed = super.mobInteract(player, hand);
                if (breed.consumesAction()) {
                    player.setItemInHand(hand, before);
                    return breed;
                }
            }

            if (isBaby() && !isCrowEdible(held)) {
                return InteractionResult.PASS;
            }

            if (!level().isClientSide() && isDeliveryBundle(held)) {
                UUID target = resolveDeliveryTarget((ServerLevel) level(), held);
                if (target != null) {
                    insertHeldItem(held, null);
                    startDelivery((ServerLevel) level(), target);
                    return InteractionResult.SUCCESS;
                }
            }

            if (level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            insertHeldItem(held, null);
            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty() || !isCrowEdible(held) || !getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        insertHeldItem(held, held.is(MenagerieTags.CROW_TAMEABLES) ? player.getUUID() : null);
        return InteractionResult.SUCCESS;
    }

    private void insertHeldItem(ItemStack held, @Nullable UUID feeder) {
        if (!(level() instanceof ServerLevel server) || held.isEmpty()) {
            return;
        }
        ItemStack current = getMainHandItem();
        if (!current.isEmpty()) {
            spawnAtLocation(server, current.copy());
        }
        setItemInHand(InteractionHand.MAIN_HAND, held.copyWithCount(1));
        held.shrink(1);
        heldItemTime = 0;
        feederId = feeder;
    }

    public boolean isDelivering() {
        return deliveryState != DeliveryState.IDLE;
    }

    public boolean isFetching() {
        return fetching;
    }

    private boolean isDeliveryBundle(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BundleItem)) {
            return false;
        }
        var customName = stack.getCustomName();
        return customName != null && !customName.getString().isBlank();
    }

    @Nullable
    private UUID resolveDeliveryTarget(ServerLevel server, ItemStack stack) {
        var customName = stack.getCustomName();
        if (customName == null) {
            return null;
        }
        String name = customName.getString().trim();
        if (name.isEmpty()) {
            return null;
        }

        UUID uuid = parseUuid(name);
        if (uuid != null) {
            Entity entity = server.getEntityInAnyDimension(uuid);
            return entity != null && entity.isAlive() && !entity.isRemoved() ? uuid : null;
        }

        ServerPlayer player = server.getServer().getPlayerList().getPlayerByName(name);
        if (player == null) {
            for (ServerPlayer candidate : server.getServer().getPlayerList().getPlayers()) {
                if (candidate.getScoreboardName().equalsIgnoreCase(name)) {
                    player = candidate;
                    break;
                }
            }
        }
        return player != null && player.isAlive() && !player.isRemoved() ? player.getUUID() : null;
    }

    @Nullable
    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
        }
        if (value.length() == 32) {
            String dashed = value.substring(0, 8) + "-" + value.substring(8, 12) + "-"
                    + value.substring(12, 16) + "-" + value.substring(16, 20) + "-"
                    + value.substring(20);
            try {
                return UUID.fromString(dashed);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    private void startDelivery(ServerLevel server, UUID target) {
        setOrderedToSit(false);
        if (isPassenger()) {
            stopRiding();
        }
        resetLove();
        setFlying(true);
        getNavigation().stop();
        setTarget(null);

        deliveryState = DeliveryState.LEAVING;
        deliveryTarget = target;
        deliveryTicks = 0;
        deliveryDeadline = server.getGameTime() + DELIVERY_MAX_TICKS;

        UUID ownerId = getOwnerUuid();
        Entity owner = ownerId != null ? server.getEntityInAnyDimension(ownerId) : null;
        if (owner != null) {
            deliveryHomeDimension = owner.level().dimension();
            deliveryHomePos = owner.position();
        } else {
            deliveryHomeDimension = level().dimension();
            deliveryHomePos = position();
        }
    }

    private void tickDelivery() {
        if (!(level() instanceof ServerLevel server)) {
            clearDeliveryState();
            return;
        }
        if (getMainHandItem().isEmpty()) {
            clearDeliveryState();
            return;
        }
        UUID ownerId = getOwnerUuid();
        if (ownerId == null) {
            clearDeliveryState();
            return;
        }

        updateDeliveryHome(server, ownerId);
        long now = server.getGameTime();

        switch (deliveryState) {
            case LEAVING -> {
                moveAwayFrom(deliveryHomePos);
                if (++deliveryTicks >= DELIVERY_LEAVE_TICKS) {
                    deliveryState = DeliveryState.WAITING;
                    deliveryTicks = 0;
                    getNavigation().stop();
                }
            }
            case WAITING -> {
                hover();
                deliveryTicks++;
                if (deliveryTicks < DELIVERY_WAIT_TICKS) {
                    return;
                }
                Entity target = findLoadedTarget(server);
                if (target != null) {
                    deliveryState = DeliveryState.DROPPING;
                    deliveryTicks = 0;
                    teleportToEntity(target);
                    return;
                }
                if (now > deliveryDeadline
                        || deliveryTicks > DELIVERY_WAIT_TICKS + DELIVERY_TARGET_WAIT_TICKS) {
                    beginReturn();
                }
            }
            case DROPPING -> {
                hover();
                if (++deliveryTicks >= DELIVERY_DROP_TICKS) {
                    dropDeliveryBundle(server);
                    deliveryState = DeliveryState.RETURN_LEAVING;
                    deliveryTicks = 0;
                }
            }
            case RETURN_LEAVING -> {
                moveAwayFrom(position());
                if (++deliveryTicks >= DELIVERY_RETURN_LEAVE_TICKS) {
                    finishDelivery(server, ownerId);
                }
            }
            case IDLE -> {
            }
        }
    }

    private void updateDeliveryHome(ServerLevel server, UUID ownerId) {
        Entity owner = server.getEntityInAnyDimension(ownerId);
        if (owner != null && owner.isAlive()) {
            deliveryHomeDimension = owner.level().dimension();
            deliveryHomePos = owner.position();
        }
    }

    @Nullable
    private Entity findLoadedTarget(ServerLevel server) {
        if (deliveryTarget == null) {
            return null;
        }
        Entity entity = server.getEntityInAnyDimension(deliveryTarget);
        return entity != null && entity.isAlive() && !entity.isRemoved() ? entity : null;
    }

    private void teleportToEntity(Entity target) {
        ServerLevel targetLevel = target.level() instanceof ServerLevel server ? server : null;
        if (targetLevel == null) {
            beginReturn();
            return;
        }
        Vec3 destination = target.position().add(0.0D, 0.5D, 0.0D);
        teleportToPosition(targetLevel, destination);
    }

    private void teleportToPosition(ServerLevel destinationLevel, Vec3 destination) {
        ChunkPos chunk = ChunkPos.containing(BlockPos.containing(destination));
        destinationLevel.getChunkSource().addTicketWithRadius(CROW_PRELOAD_TICKET, chunk, 1);

        if (destinationLevel == level()) {
            snapTo(destination.x, destination.y, destination.z, getYRot(), getXRot());
            setDeltaMovement(Vec3.ZERO);
            setFlying(true);
            getNavigation().stop();
            return;
        }
        teleport(new TeleportTransition(destinationLevel, destination, Vec3.ZERO, getYRot(), getXRot(),
                Set.of(), TeleportTransition.DO_NOTHING));
    }

    private void beginReturn() {
        deliveryState = DeliveryState.RETURN_LEAVING;
        deliveryTicks = 0;
        setFlying(true);
        getNavigation().stop();
    }

    private void finishDelivery(ServerLevel server, UUID ownerId) {
        Entity owner = server.getEntityInAnyDimension(ownerId);
        ServerLevel destinationLevel = null;
        Vec3 destination = null;

        if (owner != null && owner.isAlive() && owner.level() instanceof ServerLevel ownerLevel) {
            destinationLevel = ownerLevel;
            destination = owner.position().add(0.0D, 0.5D, 0.0D);
        } else if (deliveryHomeDimension != null) {
            ServerLevel homeLevel = server.getServer().getLevel(deliveryHomeDimension);
            if (homeLevel != null) {
                destinationLevel = homeLevel;
                destination = deliveryHomePos.add(0.0D, 0.5D, 0.0D);
            }
        }

        clearDeliveryState();
        if (destinationLevel != null && destination != null) {
            teleportToPosition(destinationLevel, destination);
        }
    }

    private void dropDeliveryBundle(ServerLevel server) {
        ItemStack bundle = getMainHandItem().copy();
        if (bundle.isEmpty()) {
            return;
        }
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        heldItemTime = 0;
        feederId = null;
        spawnAtLocation(server, bundle, 0.5F);
    }

    private void moveAwayFrom(Vec3 from) {
        Vec3 away = position().subtract(from);
        Vec3 horizontal = new Vec3(away.x, 0.0D, away.z);
        if (horizontal.lengthSqr() < 0.01D) {
            double angle = getRandom().nextDouble() * Math.PI * 2.0D;
            horizontal = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
        } else {
            horizontal = horizontal.normalize();
        }
        Vec3 destination = position().add(horizontal.scale(8.0D)).add(0.0D, 3.0D, 0.0D);
        setFlying(true);
        getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 1.0F);
    }

    private void hover() {
        setFlying(true);
        getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0F);
    }

    @Nullable
    private UUID getOwnerUuid() {
        EntityReference<LivingEntity> reference = getOwnerReference();
        return reference != null ? reference.getUUID() : null;
    }

    private void clearDeliveryState() {
        deliveryState = DeliveryState.IDLE;
        deliveryTarget = null;
        deliveryTicks = 0;
        deliveryDeadline = 0L;
        deliveryHomeDimension = null;
        deliveryHomePos = Vec3.ZERO;
    }

    private void updateForceLoad() {
        if (!(level() instanceof ServerLevel server)) {
            releaseForceLoad();
            return;
        }
        long chunk = chunkPosition().pack();
        if (forcedLevel == server && forcedChunk == chunk) {
            return;
        }

        ServerLevel oldLevel = forcedLevel;
        long oldChunk = forcedChunk;

        forcedLevel = server;
        forcedChunk = chunk;
        acquireForcedChunk(server, chunk);

        if (oldLevel != null && oldChunk != Long.MIN_VALUE) {
            releaseForcedChunk(oldLevel, oldChunk);
        }
    }

    private void releaseForceLoad() {
        if (forcedLevel != null && forcedChunk != Long.MIN_VALUE) {
            releaseForcedChunk(forcedLevel, forcedChunk);
        }
        forcedLevel = null;
        forcedChunk = Long.MIN_VALUE;
    }

    private static void acquireForcedChunk(ServerLevel level, long chunk) {
        Map<Long, Integer> chunks = FORCED_CROW_CHUNKS.computeIfAbsent(level, key -> new HashMap<>());
        int count = chunks.getOrDefault(chunk, 0);
        chunks.put(chunk, count + 1);
        if (count == 0) {
            level.getChunkSource().addTicketWithRadius(CROW_CHUNK_TICKET, ChunkPos.unpack(chunk), 1);
        }
    }

    private static void releaseForcedChunk(ServerLevel level, long chunk) {
        Map<Long, Integer> chunks = FORCED_CROW_CHUNKS.get(level);
        if (chunks == null) {
            return;
        }
        int count = chunks.getOrDefault(chunk, 0);
        if (count <= 1) {
            chunks.remove(chunk);
            if (chunks.isEmpty()) {
                FORCED_CROW_CHUNKS.remove(level);
            }
            level.getChunkSource().removeTicketWithRadius(CROW_CHUNK_TICKET, ChunkPos.unpack(chunk), 1);
        } else {
            chunks.put(chunk, count - 1);
        }
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        releaseForceLoad();
        super.remove(reason);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        releaseForceLoad();
        super.removeAfterChangingDimensions();
    }

    private void takeHeldItem(Player player) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        ItemStack held = getMainHandItem().copy();
        if (held.isEmpty()) {
            return;
        }
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        heldItemTime = 0;
        feederId = null;
        if (!player.addItem(held)) {
            spawnAtLocation(server, held);
        }
    }

    private void toggleCommand() {
        setOrderedToSit(!isOrderedToSit());
    }

    @Override
    public void tick() {
        updateForceLoad();
        super.tick();
        prevAttackProgress = attackProgress;
        prevFlyProgress = flyProgress;
        prevSitProgress = sitProgress;

        if (isSitting() || isPassenger()) {
            if (sitProgress < 5F) {
                sitProgress++;
            }
        } else if (sitProgress > 0F) {
            sitProgress--;
        }
        if (isFlying()) {
            if (flyProgress < 5F) {
                flyProgress++;
            }
        } else if (flyProgress > 0F) {
            flyProgress--;
        }

        if (!level().isClientSide()) {
            boolean flying = isFlying();
            if (flying && landNavigator) {
                switchNavigator(false);
            } else if (!flying && !landNavigator) {
                switchNavigator(true);
            }
            if (flying) {
                timeFlying++;
                setNoGravity(true);
                if (isSitting() || isPassenger() || isInLove()) {
                    setFlying(false);
                } else if (timeFlying > 8 && (onGround() || verticalCollisionBelow)) {
                    setFlying(false);
                }
            } else {
                timeFlying = 0;
                setNoGravity(false);
            }

            if (!getMainHandItem().isEmpty()) {
                heldItemTime++;
                if (heldItemTime >= EAT_DELAY && isCrowEdible(getMainHandItem())
                        && (!isTame() || getHealth() < getMaxHealth())) {
                    heldItemTime = 0;
                    heal(FEED_HEAL);
                    gameEvent(GameEvent.EAT);
                    playSound(SoundEvents.PARROT_EAT, getSoundVolume(), getVoicePitch());
                    ItemStack held = getMainHandItem();
                    if (feederId != null && held.is(MenagerieTags.CROW_TAMEABLES) && !isTame()) {
                        Player feeder = level() instanceof ServerLevel server
                                ? server.getPlayerInAnyDimension(feederId)
                                : null;
                        if (feeder != null) {
                            if (getRandom().nextFloat() < TAMING_CHANCE) {
                                tame(feeder);
                                setOrderedToSit(false);
                                if (feeder instanceof ServerPlayer serverPlayer) {
                                    CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
                                }
                                level().broadcastEntityEvent(this, (byte) 7);
                            } else {
                                level().broadcastEntityEvent(this, (byte) 6);
                            }
                        }
                        feederId = null;
                    }
                    ItemStackTemplate remainder = held.getItem().getCraftingRemainder();
                    if (remainder != null && level() instanceof ServerLevel server) {
                        ItemStack rest = remainder.create();
                        if (!rest.isEmpty()) {
                            spawnAtLocation(server, rest);
                        }
                    }
                    held.shrink(1);
                    if (held.isEmpty()) {
                        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
            } else {
                heldItemTime = 0;
                feederId = null;
            }
        }

        if (entityData.get(ATTACK_TICK) > 0) {
            entityData.set(ATTACK_TICK, entityData.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else if (attackProgress > 0F) {
            attackProgress--;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Flying", isFlying());
        output.putString("DeliveryState", deliveryState.name());
        output.putInt("DeliveryTicks", deliveryTicks);
        output.putLong("DeliveryDeadline", deliveryDeadline);
        if (deliveryTarget != null) {
            output.putString("DeliveryTarget", deliveryTarget.toString());
        }
        if (deliveryHomeDimension != null) {
            output.putString("DeliveryHomeDimension", deliveryHomeDimension.identifier().toString());
        }
        output.putDouble("DeliveryHomeX", deliveryHomePos.x);
        output.putDouble("DeliveryHomeY", deliveryHomePos.y);
        output.putDouble("DeliveryHomeZ", deliveryHomePos.z);
    }

    @Override
    public void travel(Vec3 vec) {
        if (isSitting()) {
            if (getNavigation().getPath() != null) {
                getNavigation().stop();
            }
            vec = Vec3.ZERO;
        }
        if (isInWater() && getDeltaMovement().y > 0F) {
            setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.5D, 1.0D));
        }
        super.travel(vec);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setFlying(input.getBooleanOr("Flying", false));

        boolean sitting = isOrderedToSit();
        Optional<Boolean> oldCrowSitting = input.read("CrowSitting", Codec.BOOL);
        Optional<Integer> oldCommand = input.getInt("Command");
        if (oldCrowSitting.isPresent()) {
            sitting = oldCrowSitting.get();
        } else if (oldCommand.isPresent()) {
            sitting = oldCommand.get() >= 2;
        }
        setOrderedToSit(sitting);

        deliveryState = parseDeliveryState(input.getStringOr("DeliveryState", DeliveryState.IDLE.name()));
        deliveryTicks = input.getIntOr("DeliveryTicks", 0);
        deliveryDeadline = input.getLongOr("DeliveryDeadline", 0L);
        deliveryTarget = parseUuid(input.getStringOr("DeliveryTarget", ""));
        Identifier homeId = Identifier.tryParse(input.getStringOr("DeliveryHomeDimension", ""));
        deliveryHomeDimension = homeId != null ? ResourceKey.create(Registries.DIMENSION, homeId) : null;
        deliveryHomePos = new Vec3(
                input.getDoubleOr("DeliveryHomeX", 0.0D),
                input.getDoubleOr("DeliveryHomeY", 0.0D),
                input.getDoubleOr("DeliveryHomeZ", 0.0D));

        if (isDelivering() && (deliveryTarget == null || getMainHandItem().isEmpty())) {
            clearDeliveryState();
        }
    }

    private static DeliveryState parseDeliveryState(String name) {
        try {
            return DeliveryState.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return DeliveryState.IDLE;
        }
    }

    public boolean isFlying() {
        return entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if (!flying || (!isBaby() && !isPassenger())) {
            entityData.set(FLYING, flying);
        }
    }

    public boolean isSitting() {
        return isOrderedToSit() || isInSittingPose();
    }

    public boolean isFollowing() {
        return isTame() && !isSitting();
    }

    @Override
    public void setOrderedToSit(boolean sit) {
        super.setOrderedToSit(sit);
        setInSittingPose(sit);
        if (sit) {
            getNavigation().stop();
            setFlying(false);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
        builder.define(ATTACK_TICK, 0);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.FALL) || super.isInvulnerableTo(level, source);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return MenagerieEntities.CROW.create(level, EntitySpawnReason.BREEDING);
    }

    public boolean isTargetBlocked(Vec3 target) {
        Vec3 eye = new Vec3(getX(), getEyeY(), getZ());
        return level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 60;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MenagerieSounds.CROW_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MenagerieSounds.CROW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MenagerieSounds.CROW_HURT;
    }

    public Vec3 getBlockInViewAway(Vec3 fleePos, float radiusAdd) {
        float radius = 3.15F * -3 - getRandom().nextInt(24) - radiusAdd;
        float angle = crowAngle();
        BlockPos radial = new BlockPos((int) (fleePos.x() + radius * Mth.sin(Mth.PI + angle)), 0,
                (int) (fleePos.z() + radius * Mth.cos(angle)));
        BlockPos ground = crowGround(radial);
        int above = (int) getY() - ground.getY();
        BlockPos dest = above > 8 ? ground.above(4 + getRandom().nextInt(10)) : ground.above(getRandom().nextInt(6) + 1);
        if (!isTargetBlocked(Vec3.atCenterOf(dest)) && distanceToSqr(Vec3.atCenterOf(dest)) > 1) {
            return Vec3.atCenterOf(dest);
        }
        return null;
    }

    private BlockPos crowGround(BlockPos in) {
        BlockPos pos = new BlockPos(in.getX(), (int) getY(), in.getZ());
        while (pos.getY() > level().getMinY() && !level().getBlockState(pos).isSolid() && level().getFluidState(pos).isEmpty()) {
            pos = pos.below();
        }
        return pos;
    }

    public Vec3 getBlockGrounding(Vec3 fleePos) {
        for (int attempt = 0; attempt < 8; attempt++) {
            float radius = 4.0F + getRandom().nextFloat() * 8.0F;
            float angle = crowAngle();
            int x = Mth.floor(fleePos.x() + radius * Mth.sin(Mth.PI + angle));
            int z = Mth.floor(fleePos.z() + radius * Mth.cos(angle));
            BlockPos landing = level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, (int) fleePos.y(), z));
            BlockPos ground = landing.below();
            if (ground.getY() < level().getMinY()) {
                continue;
            }
            BlockState groundState = level().getBlockState(ground);
            if (!groundState.getFluidState().isEmpty()
                    || groundState.getCollisionShape(level(), ground).isEmpty()) {
                continue;
            }
            if (level().getBlockState(landing).getCollisionShape(level(), landing).isEmpty()
                    && !isTargetBlocked(Vec3.atCenterOf(landing))
                    && distanceToSqr(Vec3.atCenterOf(landing)) > 1.0D) {
                return Vec3.atCenterOf(landing);
            }
        }
        return null;
    }

    private float crowAngle() {
        float neg = getRandom().nextBoolean() ? 1 : -1;
        return CrowMath.STARTING_ANGLE * yBodyRot + 3.15F + getRandom().nextFloat() * neg;
    }

    private boolean isOverWater() {
        BlockPos pos = blockPosition();
        while (pos.getY() > level().getMinY() && level().isEmptyBlock(pos)) {
            pos = pos.below();
        }
        return !level().getFluidState(pos).isEmpty();
    }

    public void peck() {
        entityData.set(ATTACK_TICK, 7);
    }

    private boolean isCrowEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(MenagerieTags.CROW_FOODSTUFFS);
    }

    private static boolean hasAvailableOwner(CrowEntity crow) {
        LivingEntity owner = crow.getOwner();
        return owner != null && owner.isAlive() && !owner.isSpectator()
                && owner.level() == crow.level();
    }

    private final class WalkIdleGoal extends Goal {
        private final CrowEntity crow = CrowEntity.this;
        private double x;
        private double y;
        private double z;
        private boolean flightTarget;
        private boolean landingTarget;

        WalkIdleGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (crow.isVehicle() || crow.isPassenger() || crow.isSitting()
                    || (crow.getTarget() != null && crow.getTarget().isAlive())
                    || (crow.isTame() && hasAvailableOwner(crow))) {
                return false;
            }
            if (crow.getRandom().nextInt(30) != 0 && !crow.isFlying()) {
                return false;
            }
            if (crow.onGround()) {
                flightTarget = random.nextBoolean();
            } else {
                flightTarget = true;
            }
            Vec3 pos = findPos();
            if (pos == null) {
                return false;
            }
            x = pos.x;
            y = pos.y;
            z = pos.z;
            return true;
        }

        @Override
        public void tick() {
            if (flightTarget) {
                crow.getMoveControl().setWantedPosition(x, y, z, 1F);
                if (landingTarget && crow.distanceToSqr(x, y, z) < 4.0D) {
                    crow.setFlying(false);
                    crow.getNavigation().stop();
                    stop();
                }
            } else {
                crow.getNavigation().moveTo(x, y, z, 1F);
                if (isFlying() && crow.onGround()) {
                    crow.setFlying(false);
                }
            }
            if (isFlying() && crow.onGround() && timeFlying > 10) {
                crow.setFlying(false);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 origin = crow.position();
            if (crow.isOverWater()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (timeFlying < 50 || crow.isOverWater()) {
                    landingTarget = false;
                    return crow.getBlockInViewAway(origin, 0);
                }
                landingTarget = true;
                return crow.getBlockGrounding(origin);
            }
            landingTarget = false;
            return LandRandomPos.getPos(crow, 10, 7);
        }

        @Override
        public boolean canContinueToUse() {
            if (crow.isSitting() || (crow.isTame() && hasAvailableOwner(crow))) {
                return false;
            }
            if (flightTarget) {
                return crow.isFlying() && crow.distanceToSqr(x, y, z) > 2F;
            }
            return !crow.getNavigation().isDone() && !crow.isVehicle();
        }

        @Override
        public void start() {
            if (flightTarget) {
                crow.setFlying(true);
            }
            crow.getMoveControl().setWantedPosition(x, y, z, 1F);
            if (!flightTarget) {
                crow.getNavigation().moveTo(x, y, z, 1F);
            }
        }

        @Override
        public void stop() {
            crow.getNavigation().stop();
            super.stop();
        }
    }

    private final class ScatterGoal extends Goal {
        private final Sorter sorter = new Sorter(CrowEntity.this);
        private final Predicate<? super Entity> selector = new Predicate<>() {
            @Override
            public boolean apply(@Nullable Entity entity) {
                if (entity == null) {
                    return false;
                }
                if (entity.isAlive() && entity.getType().builtInRegistryHolder().is(MenagerieTags.SCATTERS_CROWS)) {
                    return true;
                }
                if (entity instanceof Player player) {
                    if (player.isCreative() || player.isSpectator()) {
                        return false;
                    }
                    boolean calm = player.isShiftKeyDown()
                            && (player.getMainHandItem().is(MenagerieTags.CROW_FOODSTUFFS)
                            || player.getOffhandItem().is(MenagerieTags.CROW_FOODSTUFFS));
                    return !calm;
                }
                return false;
            }
        };
        private Entity target;
        private Vec3 flightTarget;
        private int cooldown;

        ScatterGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (isPassenger() || isVehicle() || isTame()) {
                return false;
            }
            if (getRandom().nextInt(8) != 0 && level().getGameTime() % 10 != 0 && getNoActionTime() >= 100) {
                return false;
            }
            List<Entity> nearby = level().getEntitiesOfClass(Entity.class, targetArea(), selector);
            if (nearby.isEmpty()) {
                return false;
            }
            nearby.sort(sorter);
            target = nearby.get(0);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && target.isAlive() && !isTame();
        }

        @Override
        public void stop() {
            flightTarget = null;
            target = null;
        }

        @Override
        public void tick() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (flightTarget != null) {
                setFlying(true);
                getMoveControl().setWantedPosition(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                if (cooldown == 0 && isTargetBlocked(flightTarget)) {
                    cooldown = 30;
                    flightTarget = null;
                }
            }
            if (target != null) {
                if (onGround() || flightTarget == null || distanceToSqr(flightTarget) < 3) {
                    Vec3 away = getBlockInViewAway(target.position(), 0);
                    if (away != null && away.y() > getY()) {
                        flightTarget = away;
                    }
                }
                if (distanceTo(target) > 20.0F) {
                    stop();
                }
            }
        }

        private AABB targetArea() {
            return new AABB(-2, -2, -2, 2, 2, 2).move(getX(), getY() + 0.5, getZ());
        }

        private record Sorter(Entity self) implements Comparator<Entity> {
            @Override
            public int compare(Entity a, Entity b) {
                return Double.compare(self.distanceToSqr(a), self.distanceToSqr(b));
            }
        }
    }

    private final class DeliveryGoal extends Goal {
        DeliveryGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return isDelivering();
        }

        @Override
        public boolean canContinueToUse() {
            return isDelivering();
        }

        @Override
        public void tick() {
            tickDelivery();
        }

        @Override
        public void stop() {
            getNavigation().stop();
            getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0F);
            super.stop();
        }
    }

    private final class FetchBundleGoal extends Goal {
        private static final int SEARCH_RADIUS = 24;
        private static final int SEARCH_HEIGHT = 8;
        private static final int SCAN_INTERVAL = 10;
        private static final int MAX_FETCH_TICKS = 300;
        private static final int ABANDON_COOLDOWN = 1200;
        private static final double PICKUP_DISTANCE_SQR = 2.25D;

        @Nullable
        private UUID fetchItemUuid;
        @Nullable
        private UUID ignoredItemUuid;
        private long ignoredUntil;
        private int scanCooldown;
        private int repathCooldown;
        private int fetchTicks;

        FetchBundleGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (level().isClientSide() || !isTame() || isDelivering() || isVehicle()
                    || isLeashed() || !getMainHandItem().isEmpty()) {
                return false;
            }
            if (getTarget() != null && getTarget().isAlive()) {
                return false;
            }
            if (ignoredItemUuid != null && level().getGameTime() >= ignoredUntil) {
                ignoredItemUuid = null;
            }
            if (--scanCooldown > 0) {
                return false;
            }
            scanCooldown = SCAN_INTERVAL;

            ItemEntity item = findNearestOwnerBundle();
            if (item == null) {
                return false;
            }
            fetchItemUuid = item.getUUID();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (level().isClientSide() || !isTame() || isDelivering() || isVehicle()
                    || isLeashed() || !getMainHandItem().isEmpty()
                    || (getTarget() != null && getTarget().isAlive())) {
                return false;
            }
            ItemEntity item = getFetchItem();
            return item != null && isValidOwnerBundle(item, getOwnerUuid());
        }

        @Override
        public void start() {
            if (isPassenger()) {
                stopRiding();
            }
            setOrderedToSit(false);
            setFlying(true);
            fetching = true;
            repathCooldown = 0;
            fetchTicks = 0;
            getNavigation().stop();
        }

        @Override
        public void stop() {
            fetching = false;
            fetchItemUuid = null;
            getNavigation().stop();
            getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0F);
            super.stop();
        }

        @Override
        public void tick() {
            ItemEntity item = getFetchItem();
            if (item == null) {
                stop();
                return;
            }
            if (++fetchTicks > MAX_FETCH_TICKS) {
                abandonFetch();
                return;
            }

            if (distanceToSqr(item.position()) > PICKUP_DISTANCE_SQR) {
                if (--repathCooldown <= 0) {
                    repathCooldown = 5;
                    setFlying(true);
                    getMoveControl().setWantedPosition(
                            item.getX(), item.getY() + item.getBbHeight() * 0.5D, item.getZ(), 1.0F);
                }
                return;
            }

            ItemStack bundle = item.getItem().copyWithCount(1);
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) {
                item.discard();
            }
            setItemInHand(InteractionHand.MAIN_HAND, bundle);
            heldItemTime = 0;
            feederId = null;

            if (level() instanceof ServerLevel server) {
                UUID target = resolveDeliveryTarget(server, bundle);
                if (target != null) {
                    startDelivery(server, target);
                }
            }
            stop();
        }

        private void abandonFetch() {
            if (fetchItemUuid != null) {
                ignoredItemUuid = fetchItemUuid;
                ignoredUntil = level().getGameTime() + ABANDON_COOLDOWN;
            }
            stop();
        }

        @Nullable
        private ItemEntity findNearestOwnerBundle() {
            UUID ownerId = getOwnerUuid();
            if (ownerId == null || !(level() instanceof ServerLevel server)) {
                return null;
            }
            AABB area = getBoundingBox().inflate(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS);
            List<ItemEntity> items = server.getEntitiesOfClass(ItemEntity.class, area,
                    item -> isValidOwnerBundle(item, ownerId));
            return items.stream()
                    .min(Comparator.comparingDouble(CrowEntity.this::distanceToSqr))
                    .orElse(null);
        }

        @Nullable
        private ItemEntity getFetchItem() {
            if (fetchItemUuid == null || !(level() instanceof ServerLevel server)) {
                return null;
            }
            Entity entity = server.getEntity(fetchItemUuid);
            return entity instanceof ItemEntity item ? item : null;
        }

        private boolean isValidOwnerBundle(ItemEntity item, @Nullable UUID ownerId) {
            if (ownerId == null || item.isRemoved() || item.getItem().isEmpty() || item.isInLava()
                    || !isDeliveryBundle(item.getItem())) {
                return false;
            }
            if (ignoredItemUuid != null && item.getUUID().equals(ignoredItemUuid)
                    && level().getGameTime() < ignoredUntil) {
                return false;
            }
            Entity thrower = item.getOwner();
            return thrower != null && thrower.getUUID().equals(ownerId);
        }
    }

    private enum DeliveryState {
        IDLE,
        LEAVING,
        WAITING,
        DROPPING,
        RETURN_LEAVING
    }

    private static final class AnimalCheck {
        private AnimalCheck() {
        }

        static boolean isBrightEnough(LevelAccessor level, BlockPos pos) {
            return level.getRawBrightness(pos, 0) > 8;
        }
    }
}
