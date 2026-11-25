package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class BuildLockPlayerMixin {
	@Inject(method = "blockActionRestricted", at = @At("HEAD"), cancellable = true)
	private void nullbloom$lockBreaking(Level level, BlockPos pos, GameType gameType, CallbackInfoReturnable<Boolean> cir) {
		if (NullbloomDimension.isBuildLocked(level) || GodAudience.isIn((Player) (Object) this)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mayUseItemAt", at = @At("HEAD"), cancellable = true)
	private void nullbloom$lockItemPlacement(BlockPos pos, Direction direction, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (NullbloomDimension.isBuildLocked(((Player) (Object) this).level()) || GodAudience.isIn((Player) (Object) this)) {
			cir.setReturnValue(false);
		}
	}
}
