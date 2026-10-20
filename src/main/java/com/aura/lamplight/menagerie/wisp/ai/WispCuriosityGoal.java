package com.aura.lamplight.menagerie.wisp.ai;

import com.aura.lamplight.menagerie.registry.MenagerieTags;
import com.aura.lamplight.menagerie.wisp.WispEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class WispCuriosityGoal extends Goal {
    private final WispEntity wisp;
    private Entity targetEntity;
    private BlockPos lurePos;
    private int updateTimer;

    public WispCuriosityGoal(WispEntity wisp) {
        this.wisp = wisp;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.wisp.isTame() || this.wisp.isOrderedToSit() || this.wisp.getRandom().nextInt(30) != 0) {
            return false;
        }

        List<Player> players = this.wisp.level().getEntitiesOfClass(
                Player.class,
                this.wisp.getBoundingBox().inflate(12.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator());
        if (!players.isEmpty()) {
            players.sort(Comparator.comparingDouble(this.wisp::distanceToSqr));
            this.targetEntity = players.getFirst();
            return true;
        }

        this.lurePos = this.findLure();
        return this.lurePos != null;
    }

    private BlockPos findLure() {
        BlockPos base = this.wisp.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-6, -3, -6), base.offset(6, 3, 6))) {
            if (this.wisp.level().getBlockState(pos).is(MenagerieTags.WISP_LURES)) {
                return pos.immutable();
            }
        }
        return null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.wisp.isTame() || this.wisp.isOrderedToSit()) {
            return false;
        }
        if (this.targetEntity != null) {
            return this.targetEntity.isAlive() && this.wisp.distanceToSqr(this.targetEntity) < 400.0D;
        }
        return this.lurePos != null && this.wisp.level().getBlockState(this.lurePos).is(MenagerieTags.WISP_LURES);
    }

    @Override
    public void start() {
        this.updateTimer = 0;
    }

    @Override
    public void tick() {
        if (--this.updateTimer > 0) {
            return;
        }
        this.updateTimer = 10;

        if (this.targetEntity != null) {
            if (this.wisp.distanceToSqr(this.targetEntity) < 6.0D) {
                return;
            }
            Vec3 target = this.targetEntity.getEyePosition().add(0.0D, 0.2D, 0.0D);
            this.wisp.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.09D);
            return;
        }

        if (this.lurePos != null) {
            Vec3 target = Vec3.atCenterOf(this.lurePos).add(0.0D, 1.8D, 0.0D);
            if (this.wisp.distanceToSqr(target) < 3.0D) {
                return;
            }
            this.wisp.getMoveControl().setWantedPosition(target.x, target.y, target.z, 0.08D);
        }
    }

    @Override
    public void stop() {
        this.targetEntity = null;
        this.lurePos = null;
    }
}
