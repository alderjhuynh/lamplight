package com.aura.lamplight.menagerie.wisp;

import com.aura.lamplight.menagerie.crow.ai.DirectNavigator;
import com.aura.lamplight.menagerie.effect.MenagerieEffects;
import com.aura.lamplight.menagerie.registry.MenagerieSounds;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import com.aura.lamplight.menagerie.wisp.ai.WispCuriosityGoal;
import com.aura.lamplight.menagerie.wisp.ai.WispDriftGoal;
import com.aura.lamplight.menagerie.wisp.ai.WispFollowOwnerGoal;
import com.aura.lamplight.menagerie.wisp.ai.WispShoalGoal;
import com.aura.lamplight.menagerie.wisp.ai.WispStartleGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class WispEntity extends TamableAnimal {
    public static final int PULSE_CYCLE = 40;
    public static final int TAME_TRUST_REQUIRED = 3;
    public static final int MAX_WARMTH = 100;

    private static final EntityDataAccessor<Integer> WARMTH =
            SynchedEntityData.defineId(WispEntity.class, EntityDataSerializers.INT);

    private int trust;
    private int warmthTimer;
    private int auraTimer;
    private boolean nearWarmthSource;
    private int startleTicks;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.moveControl = new WispPulseControl(this);
        this.navigation = new DirectNavigator(this, level);
    }

    public static AttributeSupplier.Builder bakeAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static <T extends Mob> boolean canWispSpawn(
            EntityType<WispEntity> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir()
                || !level.getBlockState(pos.above()).isAir()
                || !level.getBlockState(pos.above(2)).isAir()
                || !level.getFluidState(pos).isEmpty()) {
            return false;
        }
        return EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) < 7;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new WispStartleGoal(this));
        this.goalSelector.addGoal(2, new WispFollowOwnerGoal(this));
        this.goalSelector.addGoal(3, new WispDriftGoal(this));
        this.goalSelector.addGoal(4, new WispShoalGoal(this));
        this.goalSelector.addGoal(5, new WispCuriosityGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WARMTH, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.startleTicks > 0) {
            this.startleTicks--;
        }

        if (this.level().isClientSide()) {
            this.spawnAmbientParticles();
            return;
        }

        if (this.isTame()) {
            this.tickWarmth();
        }
    }

    private void spawnAmbientParticles() {
        if (this.isInvisible()) {
            return;
        }
        if (this.random.nextInt(7) == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getRandomX(0.65D),
                    this.getY() + 0.22D + this.random.nextDouble() * 0.55D,
                    this.getRandomZ(0.65D),
                    0.0D,
                    0.005D,
                    0.0D);
        }
        if (this.isTame() && this.getWarmthTier() > 0 && this.random.nextInt(11) == 0) {
            this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getRandomX(0.45D),
                    this.getY() + 0.55D + this.random.nextDouble() * 0.3D,
                    this.getRandomZ(0.45D),
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    private void tickWarmth() {
        if (this.warmthTimer % 20 == 0) {
            this.nearWarmthSource = this.isNearWarmthSource();
        }
        this.warmthTimer++;

        int interval = 200;
        if (this.isInWaterOrRain()) {
            interval = 100;
        } else if (this.nearWarmthSource) {
            interval = 400;
        }

        if (this.warmthTimer >= interval) {
            this.warmthTimer = 0;
            if (this.getWarmth() > 0) {
                this.setWarmth(this.getWarmth() - 1);
            }
        }
    }

    private boolean isNearWarmthSource() {
        if (this.getBlockStateOn().is(BlockTags.SOUL_SPEED_BLOCKS)) {
            return true;
        }
        BlockPos base = this.blockPosition();
        BlockPos from = base.offset(-4, -2, -4);
        BlockPos to = base.offset(4, 2, 4);
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (this.level().getBlockState(pos).is(MenagerieTags.WISP_RECHARGE_BLOCKS)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);

        this.auraTimer++;
        if (this.auraTimer < 20) {
            return;
        }
        this.auraTimer = 0;

        if (this.isTame()) {
            this.applyKindledAura();
        } else {
            this.applyAnchoredAura(level);
        }
    }

    private void applyKindledAura() {
        int tier = this.getWarmthTier();
        if (tier <= 0) {
            return;
        }
        LivingEntity owner = this.getOwner();
        if (owner == null || owner.level() != this.level() || !owner.isAlive()) {
            return;
        }
        if (this.distanceToSqr(owner) > 100.0D) {
            return;
        }
        owner.addEffect(new MobEffectInstance(MenagerieEffects.KINDLED, 60, tier - 1, true, true, true));
    }

    private void applyAnchoredAura(ServerLevel level) {
        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(4.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator());
        if (players.isEmpty()) {
            return;
        }

        int nearWisps = level.getEntitiesOfClass(
                WispEntity.class,
                this.getBoundingBox().inflate(6.0D),
                wisp -> !wisp.isTame()).size();
        int amplifier = Mth.clamp(nearWisps - 1, 0, 1);

        for (Player player : players) {
            if (isWarding(player) || player.level().getRawBrightness(player.blockPosition(), 0) > 8) {
                continue;
            }
            player.addEffect(new MobEffectInstance(MenagerieEffects.ANCHORED, 60, amplifier, false, true, true));
        }
    }

    private static boolean isWarding(Player player) {
        return isWardingItem(player.getMainHandItem()) || isWardingItem(player.getOffhandItem());
    }

    private static boolean isWardingItem(ItemStack stack) {
        return stack.is(Items.TORCH)
                || stack.is(Items.LANTERN)
                || stack.is(Items.SOUL_TORCH)
                || stack.is(Items.SOUL_LANTERN);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        InteractionResult use = held.interactLivingEntity(player, this, hand);
        if (use.consumesAction()) {
            return use;
        }

        if (!this.isTame()) {
            if (!held.is(MenagerieTags.WISP_TAMEABLES)) {
                return InteractionResult.PASS;
            }
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            held.consume(1, player);
            this.trust++;
            this.setWarmth(Math.min(MAX_WARMTH, this.getWarmth() + 10));
            this.startleTicks = 0;
            this.playSound(MenagerieSounds.WISP_TAME, 0.7F, 1.2F + this.trust * 0.1F);
            this.level().broadcastEntityEvent(this, (byte) 7);
            ((WispPulseControl) this.getMoveControl()).setWait();

            if (this.trust >= TAME_TRUST_REQUIRED) {
                this.tame(player);
                this.setOrderedToSit(false);
                this.setWarmth(75);
                this.level().broadcastEntityEvent(this, (byte) 7);
                this.sendMessage(player, Component.translatable("message.lamplight.wisp.tamed"));
            } else {
                this.sendMessage(player, Component.translatable(
                        "message.lamplight.wisp.trust", this.trust, TAME_TRUST_REQUIRED));
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        if (!this.isOwnedBy(player)) {
            return InteractionResult.PASS;
        }

        if (held.is(MenagerieTags.WISP_TAMEABLES)) {
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            held.consume(1, player);
            this.setWarmth(this.getWarmth() + 30);
            this.heal(2.0F);
            this.playSound(MenagerieSounds.WISP_TAME, 0.6F, 1.35F);
            this.sendMessage(player, Component.translatable("message.lamplight.wisp.feed"));
            return InteractionResult.SUCCESS_SERVER;
        }

        if (held.isEmpty()) {
            if (this.level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            boolean stay = !this.isOrderedToSit();
            this.setOrderedToSit(stay);
            this.sendMessage(player, Component.translatable(
                    stay ? "message.lamplight.wisp.stay" : "message.lamplight.wisp.follow"));
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    private void sendMessage(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, true);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.startleTicks = 60;
            if (this.isTame()) {
                this.setOrderedToSit(false);
            }
            ((WispPulseControl) this.getMoveControl()).setWait();
        }
        return hurt;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.FALL) || super.isInvulnerableTo(level, source);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void travel(Vec3 input) {
        Vec3 movement = this.getDeltaMovement();
        this.move(MoverType.SELF, movement);

        if (this.horizontalCollision) {
            movement = new Vec3(movement.x * 0.35D, movement.y, movement.z * 0.35D);
        }
        if (this.verticalCollision) {
            movement = new Vec3(movement.x, 0.0D, movement.z);
        }

        this.setDeltaMovement(movement.scale(0.91D));
        this.resetFallDistance();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canMate(Animal other) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MenagerieTags.WISP_FOODSTUFFS);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame();
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.isTame();
    }

    @Override
    public void setOrderedToSit(boolean orderedToSit) {
        super.setOrderedToSit(orderedToSit);
        this.setInSittingPose(orderedToSit);
        if (orderedToSit) {
            this.getMoveControl().setWait();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Trust", this.trust);
        output.putInt("Warmth", this.getWarmth());
        output.putInt("StartleTicks", this.startleTicks);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.trust = input.getIntOr("Trust", 0);
        this.setWarmth(input.getIntOr("Warmth", 0));
        this.startleTicks = input.getIntOr("StartleTicks", 0);
        if (this.isTame() && this.getWarmth() == 0) {
            this.setWarmth(25);
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return 140;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MenagerieSounds.WISP_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MenagerieSounds.WISP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MenagerieSounds.WISP_HURT;
    }

    @Override
    public float getSoundVolume() {
        return 0.55F;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isBaby() ? 1.55F : 1.0F);
    }

    public void playPulseSound() {
        this.playSound(MenagerieSounds.WISP_PULSE, 0.3F, this.isBaby() ? 1.6F : 1.0F);
    }

    public int getWarmth() {
        return this.entityData.get(WARMTH);
    }

    public void setWarmth(int warmth) {
        this.entityData.set(WARMTH, Mth.clamp(warmth, 0, MAX_WARMTH));
    }

    public int getWarmthTier() {
        int warmth = this.getWarmth();
        if (warmth >= 75) {
            return 2;
        }
        return warmth >= 25 ? 1 : 0;
    }

    public int getTrust() {
        return this.trust;
    }

    public int getStartleTicks() {
        return this.startleTicks;
    }

    public void startle() {
        this.startleTicks = Math.max(this.startleTicks, 60);
    }

    public float getPulseProgress(float partialTick) {
        long time = this.level().getGameTime();
        return (float) ((time + partialTick) % PULSE_CYCLE) / PULSE_CYCLE;
    }

    public boolean isFollowingOwner() {
        return this.isTame() && !this.isOrderedToSit();
    }

    public boolean isWispSitting() {
        return this.isOrderedToSit() || this.isInSittingPose();
    }

    public boolean isOnSoulBlock() {
        return this.getBlockStateOn().is(BlockTags.SOUL_SPEED_BLOCKS);
    }

}
