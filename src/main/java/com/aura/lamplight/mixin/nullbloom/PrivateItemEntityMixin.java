package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.GodAudience;
import com.aura.lamplight.nullbloom.NullbloomDimension;
import java.util.UUID;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class PrivateItemEntityMixin {
	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void nullbloom$noStealing(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		if (!NullbloomDimension.is(self)) {
			UUID thrower = NullbloomDimension.privateOwner(self);
			if (thrower != null && !thrower.equals(player.getUUID())
				&& (GodAudience.isIn(thrower) || GodAudience.isIn(player))) {
				ci.cancel();
			}
			return;
		}
		UUID owner = NullbloomDimension.privateOwner(self);
		if (owner != null && !owner.equals(player.getUUID())) {
			ci.cancel();
		}
	}
}
