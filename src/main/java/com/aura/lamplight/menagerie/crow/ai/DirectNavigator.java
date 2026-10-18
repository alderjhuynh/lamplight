package com.aura.lamplight.menagerie.crow.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

// Ported from Alex's Mobs DirectPathNavigator (LGPL-3.0, see NOTICE_CROW.md).
public class DirectNavigator extends GroundPathNavigation {
    private final Mob mob;
    private final float yOffset;

    public DirectNavigator(Mob mob, Level level) {
        this(mob, level, 0);
    }

    public DirectNavigator(Mob mob, Level level, float yOffset) {
        super(mob, level);
        this.mob = mob;
        this.yOffset = yOffset;
    }

    @Override
    public void tick() {
        ++this.tick;
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        mob.getMoveControl().setWantedPosition(x, y, z, speed);
        return true;
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        mob.getMoveControl().setWantedPosition(entity.getX(), entity.getY() + yOffset, entity.getZ(), speed);
        return true;
    }
}
