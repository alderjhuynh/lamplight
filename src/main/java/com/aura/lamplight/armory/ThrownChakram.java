package com.aura.lamplight.armory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ThrownChakram extends Projectile implements ItemSupplier {
	
	public static final double OUTBOUND_SPEED = 1.15D;
	public static final double RANGE = 16.0D;
	private static final double PATH_BUDGET = RANGE * 2.25D;
	public static final int MAX_BOUNCES = 6;

	private static final double RETURN_START_SPEED = 0.40D;
	private static final double RETURN_ACCELERATION = 0.09D;
	private static final double RETURN_MAX_SPEED = 1.90D;
	private static final double RETURN_STEER = 0.45D;
	private static final double CATCH_RADIUS = 1.25D;

	private static final double WATER_SPEED_FACTOR = 0.60D;

	private static final float HIT_MARGIN = 0.3F;
	private static final float HIT_KNOCKBACK = 0.35F;
	private static final float RICOCHET_BONUS_PER_BOUNCE = 0.15F;
	private static final int RICOCHET_BONUS_MAX_BOUNCES = 4;

	private static final double SEEK_RANGE = 8.0D;
	private static final int SEEK_TICKS_AFTER_BOUNCE = 12;
	private static final double SEEK_STEER = 0.28D;

	private static final int MAX_LIFETIME_TICKS = 20 * 20;
	private static final int OWNER_MISSING_GRACE_TICKS = 60;

	private static final int MOMENTUM_BASE_TICKS = 30;
	private static final int MOMENTUM_PER_HIT_TICKS = 20;
	private static final int MOMENTUM_MAX_TICKS = 100;

	private static final int WHIRR_INTERVAL_TICKS = 4;

	public static final ResourceKey<DamageType> CHAKRAM_DAMAGE =
			ResourceKey.create(Registries.DAMAGE_TYPE, Armory.id("chakram"));

	private static final EntityDataAccessor<ItemStack> DATA_ITEM =
			SynchedEntityData.defineId(ThrownChakram.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Boolean> DATA_RETURNING =
			SynchedEntityData.defineId(ThrownChakram.class, EntityDataSerializers.BOOLEAN);

	private float damage = 5.0F;
	private int returnSlot = -1;
	private boolean infinite;

	private @Nullable Vec3 heading;
	private double speed = OUTBOUND_SPEED;
	private double travelled;
	private int bounces;
	private int hits;
	private int seekTicks;
	private int ownerMissingTicks;
	private final IntOpenHashSet hitThisLeg = new IntOpenHashSet();

	public ThrownChakram(final EntityType<? extends ThrownChakram> type, final Level level) {
		super(type, level);
	}

	public ThrownChakram(final Level level, final LivingEntity owner, final ItemStack stack) {
		this(ArmoryEntityTypes.THROWN_CHAKRAM, level);
		this.setPos(owner.getX(), owner.getEyeY() - 0.15D, owner.getZ());
		this.setOwner(owner);
		this.setItem(stack);
	}

	public void configure(final float damage, final int returnSlot, final boolean infinite) {
		this.damage = Math.max(0.0F, damage);
		this.returnSlot = returnSlot;
		this.infinite = infinite;
	}

	public void setItem(final ItemStack stack) {
		this.entityData.set(DATA_ITEM, stack.copyWithCount(1));
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(DATA_ITEM);
	}

	public boolean isReturning() {
		return this.entityData.get(DATA_RETURNING);
	}

	@Override
	protected void defineSynchedData(final SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM, new ItemStack(ModItems.DIAMOND_CHAKRAM));
		builder.define(DATA_RETURNING, false);
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.store("Item", ItemStack.CODEC, this.getItem());
		output.putFloat("Damage", this.damage);
		output.putInt("ReturnSlot", this.returnSlot);
		output.putBoolean("Infinite", this.infinite);
		output.putBoolean("Returning", this.isReturning());
		output.putDouble("Speed", this.speed);
		output.putDouble("Travelled", this.travelled);
		output.putInt("Bounces", this.bounces);
		output.putInt("Hits", this.hits);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		this.setItem(input.read("Item", ItemStack.CODEC).orElseGet(() -> new ItemStack(ModItems.DIAMOND_CHAKRAM)));
		this.damage = input.getFloatOr("Damage", 5.0F);
		this.returnSlot = input.getIntOr("ReturnSlot", -1);
		this.infinite = input.getBooleanOr("Infinite", false);
		this.entityData.set(DATA_RETURNING, input.getBooleanOr("Returning", false));
		this.speed = input.getDoubleOr("Speed", OUTBOUND_SPEED);
		this.travelled = input.getDoubleOr("Travelled", 0.0D);
		this.bounces = input.getIntOr("Bounces", 0);
		this.hits = input.getIntOr("Hits", 0);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(final double distance) {
		return distance < 64.0D * 64.0D;
	}

	@Override
	public boolean canUsePortal(final boolean ignorePassenger) {
		return false;
	}
	@Override
	public void tick() {
		super.tick();
		if (this.level() instanceof ServerLevel serverLevel) {
			this.serverTick(serverLevel);
		} else {
			this.clientTick();
		}
	}

	private void clientTick() {
		Vec3 velocity = this.getDeltaMovement();
		this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);

		if (this.isReturning() && this.tickCount % 2 == 0) {
			this.level().addParticle(ParticleTypes.ELECTRIC_SPARK,
					this.getX(), this.getY() + 0.1D, this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}

	private void serverTick(final ServerLevel level) {
		Entity owner = this.getOwner();
		if (!isValidOwner(owner)) {
			this.setDeltaMovement(Vec3.ZERO);
			if (++this.ownerMissingTicks > OWNER_MISSING_GRACE_TICKS) {
				this.dropAsItem(level);
			}
			return;
		}
		this.ownerMissingTicks = 0;

		if (this.tickCount > MAX_LIFETIME_TICKS) {
			this.dropAsItem(level);
			return;
		}

		if (this.heading == null) {
			Vec3 motion = this.getDeltaMovement();
			this.heading = motion.lengthSqr() > 1.0E-6D ? motion.normalize() : owner.getLookAngle();
		}

		if (this.tickCount % WHIRR_INTERVAL_TICKS == 0) {
			float pitch = (this.isReturning() ? 1.15F : 1.0F) + this.random.nextFloat() * 0.1F;
			level.playSound(null, this.getX(), this.getY(), this.getZ(),
					ArmorySounds.CHAKRAM_WHIRR, SoundSource.PLAYERS, 0.35F, pitch);
		}

		if (!this.isReturning() && this.shouldTurnAround(owner)) {
			this.beginReturn(level);
		}

		Vec3 ownerCenter = owner.getBoundingBox().getCenter();
		double distToOwner = this.position().distanceTo(ownerCenter);

		if (this.isReturning()) {
			this.steerHome(ownerCenter, distToOwner);
		} else {
			this.seekAfterBounce(level, owner);
		}

		double stepLength = this.speed * (this.isInWater() ? WATER_SPEED_FACTOR : 1.0D);
		if (this.isReturning()) {
			stepLength = Math.min(stepLength, Math.max(distToOwner, 0.05D));
		}
		Vec3 step = this.heading.scale(stepLength);
		this.setDeltaMovement(step);

		Vec3 from = this.position();
		Vec3 to = from.add(step);

		BlockHitResult blockHit = null;
		if (!this.isReturning()) {
			BlockHitResult clip = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if (clip.getType() != HitResult.Type.MISS) {
				blockHit = clip;
				to = clip.getLocation();
			}
		}

		this.sliceEntities(level, owner, from, to);
		if (!this.isAlive()) {
			return;
		}

		this.setPos(to);
		if (!this.isReturning()) {
			this.travelled += from.distanceTo(to);
		}
		this.cutCobwebs(level);

		if (blockHit != null) {
			this.ricochet(level, blockHit);
		}

		if (this.isReturning() && this.position().distanceTo(ownerCenter) <= CATCH_RADIUS) {
			this.catchByOwner(level, owner);
			return;
		}

		if (this.isReturning() || this.seekTicks > 0 || blockHit != null) {
			this.needsSync = true;
		}
	}

	private static boolean isValidOwner(final @Nullable Entity owner) {
		return owner != null && owner.isAlive() && !(owner instanceof ServerPlayer player && player.isSpectator());
	}

	private boolean shouldTurnAround(final Entity owner) {
		if (this.travelled >= PATH_BUDGET || this.bounces >= MAX_BOUNCES) {
			return true;
		}
		return this.position().distanceToSqr(owner.getBoundingBox().getCenter()) >= RANGE * RANGE;
	}

	private void beginReturn(final ServerLevel level) {
		this.entityData.set(DATA_RETURNING, true);
		this.speed = RETURN_START_SPEED;
		this.seekTicks = 0;
		this.hitThisLeg.clear();
		this.needsSync = true;
		level.playSound(null, this.getX(), this.getY(), this.getZ(),
				ArmorySounds.CHAKRAM_RETURN, SoundSource.PLAYERS, 0.8F, 1.0F);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 0.1D, this.getZ(),
				6, 0.15D, 0.15D, 0.15D, 0.2D);
	}

	private void steerHome(final Vec3 ownerCenter, final double distToOwner) {
		if (distToOwner > 1.0E-4D && this.heading != null) {
			Vec3 want = ownerCenter.subtract(this.position()).scale(1.0D / distToOwner);
			this.heading = this.heading.scale(1.0D - RETURN_STEER).add(want.scale(RETURN_STEER)).normalize();
		}
		this.speed = Math.min(RETURN_MAX_SPEED, this.speed + RETURN_ACCELERATION);
	}

	private void seekAfterBounce(final ServerLevel level, final Entity owner) {
		if (this.seekTicks <= 0 || this.heading == null) {
			return;
		}
		this.seekTicks--;

		Vec3 here = this.position();
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		AABB area = this.getBoundingBox().inflate(SEEK_RANGE);
		for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, area, e -> this.isSeekable(e, owner))) {
			Vec3 to = candidate.getBoundingBox().getCenter().subtract(here);
			double dist = to.lengthSqr();
			if (dist < 1.0E-4D || dist >= bestDist) {
				continue;
			}
			if (this.heading.dot(to.normalize()) <= 0.0D) {
				continue;
			}
			BlockHitResult los = level.clip(new ClipContext(here, candidate.getBoundingBox().getCenter(),
					ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if (los.getType() != HitResult.Type.MISS) {
				continue;
			}
			best = candidate;
			bestDist = dist;
		}

		if (best != null) {
			Vec3 want = best.getBoundingBox().getCenter().subtract(here).normalize();
			this.heading = this.heading.scale(1.0D - SEEK_STEER).add(want.scale(SEEK_STEER)).normalize();
		}
	}

	private boolean isSeekable(final LivingEntity entity, final Entity owner) {
		if (entity == owner || !entity.isAlive() || entity.isSpectator() || this.hitThisLeg.contains(entity.getId())) {
			return false;
		}
		if (entity instanceof OwnableEntity ownable && ownable.getOwner() == owner) {
			return false;
		}
		return entity instanceof Enemy || (entity instanceof Mob mob && mob.getTarget() == owner);
	}

	private boolean canSlice(final Entity entity, final Entity owner) {
		if (!(entity instanceof LivingEntity living) || entity == owner || !living.isAlive()) {
			return false;
		}
		if (this.hitThisLeg.contains(entity.getId()) || !this.canHitEntity(entity)) {
			return false;
		}
		if (entity instanceof OwnableEntity ownable && ownable.getOwner() == owner) {
			return false; 
		}
		return !(entity instanceof Player target && owner instanceof Player thrower && !thrower.canHarmPlayer(target));
	}

	private void sliceEntities(final ServerLevel level, final Entity owner, final Vec3 from, final Vec3 to) {
		AABB search = this.getBoundingBox().expandTowards(to.subtract(from)).inflate(1.0D);
		List<EntityHitResult> hitList = new ArrayList<>();
		for (Entity entity : level.getEntities(this, search, e -> this.canSlice(e, owner))) {
			AABB box = entity.getBoundingBox().inflate(HIT_MARGIN + entity.getPickRadius());
			Optional<Vec3> point = box.contains(from) ? Optional.of(from) : box.clip(from, to);
			point.ifPresent(p -> hitList.add(new EntityHitResult(entity, p)));
		}
		hitList.sort(Comparator.comparingDouble(h -> from.distanceToSqr(h.getLocation())));

		for (EntityHitResult hit : hitList) {
			if (!this.isAlive()) {
				return;
			}
			if (hit.getEntity() instanceof LivingEntity living) {
				this.sliceEntity(level, owner, living, hit.getLocation());
			}
		}
	}

	private DamageSource damageSource(final ServerLevel level, final @Nullable Entity owner) {
		Holder.Reference<DamageType> type = level.registryAccess()
				.lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(CHAKRAM_DAMAGE);
		return new DamageSource(type, this, owner);
	}

	private float currentDamage() {
		int counted = Math.min(this.bounces, RICOCHET_BONUS_MAX_BOUNCES);
		return this.damage * (1.0F + RICOCHET_BONUS_PER_BOUNCE * counted);
	}

	private void sliceEntity(final ServerLevel level, final Entity owner, final LivingEntity target, final Vec3 at) {
		this.hitThisLeg.add(target.getId());

		ItemStack weapon = this.getItem();
		DamageSource source = this.damageSource(level, owner);
		float dmg = EnchantmentHelper.modifyDamage(level, weapon, target, source, this.currentDamage());

		if (!target.hurtServer(level, source, dmg)) {
			return;
		}
		this.hits++;

		if (this.heading != null) {
			float knockback = EnchantmentHelper.modifyKnockback(level, weapon, target, source, HIT_KNOCKBACK);
			if (knockback > 0.0F) {
				target.knockback(knockback, -this.heading.x, -this.heading.z, source, dmg);
			}
		}
		EnchantmentHelper.doPostAttackEffectsWithItemSource(level, target, source, weapon);

		Vec3 center = target.getBoundingBox().getCenter();
		level.sendParticles(ParticleTypes.ENCHANTED_HIT, center.x, center.y, center.z, 10, 0.25D, 0.25D, 0.25D, 0.3D);
		level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 4, 0.2D, 0.2D, 0.2D, 0.25D);
		level.playSound(null, at.x, at.y, at.z, ArmorySounds.CHAKRAM_HIT,
				SoundSource.PLAYERS, 0.9F, 0.9F + this.random.nextFloat() * 0.3F);
	}

	private void ricochet(final ServerLevel level, final BlockHitResult hit) {
		super.onHitBlock(hit);

		Direction face = hit.getDirection();
		Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
		if (this.heading != null) {
			Vec3 reflected = this.heading.subtract(normal.scale(2.0D * this.heading.dot(normal)));
			this.heading = reflected.lengthSqr() > 1.0E-6D ? reflected.normalize() : normal;
		}
		this.setPos(this.position().add(normal.scale(0.06D)));

		this.bounces++;
		this.speed = OUTBOUND_SPEED;
		this.seekTicks = SEEK_TICKS_AFTER_BOUNCE;
		this.needsSync = true;

		Vec3 p = hit.getLocation();
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 8, 0.1D, 0.1D, 0.1D, 0.3D);
		level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 3, 0.1D, 0.1D, 0.1D, 0.2D);
		level.playSound(null, p.x, p.y, p.z, ArmorySounds.CHAKRAM_BOUNCE,
				SoundSource.PLAYERS, 0.9F, 0.9F + 0.08F * Math.min(this.bounces, 6));

		if (this.bounces >= MAX_BOUNCES) {
			this.beginReturn(level);
		}
	}

	private static boolean isCuttable(final BlockState state) {
		return state.is(Blocks.COBWEB) || state.is(Blocks.TRIPWIRE);
	}

	private void cutCobwebs(final ServerLevel level) {
		AABB box = this.getBoundingBox().inflate(0.1D);
		for (BlockPos pos : BlockPos.betweenClosed(box)) {
			BlockState state = level.getBlockState(pos);
			if (isCuttable(state) && this.mayInteract(level, pos)) {
				level.destroyBlock(pos, true, this, 512);
			}
		}
	}

	private void catchByOwner(final ServerLevel level, final Entity owner) {
		ItemStack stack = this.getItem().copy();

		if (!this.infinite) {
			if (owner instanceof Player player) {
				this.giveBack(level, player, stack);
			} else {
				this.spawnAtLocation(level, stack);
			}
		}

		if (this.hits > 0 && owner instanceof LivingEntity living) {
			int ticks = Math.min(MOMENTUM_MAX_TICKS, MOMENTUM_BASE_TICKS + MOMENTUM_PER_HIT_TICKS * this.hits);
			living.addEffect(new MobEffectInstance(MobEffects.SPEED, ticks, 0, false, true));
		}

		Vec3 c = owner.getBoundingBox().getCenter();
		level.playSound(null, c.x, c.y, c.z, ArmorySounds.CHAKRAM_CATCH,
				SoundSource.PLAYERS, 0.9F, 1.0F + Math.min(this.hits, 4) * 0.05F);
		level.sendParticles(ParticleTypes.ENCHANTED_HIT, c.x, c.y, c.z, 8, 0.3D, 0.3D, 0.3D, 0.2D);
		this.discard();
	}

	private void giveBack(final ServerLevel level, final Player player, final ItemStack stack) {
		Inventory inventory = player.getInventory();
		if (this.returnSlot >= 0 && inventory.getItem(this.returnSlot).isEmpty()) {
			inventory.setItem(this.returnSlot, stack);
		} else if (!inventory.add(stack)) {
			player.drop(stack, false);
		}
	}

	private void dropAsItem(final ServerLevel level) {
		if (!this.infinite) {
			this.spawnAtLocation(level, this.getItem().copy());
		}
		this.discard();
	}
}