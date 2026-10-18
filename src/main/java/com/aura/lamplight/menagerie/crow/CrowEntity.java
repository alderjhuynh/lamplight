package com.aura.lamplight.menagerie.crow;

import com.aura.lamplight.menagerie.crow.ai.CrowCircleCropsGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowFollowOwnerGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowMeleeGoal;
import com.aura.lamplight.menagerie.crow.ai.CrowShoulderGoal;
import com.aura.lamplight.menagerie.crow.ai.DirectNavigator;
import com.aura.lamplight.menagerie.crow.ai.FlightMoveControl;
import com.aura.lamplight.menagerie.crow.ai.TargetItemsGoal;
import com.aura.lamplight.menagerie.registry.MenagerieEntities;
import com.aura.lamplight.menagerie.registry.MenagerieSounds;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import com.google.common.base.Predicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
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
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CrowEntity extends ShoulderRidingEntity implements TargetsDroppedItems {
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> COMMAND = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> PERCH_POS = SynchedEntityData.defineId(CrowEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public float prevFlyProgress;
    public float flyProgress;
    public float prevAttackProgress;
    public float attackProgress;
    public float prevSitProgress;
    public float sitProgress;
    public int fleePumpkinFlag;
    public boolean aiItemFlag;
    public boolean aiItemFrameFlag;

    private boolean landNavigator;
    private int timeFlying;
    private int heldItemTime;
    private int perchCooldown;
    @Nullable
    private UUID seedThrowerId;
    private final boolean gatheringClockwise = false;

    public CrowEntity(EntityType<? extends CrowEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(PathType.FIRE, -1.0F);
        setPathfindingMalus(PathType.WATER, -1.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        setPathfindingMalus(PathType.COCOA, -1.0F);
        setPathfindingMalus(PathType.FENCE, -1.0F);
        switchNavigator(false);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
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
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        goalSelector.addGoal(2, new CrowMeleeGoal(this));
        goalSelector.addGoal(2, new CrowShoulderGoal(this));
        goalSelector.addGoal(3, new CrowFollowOwnerGoal(this, 1.0D, 4.0F, 2.0F, true));
        goalSelector.addGoal(4, new DepositChestsGoal());
        goalSelector.addGoal(4, new ScatterGoal());
        goalSelector.addGoal(5, new AvoidPumpkinsGoal());
        goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(6, new CrowCircleCropsGoal(this));
        goalSelector.addGoal(7, new WalkIdleGoal());
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, PathfinderMob.class, 6.0F));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new CrowTargetItemsGoal(this, false, false, 40, 16));
        targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(4, new HurtByTargetGoal(this, Player.class).setAlertOthers());
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

    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return false;
    }

    public boolean causeFallDamage(float distance, float multiplier) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isInvulnerableTo(level, source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        setOrderedToSit(false);
        float finalAmount = amount;
        if (attacker != null && isTame() && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) {
            finalAmount = (amount + 1.0F) / 4.0F;
        }
        boolean hurt = super.hurtServer(level, source, finalAmount);
        if (hurt && !getMainHandItem().isEmpty()) {
            spawnAtLocation(level, getMainHandItem().copy());
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        return hurt;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MenagerieTags.CROW_BREEDABLES) && isTame();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        InteractionResult result = super.mobInteract(player, hand);
        if (!getMainHandItem().isEmpty() && result != InteractionResult.SUCCESS) {
            if (!level().isClientSide() && level() instanceof ServerLevel server) {
                spawnAtLocation(server, getMainHandItem().copy());
            }
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        InteractionResult use = held.interactLivingEntity(player, this, hand);
        if (use != InteractionResult.SUCCESS && result != InteractionResult.SUCCESS
                && isTame() && isOwnedBy(player) && !isFood(held)) {
            if (isCrowEdible(held) && getMainHandItem().isEmpty()) {
                ItemStack one = held.copy();
                one.setCount(1);
                setItemInHand(InteractionHand.MAIN_HAND, one);
                held.shrink(1);
            }
            setCommand((getCommand() + 1) % 4);
            if (getCommand() == 3) {
                player.sendOverlayMessage(Component.translatable("entity.lamplight.crow.command_3", getName()));
            } else {
                player.sendOverlayMessage(Component.translatable("entity.lamplight.all.command_" + getCommand(), getName()));
            }
            setOrderedToSit(getCommand() == 2);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void tick() {
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
        if (fleePumpkinFlag > 0) {
            fleePumpkinFlag--;
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
        }

        if (!getMainHandItem().isEmpty()) {
            heldItemTime++;
            if (heldItemTime > 60 && isCrowEdible(getMainHandItem()) && (!isTame() || getHealth() < getMaxHealth())) {
                heldItemTime = 0;
                heal(4);
                gameEvent(GameEvent.EAT);
                playSound(SoundEvents.PARROT_EAT, getSoundVolume(), getVoicePitch());
                if (seedThrowerId != null && getMainHandItem().is(MenagerieTags.CROW_TAMEABLES) && !isTame()) {
                    if (getRandom().nextFloat() < 0.3F) {
                        setTame(true, true);
                        setCommand(1);
                        setOwnerReference(EntityReference.of(seedThrowerId));
                        if (level() instanceof ServerLevel server) {
                            Player feeder = server.getPlayerInAnyDimension(seedThrowerId);
                            if (feeder instanceof ServerPlayer serverPlayer) {
                                CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
                            }
                        }
                        level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                ItemStackTemplate remainder = getMainHandItem().getItem().getCraftingRemainder();
                if (remainder != null && level() instanceof ServerLevel server) {
                    ItemStack rest = remainder.create();
                    if (!rest.isEmpty()) {
                        spawnAtLocation(server, rest);
                    }
                }
                getMainHandItem().shrink(1);
            }
        } else {
            heldItemTime = 0;
        }
        if (entityData.get(ATTACK_TICK) > 0) {
            entityData.set(ATTACK_TICK, entityData.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else if (attackProgress > 0F) {
            attackProgress--;
        }
        if (perchCooldown > 0) {
            perchCooldown--;
        }
        if (isTame() && perchCooldown == 0) {
            perchCooldown = 50;
            if (getBlockStateOn().is(MenagerieTags.CROW_HOME_BLOCKS)) {
                heal(1);
                level().broadcastEntityEvent(this, (byte) 67);
                setPerchPos(getBlockPosBelowThatAffectsMyMovement());
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 67 || id == 68) {
            var particle = id == 67 ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER;
            for (int i = 0; i < 7; i++) {
                level().addParticle(particle, getRandomX(1.0D), getRandomY() + 0.5D, getRandomZ(1.0D),
                        random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D, random.nextGaussian() * 0.02D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Flying", isFlying());
        output.putBoolean("CrowSitting", isSitting());
        output.putInt("Command", getCommand());
        BlockPos perch = getPerchPos();
        if (perch != null) {
            output.putInt("PerchX", perch.getX());
            output.putInt("PerchY", perch.getY());
            output.putInt("PerchZ", perch.getZ());
        }
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
        setOrderedToSit(input.getBooleanOr("CrowSitting", false));
        setCommand(input.getIntOr("Command", 0));
        if (input.getInt("PerchX").isPresent() && input.getInt("PerchY").isPresent() && input.getInt("PerchZ").isPresent()) {
            setPerchPos(new BlockPos(input.getIntOr("PerchX", 0), input.getIntOr("PerchY", 0), input.getIntOr("PerchZ", 0)));
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

    public int getCommand() {
        return entityData.get(COMMAND);
    }

    public void setCommand(int command) {
        entityData.set(COMMAND, command);
    }

    public boolean isSitting() {
        return entityData.get(SITTING);
    }

    @Override
    public void setOrderedToSit(boolean sit) {
        entityData.set(SITTING, sit);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
        builder.define(ATTACK_TICK, 0);
        builder.define(COMMAND, 0);
        builder.define(SITTING, false);
        builder.define(PERCH_POS, Optional.empty());
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS)
                || super.isInvulnerableTo(level, source);
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
        while (pos.getY() > -64 && !level().getBlockState(pos).isSolid() && level().getFluidState(pos).isEmpty()) {
            pos = pos.below();
        }
        return pos;
    }

    public Vec3 getBlockGrounding(Vec3 fleePos) {
        float radius = 3.15F * -3 - getRandom().nextInt(24);
        float angle = crowAngle();
        BlockPos radial = new BlockPos((int) (fleePos.x() + radius * Mth.sin(Mth.PI + angle)), (int) getY(),
                (int) (fleePos.z() + radius * Mth.cos(angle)));
        BlockPos ground = crowGround(radial);
        if (ground.getY() == -64) {
            return position();
        }
        ground = blockPosition();
        while (ground.getY() > -64 && !level().getBlockState(ground).isSolid()) {
            ground = ground.below();
        }
        if (!isTargetBlocked(Vec3.atCenterOf(ground.above()))) {
            return Vec3.atCenterOf(ground);
        }
        return null;
    }

    private float crowAngle() {
        float neg = getRandom().nextBoolean() ? 1 : -1;
        return CrowMaths.STARTING_ANGLE * yBodyRot + 3.15F + getRandom().nextFloat() * neg;
    }

    private boolean isOverWater() {
        BlockPos pos = blockPosition();
        while (pos.getY() > -64 && level().isEmptyBlock(pos)) {
            pos = pos.below();
        }
        return !level().getFluidState(pos).isEmpty();
    }

    public void peck() {
        entityData.set(ATTACK_TICK, 7);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (isCrowEdible(stack) || isTame());
    }

    private boolean isCrowEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(MenagerieTags.CROW_FOODSTUFFS);
    }

    @Override
    public double getMaxDistToItem() {
        return 1.0D;
    }

    @Override
    public void onGetItem(ItemEntity entity) {
        ItemStack duplicate = entity.getItem().copy();
        duplicate.setCount(1);
        if (!getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && !level().isClientSide()
                && level() instanceof ServerLevel server) {
            spawnAtLocation(server, getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
        }
        setItemInHand(InteractionHand.MAIN_HAND, duplicate);
        Entity thrower = entity.getOwner();
        if (entity.getItem().is(MenagerieTags.CROW_TAMEABLES) && !isTame() && thrower != null) {
            seedThrowerId = thrower.getUUID();
        } else {
            seedThrowerId = null;
        }
    }

    @Nullable
    public BlockPos getPerchPos() {
        return entityData.get(PERCH_POS).orElse(null);
    }

    public void setPerchPos(@Nullable BlockPos pos) {
        entityData.set(PERCH_POS, Optional.ofNullable(pos));
    }

    public Vec3 gatheringVec(Vec3 origin, float dist) {
        if (getPerchPos() == null) {
            return null;
        }
        float angle = CrowMaths.EIGHT_STARTING_ANGLE * (gatheringClockwise ? -tickCount : tickCount);
        Vec3 pos = new Vec3(getPerchPos().getX() + dist * Mth.sin(angle), getPerchPos().getY() + 2,
                getPerchPos().getZ() + dist * Mth.cos(angle));
        if (level().isEmptyBlock(CrowBlockPos.fromVec3(pos))) {
            return pos;
        }
        return null;
    }

    private static ItemStack insertInto(Container container, Direction side, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            if (!container.canPlaceItem(slot, remaining)) {
                continue;
            }
            if (container instanceof WorldlyContainer worldly && !worldly.canPlaceItemThroughFace(slot, remaining, side)) {
                continue;
            }
            ItemStack existing = container.getItem(slot);
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, remaining)) {
                continue;
            }
            int max = Math.min(container.getMaxStackSize(), remaining.getMaxStackSize());
            int room = existing.isEmpty() ? max : max - existing.getCount();
            if (room <= 0) {
                continue;
            }
            int move = Math.min(room, remaining.getCount());
            if (!simulate) {
                if (existing.isEmpty()) {
                    ItemStack placed = remaining.copy();
                    placed.setCount(move);
                    container.setItem(slot, placed);
                } else {
                    existing.grow(move);
                    container.setItem(slot, existing);
                }
            }
            remaining.shrink(move);
        }
        if (!simulate) {
            container.setChanged();
        }
        return remaining;
    }

    private final class WalkIdleGoal extends Goal {
        private final CrowEntity crow = CrowEntity.this;
        private double x;
        private double y;
        private double z;
        private boolean flightTarget;

        WalkIdleGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (crow.isVehicle() || getCommand() == 1 || aiItemFlag || crow.isPassenger() || crow.isSitting()
                    || (crow.getTarget() != null && crow.getTarget().isAlive())) {
                return false;
            }
            if (crow.getRandom().nextInt(30) != 0 && !crow.isFlying()) {
                return false;
            }
            if (crow.onGround()) {
                flightTarget = random.nextBoolean();
            } else {
                flightTarget = random.nextInt(5) > 0 && timeFlying < 200;
            }
            if (crow.getCommand() == 3) {
                if (crow.aiItemFrameFlag) {
                    return false;
                }
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
            if (crow.getCommand() == 3 && crow.getPerchPos() != null) {
                return gatheringVec(origin, 4 + random.nextInt(2));
            }
            if (crow.isOverWater()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (timeFlying < 50 || crow.isOverWater()) {
                    return crow.getBlockInViewAway(origin, 0);
                }
                return crow.getBlockGrounding(origin);
            }
            return LandRandomPos.getPos(crow, 10, 7);
        }

        @Override
        public boolean canContinueToUse() {
            if (aiItemFlag || crow.isSitting() || getCommand() == 1) {
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
                return entity instanceof Player player && !player.isCreative() && !player.isSpectator();
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
            if (isPassenger() || aiItemFlag || isVehicle() || isTame()) {
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
            return target != null && !isTame();
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

    private final class AvoidPumpkinsGoal extends Goal {
        private BlockPos destination;
        private int delay = 70;
        private Vec3 flightTarget;

        @Override
        public boolean canContinueToUse() {
            return destination != null && isPumpkin(level(), destination.mutable()) && closeToPumpkin(16);
        }

        private boolean closeToPumpkin(double dist) {
            return destination == null || distanceToSqr(Vec3.atCenterOf(destination)) < dist * dist;
        }

        @Override
        public boolean canUse() {
            if (isTame()) {
                return false;
            }
            if (delay > 0) {
                delay--;
                return false;
            }
            delay = 70 + random.nextInt(150);
            return search();
        }

        @Override
        public void start() {
            fleePumpkinFlag = 200;
            Vec3 away = getBlockInViewAway(Vec3.atCenterOf(destination), 10);
            if (away != null) {
                flightTarget = away;
                setFlying(true);
                getMoveControl().setWantedPosition(away.x, away.y, away.z, 1F);
            }
        }

        @Override
        public void tick() {
            if (closeToPumpkin(16)) {
                fleePumpkinFlag = 200;
                if (flightTarget == null || distanceToSqr(flightTarget) < 2F) {
                    Vec3 away = getBlockInViewAway(Vec3.atCenterOf(destination), 10);
                    if (away != null) {
                        flightTarget = away;
                        setFlying(true);
                    }
                }
                if (flightTarget != null) {
                    getMoveControl().setWantedPosition(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                }
            }
        }

        @Override
        public void stop() {
            flightTarget = null;
        }

        private boolean search() {
            BlockPos origin = blockPosition();
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dy = -8; dy <= 2; dy++) {
                for (int range = 0; range < 20; range++) {
                    for (int dx = 0; dx <= range; dx = dx > 0 ? -dx : 1 - dx) {
                        for (int dz = dx < range && dx > -range ? range : 0; dz <= range; dz = dz > 0 ? -dz : 1 - dz) {
                            cursor.setWithOffset(origin, dx, dy - 1, dz);
                            if (isPumpkin(level(), cursor)) {
                                destination = cursor.immutable();
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        private boolean isPumpkin(Level level, BlockPos.MutableBlockPos pos) {
            return level.getBlockState(pos).is(MenagerieTags.CROW_FEARS);
        }
    }

    private static final class CrowTargetItemsGoal extends TargetItemsGoal {
        CrowTargetItemsGoal(PathfinderMob mob, boolean checkSight, boolean onlyNearby, int threshold, int radius) {
            super(mob, checkSight, onlyNearby, threshold, radius);
            this.executionChance = 1;
        }

        @Override
        public void stop() {
            super.stop();
            ((CrowEntity) mob).aiItemFlag = false;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !((CrowEntity) mob).isSitting()
                    && (mob.getTarget() == null || !mob.getTarget().isAlive());
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && !((CrowEntity) mob).isSitting()
                    && (mob.getTarget() == null || !mob.getTarget().isAlive());
        }

        @Override
        protected void moveTo() {
            CrowEntity crow = (CrowEntity) mob;
            if (targetEntity == null) {
                return;
            }
            crow.aiItemFlag = true;
            if (mob.distanceTo(targetEntity) < 2) {
                crow.getMoveControl().setWantedPosition(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), 1);
                crow.peck();
            }
            if (mob.distanceTo(targetEntity) > 8 || crow.isFlying()) {
                crow.setFlying(true);
                if (!crow.hasLineOfSight(targetEntity)) {
                    crow.getMoveControl().setWantedPosition(targetEntity.getX(), 1 + crow.getY(), targetEntity.getZ(), 1);
                } else {
                    float dx = (float) (crow.getX() - targetEntity.getX());
                    float dz = (float) (crow.getZ() - targetEntity.getZ());
                    float horizontal = Mth.sqrt(dx * dx + dz * dz);
                    crow.getMoveControl().setWantedPosition(targetEntity.getX(),
                            targetEntity.getY() + (horizontal < 5F ? 0 : 1.8F), targetEntity.getZ(), 1);
                }
            } else {
                mob.getNavigation().moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(), 1);
            }
        }

        @Override
        public void tick() {
            super.tick();
            moveTo();
        }
    }

    private final class DepositChestsGoal extends Goal {
        private final Sorter sorter = new Sorter(CrowEntity.this);
        private final Predicate<ItemFrame> selector = new Predicate<>() {
            @Override
            public boolean apply(@Nullable ItemFrame frame) {
                if (frame == null) {
                    return false;
                }
                BlockPos behind = frame.getPos().relative(frame.getDirection().getOpposite());
                return frame.level().getBlockEntity(behind) instanceof Container
                        && ItemStack.isSameItem(frame.getItem(), getMainHandItem());
            }
        };
        private ItemFrame target;
        private Vec3 flightTarget;
        private int cooldown;

        DepositChestsGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (isPassenger() || aiItemFlag || isVehicle() || isSitting() || getCommand() != 3 || getMainHandItem().isEmpty()) {
                return false;
            }
            if (getRandom().nextInt(8) != 0 && level().getGameTime() % 10 != 0 && getNoActionTime() >= 100) {
                return false;
            }
            List<ItemFrame> frames = level().getEntitiesOfClass(ItemFrame.class, targetArea(), selector);
            if (frames.isEmpty()) {
                return false;
            }
            frames.sort(sorter);
            target = frames.get(0);
            aiItemFrameFlag = true;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && getCommand() == 3 && !getMainHandItem().isEmpty();
        }

        @Override
        public void stop() {
            flightTarget = null;
            target = null;
            aiItemFrameFlag = false;
        }

        @Override
        public void tick() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (flightTarget != null) {
                setFlying(true);
                if (horizontalCollision) {
                    getMoveControl().setWantedPosition(flightTarget.x, getY() + 1F, flightTarget.z, 1F);
                } else {
                    getMoveControl().setWantedPosition(flightTarget.x, flightTarget.y, flightTarget.z, 1F);
                }
            }
            if (target == null) {
                return;
            }
            flightTarget = target.position();
            if (distanceTo(target) >= 2.0F) {
                return;
            }
            try {
                BlockPos behind = target.getPos().relative(target.getDirection().getOpposite());
                BlockEntity blockEntity = target.level().getBlockEntity(behind);
                Direction face = target.getDirection();
                if (blockEntity instanceof Container container && cooldown == 0 && level() instanceof ServerLevel) {
                    ItemStack held = getItemInHand(InteractionHand.MAIN_HAND).copy();
                    if (!insertInto(container, face, held, true).equals(held)) {
                        ItemStack rest = insertInto(container, face, held, false);
                        setItemInHand(InteractionHand.MAIN_HAND, rest);
                        peck();
                    } else {
                        cooldown = 20;
                    }
                }
            } catch (Exception ignored) {
            }
            stop();
        }

        private AABB targetArea() {
            return new AABB(-16, -16, -16, 16, 16, 16).move(getX(), getY(), getZ());
        }

        private record Sorter(Entity self) implements Comparator<Entity> {
            @Override
            public int compare(Entity a, Entity b) {
                return Double.compare(self.distanceToSqr(a), self.distanceToSqr(b));
            }
        }
    }

    private static final class AnimalCheck {
        private AnimalCheck() {
        }

        static boolean isBrightEnough(LevelAccessor level, BlockPos pos) {
            return level.getRawBrightness(pos, 0) > 8;
        }
    }
}
