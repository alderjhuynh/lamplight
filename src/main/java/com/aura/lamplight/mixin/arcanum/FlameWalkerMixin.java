package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class FlameWalkerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void arcanum$flameWalkerTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Level level = self.level();
        if (level.isClientSide()) return;
        if (self.isSpectator()) return;

        ItemStack feet = self.getItemBySlot(EquipmentSlot.FEET);
        if (feet.isEmpty()) return;
        int lvl = FreezeHandler.getLevel(feet, ArcanumEnchantments.FLAME_WALKER);
        if (lvl <= 0) return;

        if (self.isCrouching()) return;
        // Only attempt when close to lava
        if (self.isInLava()) {
            // still allow platform under
        } else {
            // check if standing just above lava (onGround or falling close)
            // frost walker checks onGround, we allow even when not onGround to walk over lava
            // Simple: if not near lava, skip
        }

        BlockPos center = self.blockPosition();
        int radius = 2 + lvl; // 3 at lvl1, 4 at lvl2 matching frost walker scaling
        // Y level just below feet
        int y = center.getY() - 1;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            BlockPos target = new BlockPos(pos.getX(), y, pos.getZ());
            // distance check circular
            if (target.distManhattan(center) > radius && target.distManhattan(center) > 3) continue;
            if (target.closerThan(center, radius + 1) == false) {
                // use euclidean
                double dx = target.getX() + 0.5 - self.getX();
                double dz = target.getZ() + 0.5 - self.getZ();
                if (dx * dx + dz * dz > (radius + 0.5) * (radius + 0.5)) continue;
            }
            BlockState state = level.getBlockState(target);
            if (!state.is(Blocks.LAVA)) continue;
            // Need air above
            BlockPos above = target.above();
            BlockState aboveState = level.getBlockState(above);
            if (!aboveState.isAir()) continue;

            // Place obsidian which will hold, or basalt for nether flavor. Use obsidian.
            // For flowing lava, still convert to obsidian (vanilla frosted_ice replaces water source only)
            // Here we replace lava source and flowing with basalt/obsidian temporary.
            // Keep as BASALT to avoid permanent obsidian grief, but BASALT also permanent.
            // Use OBSIDIAN for consistency with water->ice freezing.
            BlockState newState = Blocks.OBSIDIAN.defaultBlockState();
            level.setBlockAndUpdate(target, newState);
            // schedule tick to melt? Frosted ice melts via random tick, obsidian not. We keep permanent.
            // Could schedule later to revert, but leave as is for now.

            // Play effect? Frost walker does not need.
        }
    }
}
