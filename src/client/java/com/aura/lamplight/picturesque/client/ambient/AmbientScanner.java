package com.aura.lamplight.picturesque.client.ambient;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class AmbientScanner {
	private static final int RADIUS = 12;
	private static final int DOWN = 8;
	private static final int UP = 12;
	private static final int LEAF_RADIUS = 5;
	private static final int LEAF_VERTICAL = 4;
	private static final int SLICES_PER_TICK = 5;
	private static final int SWEEP_INTERVAL = 20;

	int leafCount;
	int fallCount;
	@Nullable Vec3 nearestFall;

	private int slice = RADIUS + 1;
	private int cx, cy, cz;
	private int accLeaves, accFalls;
	private int bestDistSqr, bestX, bestY, bestZ;
	private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

	void reset() {
		leafCount = 0;
		fallCount = 0;
		nearestFall = null;
		slice = RADIUS + 1;
	}

	void tick(ClientLevel level, LocalPlayer player, long tickCount) {
		if (slice > RADIUS) {
			if (tickCount % SWEEP_INTERVAL != 0) {
				return;
			}
			BlockPos c = player.blockPosition();
			cx = c.getX();
			cy = c.getY();
			cz = c.getZ();
			slice = -RADIUS;
			accLeaves = 0;
			accFalls = 0;
			bestDistSqr = Integer.MAX_VALUE;
		}
		for (int i = 0; i < SLICES_PER_TICK && slice <= RADIUS; i++, slice++) {
			scanSlice(level, slice);
		}
		if (slice > RADIUS) {
			leafCount = accLeaves;
			fallCount = accFalls;
			nearestFall = accFalls > 0 ? new Vec3(bestX + 0.5, bestY + 0.5, bestZ + 0.5) : null;
		}
	}

	private void scanSlice(ClientLevel level, int dx) {
		for (int dz = -RADIUS; dz <= RADIUS; dz++) {
			boolean leafColumn = Math.abs(dx) <= LEAF_RADIUS && Math.abs(dz) <= LEAF_RADIUS;
			for (int dy = -DOWN; dy <= UP; dy++) {
				pos.set(cx + dx, cy + dy, cz + dz);
				FluidState fluid = level.getFluidState(pos);
				if (!fluid.isEmpty()) {
					if (fluid.is(FluidTags.WATER) && fluid.hasProperty(FlowingFluid.FALLING)
							&& fluid.getValue(FlowingFluid.FALLING)) {
						accFalls++;
						int dist = dx * dx + dy * dy + dz * dz;
						if (dist < bestDistSqr) {
							bestDistSqr = dist;
							bestX = pos.getX();
							bestY = pos.getY();
							bestZ = pos.getZ();
						}
					}
					continue;
				}
				if (leafColumn && dy >= -LEAF_VERTICAL && dy <= LEAF_VERTICAL
						&& level.getBlockState(pos).is(BlockTags.LEAVES)) {
					accLeaves++;
				}
			}
		}
	}
}
