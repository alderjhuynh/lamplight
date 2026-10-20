package com.aura.lamplight.menagerie.crow.ai;

import com.aura.lamplight.menagerie.crow.CrowEntity;
import com.aura.lamplight.menagerie.registry.MenagerieTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class CrowForageGoal extends Goal {
    private static final int SEARCH_RADIUS = 16;
    private static final int SEARCH_HEIGHT = 4;
    private static final int PECK_COUNT = 3;
    private static final int PECK_INTERVAL = 10;
    private static final int MAX_RUNTIME = 400;
    private static final float SEED_CHANCE = 0.5F;

    private final CrowEntity crow;
    private final Level level;
    private BlockPos plantPos;
    private @Nullable UUID seedUuid;
    private int peckTicks;
    private int peckCooldown;
    private long startTick;
    private long nextForageTime;

    public CrowForageGoal(CrowEntity crow) {
        this.crow = crow;
        this.level = crow.level();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (level.isClientSide()) {
            return false;
        }
        if (crow.isBaby() || crow.isTame() || crow.isSitting() || crow.isPassenger() || crow.isVehicle()) {
            return false;
        }
        if (!crow.getMainHandItem().isEmpty()) {
            return false;
        }
        if (crow.getTarget() != null && crow.getTarget().isAlive()) {
            return false;
        }
        if (level.getGameTime() < nextForageTime) {
            return false;
        }
        if (!mobGriefing()) {
            nextForageTime = level.getGameTime() + 200L;
            return false;
        }
        if (crow.getHealth() >= crow.getMaxHealth() && crow.getRandom().nextInt(600) != 0) {
            return false;
        }
        BlockPos found = findPlant();
        if (found == null) {
            nextForageTime = level.getGameTime() + 100L;
            return false;
        }
        plantPos = found;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (crow.isTame() || crow.isSitting() || crow.isPassenger() || crow.isVehicle()
                || (crow.getTarget() != null && crow.getTarget().isAlive())
                || !crow.getMainHandItem().isEmpty()) {
            return false;
        }
        if (level.getGameTime() - startTick > MAX_RUNTIME) {
            return false;
        }
        if (seedUuid != null) {
            ItemEntity seed = getSeedEntity();
            return seed != null && !seed.isRemoved() && !seed.getItem().isEmpty()
                    && seed.getItem().is(MenagerieTags.CROW_FORAGED_SEEDS);
        }
        return plantPos != null && isValidPlant(plantPos);
    }

    @Override
    public void start() {
        seedUuid = null;
        peckTicks = 0;
        peckCooldown = 0;
        startTick = level.getGameTime();
    }

    @Override
    public void stop() {
        crow.getNavigation().stop();
        plantPos = null;
        seedUuid = null;
        peckTicks = 0;
        peckCooldown = 0;
        nextForageTime = Math.max(nextForageTime, level.getGameTime() + 200L);
    }

    @Override
    public void tick() {
        if (seedUuid != null) {
            ItemEntity seed = getSeedEntity();
            if (seed == null) {
                stop();
                return;
            }
            tickSeedPickup(seed);
            return;
        }
        if (plantPos == null || !isValidPlant(plantPos)) {
            stop();
            return;
        }

        Vec3 plantCenter = Vec3.atCenterOf(plantPos);
        if (crow.distanceToSqr(plantCenter) > 4.0D) {
            moveTo(plantCenter);
            return;
        }

        crow.getLookControl().setLookAt(plantCenter.x, plantCenter.y, plantCenter.z,
                10.0F, crow.getMaxHeadXRot());
        if (--peckCooldown <= 0) {
            peckCooldown = PECK_INTERVAL;
            crow.peck();
            peckTicks++;
            if (peckTicks >= PECK_COUNT) {
                harvest();
            }
        }
    }

    private void tickSeedPickup(ItemEntity seedEntity) {
        if (seedEntity.isRemoved() || seedEntity.getItem().isEmpty()
                || !seedEntity.getItem().is(MenagerieTags.CROW_FORAGED_SEEDS)) {
            stop();
            return;
        }
        if (!crow.getMainHandItem().isEmpty()) {
            stop();
            return;
        }

        Vec3 seedPos = seedEntity.position();
        if (crow.distanceToSqr(seedPos) < 2.0D) {
            ItemStack seed = seedEntity.getItem().copyWithCount(1);
            crow.setItemInHand(InteractionHand.MAIN_HAND, seed);
            seedEntity.getItem().shrink(1);
            if (seedEntity.getItem().isEmpty()) {
                seedEntity.discard();
            }
            crow.setFlying(false);
            seedUuid = null;
            stop();
            return;
        }
        moveTo(seedPos);
    }

    @Nullable
    private ItemEntity getSeedEntity() {
        if (seedUuid == null || !(level instanceof ServerLevel server)) {
            return null;
        }
        var entity = server.getEntity(seedUuid);
        return entity instanceof ItemEntity item ? item : null;
    }

    private void harvest() {
        if (plantPos == null) {
            stop();
            return;
        }
        BlockState state = level.getBlockState(plantPos);
        if (!mobGriefing() || !state.is(MenagerieTags.CROW_FORAGEABLES)) {
            stop();
            return;
        }

        if (state.getBlock() instanceof DoublePlantBlock) {
            BlockPos other = state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER
                    ? plantPos.above()
                    : plantPos.below();
            if (level.getBlockState(other).getBlock() == state.getBlock()) {
                level.removeBlock(other, false);
            }
        }
        level.removeBlock(plantPos, false);
        crow.peck();

        nextForageTime = level.getGameTime() + 600L + crow.getRandom().nextInt(601);
        if (crow.getRandom().nextFloat() < SEED_CHANCE) {
            ItemStack seed = new ItemStack(randomSeed(), 1);
            ItemEntity item = new ItemEntity(level,
                    plantPos.getX() + 0.5D, plantPos.getY() + 0.2D, plantPos.getZ() + 0.5D, seed);
            item.setPickUpDelay(10);
            item.setDeltaMovement((crow.getRandom().nextDouble() - 0.5D) * 0.1D,
                    0.15D,
                    (crow.getRandom().nextDouble() - 0.5D) * 0.1D);
            if (level instanceof ServerLevel server && server.addFreshEntity(item)) {
                seedUuid = item.getUUID();
            } else {
                stop();
            }
        } else {
            stop();
        }
    }

    private void moveTo(Vec3 target) {
        if (crow.isFlying() || crow.distanceToSqr(target) > 64.0D) {
            crow.setFlying(true);
            crow.getMoveControl().setWantedPosition(target.x, target.y + 0.2D, target.z, 1.0F);
        } else {
            if (!crow.onGround()) {
                crow.setFlying(false);
            }
            crow.getNavigation().moveTo(target.x, target.y, target.z, 1.0D);
        }
    }

    private BlockPos findPlant() {
        BlockPos origin = crow.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS))) {
            if (!isValidPlant(pos)) {
                continue;
            }
            double distance = crow.distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }
        return best;
    }

    private boolean isValidPlant(BlockPos pos) {
        return level.getBlockState(pos).is(MenagerieTags.CROW_FORAGEABLES);
    }

    private boolean mobGriefing() {
        return level.getServer() == null
                || level.getServer().getGameRules().get(GameRules.MOB_GRIEFING);
    }

    private Item randomSeed() {
        float roll = crow.getRandom().nextFloat();
        if (roll < 0.5F) {
            return Items.WHEAT_SEEDS;
        }
        if (roll < 0.7F) {
            return Items.BEETROOT_SEEDS;
        }
        if (roll < 0.85F) {
            return Items.MELON_SEEDS;
        }
        return Items.PUMPKIN_SEEDS;
    }
}
