package com.aura.lamplight.menagerie.raccoon.ai;

import com.aura.lamplight.menagerie.Menagerie;
import com.aura.lamplight.menagerie.raccoon.RaccoonEntity;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class RaccoonDigGoal extends Goal {
    private static final ResourceKey<LootTable> DIGGING_TABLE =
            ResourceKey.create(Registries.LOOT_TABLE, Menagerie.id("gameplay/raccoon_digging"));
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_TRIES = 15;
    private static final int SEARCH_START_HEIGHT = 3;
    private static final double REACH_DISTANCE_SQR = 3.2D;
    private static final int SWING_INTERVAL = RaccoonEntity.ATTACK_ANIMATION_LENGTH;
    private static final int SWING_COUNT = 3;
    private static final int MAX_RUNTIME = 400;
    private static final int COOLDOWN_TICKS = 1600;
    private static final int COOLDOWN_VARIANCE = 2400;

    private final RaccoonEntity raccoon;
    @Nullable
    private BlockPos digPos;
    private int runtimeTicks;
    private int swingCooldown;
    private int swings;
    private long nextDigTime;
    private int attemptCooldown;

    public RaccoonDigGoal(RaccoonEntity raccoon) {
        this.raccoon = raccoon;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.raccoon.level().isClientSide()) {
            return false;
        }
        if (this.attemptCooldown > 0) {
            this.attemptCooldown--;
            return false;
        }
        this.attemptCooldown = 10;
        if (this.raccoon.level().getGameTime() < this.nextDigTime) {
            return false;
        }
        if (this.raccoon.isBaby() || this.raccoon.isWashing() || !this.raccoon.getMainHandItem().isEmpty()
                || (this.raccoon.getTarget() != null && this.raccoon.getTarget().isAlive())) {
            return false;
        }
        BlockPos found = this.findDiggableGround();
        if (found == null) {
            return false;
        }
        this.digPos = found;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.digPos == null || this.runtimeTicks > MAX_RUNTIME || !this.digPosStillDiggable()) {
            return false;
        }
        return this.raccoon.isAlive() && this.raccoon.getMainHandItem().isEmpty()
                && (this.raccoon.getTarget() == null || !this.raccoon.getTarget().isAlive());
    }

    @Override
    public void start() {
        this.runtimeTicks = 0;
        this.swingCooldown = 0;
        this.swings = 0;
    }

    @Override
    public void stop() {
        this.raccoon.getNavigation().stop();
        this.digPos = null;
        this.runtimeTicks = 0;
        this.swingCooldown = 0;
        this.swings = 0;
        this.nextDigTime = this.raccoon.level().getGameTime() + COOLDOWN_TICKS
                + this.raccoon.getRandom().nextInt(COOLDOWN_VARIANCE);
    }

    @Override
    public void tick() {
        this.runtimeTicks++;
        if (this.digPos == null) {
            return;
        }
        if (!this.digPosStillDiggable()) {
            this.stop();
            return;
        }
        Vec3 center = Vec3.atCenterOf(this.digPos.above());
        if (this.raccoon.distanceToSqr(center) > REACH_DISTANCE_SQR) {
            this.raccoon.getNavigation().moveTo(this.digPos.getX() + 0.5D, this.digPos.getY() + 1.0D, this.digPos.getZ() + 0.5D, 1.0D);
            return;
        }
        this.raccoon.getNavigation().stop();
        this.raccoon.getLookControl().setLookAt(center.x, center.y, center.z, 10.0F, this.raccoon.getMaxHeadXRot());
        if (--this.swingCooldown <= 0) {
            this.swingCooldown = SWING_INTERVAL;
            this.swings++;
            this.raccoon.beginAttack();
            this.spawnDigParticles();
            if (this.swings >= SWING_COUNT) {
                this.digUp();
                this.stop();
            }
        }
    }

    private boolean digPosStillDiggable() {
        return this.digPos != null && this.raccoon.level().getBlockState(this.digPos).is(MenagerieTags.RACCOON_DIGGABLE);
    }

    @Nullable
    private BlockPos findDiggableGround() {
        BlockPos origin = this.raccoon.blockPosition();
        var random = this.raccoon.getRandom();
        for (int i = 0; i < SEARCH_TRIES; i++) {
            BlockPos candidate = origin.offset(random.nextInt(SEARCH_RADIUS) - SEARCH_RADIUS / 2, SEARCH_START_HEIGHT,
                    random.nextInt(SEARCH_RADIUS) - SEARCH_RADIUS / 2);
            while (this.raccoon.level().isEmptyBlock(candidate) && candidate.getY() > this.raccoon.level().getMinY()) {
                candidate = candidate.below();
            }
            if (this.isDiggable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isDiggable(BlockPos pos) {
        BlockState state = this.raccoon.level().getBlockState(pos);
        return state.is(MenagerieTags.RACCOON_DIGGABLE) && this.raccoon.level().getBlockState(pos.above()).isAir();
    }

    private void spawnDigParticles() {
        if (this.digPos == null || !(this.raccoon.level() instanceof ServerLevel server)) {
            return;
        }
        BlockState state = this.raccoon.level().getBlockState(this.digPos);
        Vec3 center = Vec3.atCenterOf(this.digPos.above());
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                center.x, center.y, center.z, 8, 0.3D, 0.1D, 0.3D, 0.05D);
    }

    private void digUp() {
        this.raccoon.getNavigation().stop();
        if (this.digPos == null || !(this.raccoon.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(this.digPos.above());
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, center)
                .withParameter(LootContextParams.THIS_ENTITY, this.raccoon)
                .create(LootContextParamSets.GIFT);
        var registryAccess = server.getServer().reloadableRegistries();
        List<ItemStack> drops = registryAccess.getLootTable(DIGGING_TABLE).getRandomItems(params);
        for (ItemStack stack : drops) {
            ItemEntity item = new ItemEntity(server, center.x, center.y, center.z, stack);
            item.setPickUpDelay(10);
            server.addFreshEntity(item);
        }
    }
}
