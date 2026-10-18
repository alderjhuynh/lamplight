package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.BackSlotAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// drops back slot items like every other item
@Mixin(Player.class)
public class PlayerBackSlotDropMixin {
	@Inject(method = "dropEquipment", at = @At("HEAD"))
	private void armory$dropBackStack(ServerLevel level, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		if (self.isCreative()) {
			return;
		}
		if (level.getGameRules().get(GameRules.KEEP_INVENTORY)) {
			return;
		}
		ItemStack back = BackSlotAttachments.getBackStack(self).copy();
		if (!back.isEmpty()) {
			BackSlotAttachments.setBackStack(self, ItemStack.EMPTY);
			ItemEntity entity = new ItemEntity(level, self.getX(), self.getY() + 0.5, self.getZ(), back);
			entity.setDefaultPickUpDelay();
			level.addFreshEntity(entity);
		}
		BackSlotAttachments.setHolding(self, false);
	}
}
