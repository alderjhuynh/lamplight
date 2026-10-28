package com.aura.lamplight.menagerie.raccoon;

import com.aura.lamplight.menagerie.raccoon.ai.RaccoonDigGoal;
import com.aura.lamplight.menagerie.raccoon.ai.RaccoonTargetItemsGoal;
import com.aura.lamplight.menagerie.raccoon.ai.RaccoonWanderRangedGoal;
import com.aura.lamplight.menagerie.raccoon.ai.RaccoonWashGoal;
import com.aura.lamplight.menagerie.registry.MenagerieSounds;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("all")
public class RaccoonEntity extends Animal {

    private static final EntityDataAccessor<Boolean> STANDING = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WASHING = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> EATING = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> WASH_POS = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> ATTACK_TICK = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.INT);

    public static final int ATTACK_ANIMATION_LENGTH = 12;
    public static final float ATTACK_ANIMATION_DAMAGE_TICK = 5F;

    private static final int EAT_SESSION_BASE = 70;
    private static final int EAT_SESSION_VARIANCE = 50;
    private static final float CHEW_SPEED = 0.4F;
    private static final float CHEW_CYCLE = (float) (Math.PI * 2.0D);

    public float prevStandProgress;
    public float standProgress;
    public float prevWashProgress;
    public float washProgress;
    public float prevEatProgress;
    public float eatProgress;
    public int maxStandTime = 75;
    private int standingTime = 0;
    public int lookForWaterBeforeEatingTimer = 0;
    private int pickupItemCooldown = 0;
    private int eatTimer = 0;
    private float chewPhase = 0.0F;

    public RaccoonEntity(EntityType<? extends RaccoonEntity> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        setDropChance(EquipmentSlot.MAINHAND, 1.0F);
    }

    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MenagerieSounds.RACCOON_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return MenagerieSounds.RACCOON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MenagerieSounds.RACCOON_HURT;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new RaccoonWashGoal(this));
        this.goalSelector.addGoal(3, new RaccoonDigGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(7, new RaccoonWanderRangedGoal(this, 120, 1.0D, 14, 7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 15.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RaccoonTargetItemsGoal(this, false));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
        beginAttack();
        return true;
    }

    public void beginAttack() {
        if (getAttackTick() == 0) {
            this.entityData.set(ATTACK_TICK, ATTACK_ANIMATION_LENGTH);
            this.maxStandTime = 15;
            this.setStanding(true);
        }
    }

    public int getAttackTick() {
        return this.entityData.get(ATTACK_TICK);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MenagerieTags.RACCOON_BREEDABLES);
    }

    public boolean isRaccoonFood(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(MenagerieTags.RACCOON_FOODSTUFFS);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        InteractionResult type = super.mobInteract(player, hand);
        if (type == InteractionResult.SUCCESS) {
            return type;
        }
        if (!this.getMainHandItem().isEmpty()) {
            if (!this.level().isClientSide() && this.level() instanceof ServerLevel server) {
                this.spawnAtLocation(server, this.getMainHandItem().copy());
            }
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            this.pickupItemCooldown = 60;
            if (!this.level().isClientSide()) {
                this.endEatSession();
            }
            return InteractionResult.SUCCESS;
        }
        if (this.isRaccoonFood(itemstack) && !this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
            if (this.getMainHandItem().isEmpty()) {
                ItemStack copy = itemstack.copy();
                copy.setCount(1);
                this.setItemInHand(InteractionHand.MAIN_HAND, copy);
                this.lookForWaterBeforeEatingTimer = 0;
                this.beginEatSession();
                if (!player.isCreative()) {
                    itemstack.shrink(1);
                }
            } else {
                this.gameEvent(GameEvent.EAT);
                this.playSound(SoundEvents.GENERIC_EAT.value(), this.getSoundVolume(), this.getVoicePitch());
                this.heal(5);
            }
            this.usePlayerItem(player, hand, itemstack);
            return InteractionResult.SUCCESS;
        }
        return type;
    }

    private void giveCraftingRemainder(ItemStack stack) {
        ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
        if (remainder != null) {
            ItemStack rest = remainder.create();
            if (!rest.isEmpty() && this.level() instanceof ServerLevel server) {
                this.spawnAtLocation(server, rest);
            }
        }
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 9.0D).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.MOVEMENT_SPEED, 0.25F);
    }

    @Override
    public void tick() {
        super.tick();
        this.prevStandProgress = this.standProgress;
        this.prevWashProgress = this.washProgress;
        this.prevEatProgress = this.eatProgress;

        if (this.isStanding()) {
            if (this.standProgress < 5F) {
                this.standProgress++;
            }
        } else if (this.standProgress > 0F) {
            this.standProgress--;
        }

        if (this.isWashing()) {
            if (this.washProgress < 5F) {
                this.washProgress++;
            }
        } else if (this.washProgress > 0F) {
            this.washProgress--;
        }

        if (this.isEating()) {
            if (this.eatProgress < 5F) {
                this.eatProgress++;
            }
        } else if (this.eatProgress > 0F) {
            this.eatProgress--;
        }

        if (this.isStanding() && !this.isEating() && ++this.standingTime > this.maxStandTime) {
            this.setStanding(false);
            this.standingTime = 0;
            this.maxStandTime = 75 + this.random.nextInt(50);
        }
        if (!this.level().isClientSide()) {
            if (this.isEating()) {
                ItemStack held = this.getMainHandItem();
                if (held.isEmpty()) {
                    this.endEatSession();
                } else if (--this.eatTimer > 0) {
                    if (this.crossedChewTop()) {
                        this.nibbleHeldItem();
                    }
                } else {
                    this.consumeHeldItem();
                    if (this.getMainHandItem().isEmpty()) {
                        this.endEatSession();
                    } else {
                        this.eatTimer = this.newEatTimer();
                    }
                }
            } else if (this.lookForWaterBeforeEatingTimer > 0) {
                this.lookForWaterBeforeEatingTimer--;
                if (this.lookForWaterBeforeEatingTimer == 0 && !this.isWashing() && this.canTargetItem(this.getMainHandItem())) {
                    this.beginEatSession();
                }
            }
        }
        if (this.isWashing()) {
            BlockPos washPos = this.getWashPos();
            if (washPos != null) {
                if (this.distanceToSqr(washPos.getX() + 0.5D, washPos.getY() + 0.5D, washPos.getZ() + 0.5D) < 3) {
                    for (int j = 0; (float) j < 4; ++j) {
                        double d2 = this.random.nextDouble();
                        double d3 = this.random.nextDouble();
                        Vec3 motion = this.getDeltaMovement();
                        this.level().addParticle(ParticleTypes.SPLASH, washPos.getX() + d2, washPos.getY() + 0.8F, washPos.getZ() + d3, motion.x, motion.y, motion.z);
                    }
                } else {
                    this.setWashing(false);
                }
            }
        }
        int attackTick = this.getAttackTick();
        if (attackTick > 0) {
            this.entityData.set(ATTACK_TICK, attackTick - 1);
            int elapsed = ATTACK_ANIMATION_LENGTH - attackTick;
            if (elapsed == (int) ATTACK_ANIMATION_DAMAGE_TICK && this.level() instanceof ServerLevel server) {
                net.minecraft.world.entity.LivingEntity target = this.getTarget();
                if (target != null && this.hasLineOfSight(target) && this.distanceTo(target) < 4) {
                    float yawRad = this.getYRot() * (float) (Math.PI / 180);
                    this.setDeltaMovement(this.getDeltaMovement().add(-net.minecraft.util.Mth.sin(yawRad) * -0.06F, 0.0D, net.minecraft.util.Mth.cos(yawRad) * -0.06F));
                    double dx = target.getX() - this.getX();
                    double dz = target.getZ() - this.getZ();
                    Vec3 push = new Vec3(dx, 0.0D, dz).normalize().scale(0.35D);
                    target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.0D, push.z));
                    target.hurtServer(server, server.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                }
            }
        }
        if (this.pickupItemCooldown > 0) {
            this.pickupItemCooldown--;
        }
    }

   public void beginEatSession() {
        if (this.level().isClientSide() || this.isEating() || this.isWashing() || this.getMainHandItem().isEmpty()) {
            return;
        }
        this.setEating(true);
        this.setStanding(true);
        this.standingTime = 0;
        this.eatTimer = this.newEatTimer();
        this.chewPhase = Float.NaN;
    }

    public void endEatSession() {
        this.setEating(false);
        this.standingTime = 0;
        this.maxStandTime = 75 + this.random.nextInt(50);
    }

    private int newEatTimer() {
        return EAT_SESSION_BASE + this.random.nextInt(EAT_SESSION_VARIANCE);
    }

   private boolean crossedChewTop() {
        float phase = Mth.positiveModulo(this.tickCount * CHEW_SPEED, CHEW_CYCLE);
        boolean wrapped = phase < this.chewPhase;
        this.chewPhase = phase;
        return wrapped;
    }

    public void nibbleHeldItem() {
        this.level().broadcastEntityEvent(this, (byte) 92);
        this.gameEvent(GameEvent.EAT);
        this.playSound(SoundEvents.GENERIC_EAT.value(), this.getSoundVolume(), this.getVoicePitch());
    }
    
    private void consumeHeldItem() {
        ItemStack held = this.getMainHandItem();
        this.level().broadcastEntityEvent(this, (byte) 92);
        this.level().broadcastEntityEvent(this, (byte) 92);
        this.gameEvent(GameEvent.EAT);
        this.playSound(SoundEvents.PLAYER_BURP, this.getSoundVolume(), 0.85F + this.random.nextFloat() * 0.2F);
        this.heal(10);
        this.giveCraftingRemainder(held);
        held.shrink(1);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 92) {
            for (int i = 0; i < 6 + this.random.nextInt(3); i++) {
                double d2 = this.random.nextGaussian() * 0.02D;
                double d0 = this.random.nextGaussian() * 0.02D;
                double d1 = this.random.nextGaussian() * 0.02D;
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItemInHand(InteractionHand.MAIN_HAND).getItem()),
                        this.getX() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() * 0.5F,
                        this.getY() + this.getBbHeight() * 0.5F + this.random.nextFloat() * this.getBbHeight() * 0.5F,
                        this.getZ() + this.random.nextFloat() * this.getBbWidth() - this.getBbWidth() * 0.5F,
                        d0, d1, d2);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    public boolean isStanding() {
        return this.entityData.get(STANDING);
    }

    public void setStanding(boolean standing) {
        this.entityData.set(STANDING, standing);
    }

    public boolean isWashing() {
        return this.entityData.get(WASHING);
    }

    public void setWashing(boolean washing) {
        this.entityData.set(WASHING, washing);
    }

    public boolean isEating() {
        return this.entityData.get(EATING);
    }

    public void setEating(boolean eating) {
        this.entityData.set(EATING, eating);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STANDING, false);
        builder.define(WASHING, false);
        builder.define(EATING, false);
        builder.define(WASH_POS, Optional.empty());
        builder.define(ATTACK_TICK, 0);
    }

    @Nullable
    public BlockPos getWashPos() {
        return this.entityData.get(WASH_POS).orElse(null);
    }

    public void setWashPos(@Nullable BlockPos washingPos) {
        this.entityData.set(WASH_POS, Optional.ofNullable(washingPos));
    }

    public boolean canTargetItem(ItemStack stack) {
        return this.isRaccoonFood(stack) && this.pickupItemCooldown == 0;
    }

    public void onGetItem(ItemEntity item) {
        this.lookForWaterBeforeEatingTimer = 100;
        ItemStack duplicate = item.getItem().copy();
        duplicate.setCount(1);
        if (!this.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && !this.level().isClientSide() && this.level() instanceof ServerLevel server) {
            this.spawnAtLocation(server, this.getItemInHand(InteractionHand.MAIN_HAND), 0.0F);
        }
        this.setItemInHand(InteractionHand.MAIN_HAND, duplicate);
    }

    public double getMaxDistToItem() {
        return 2.0D;
    }

    @Override
    public void travel(Vec3 vec) {
        if (this.isWashing() || this.isEating()) {
            if (this.getNavigation().getPath() != null) {
                this.getNavigation().stop();
            }
            vec = Vec3.ZERO;
        }
        super.travel(vec);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob partner) {
        return com.aura.lamplight.menagerie.registry.MenagerieEntities.RACCOON.create(serverLevel, EntitySpawnReason.BREEDING);
    }
}
