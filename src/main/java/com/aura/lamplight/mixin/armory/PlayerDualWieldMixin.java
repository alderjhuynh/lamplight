package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.DualWielded;
import com.aura.lamplight.armory.DualWieldedPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDualWieldMixin implements DualWieldedPlayer {
	@Unique
	private boolean armory$offhandAttack;

	@Unique
	private boolean armory$nextOffhandAttack;

	@Unique
	private int armory$pendingOffhandSwingTick = -1;

	@Inject(method = "attack", at = @At("HEAD"))
	private void armory$beginAttack(Entity target, CallbackInfo ci) {
		Player self = (Player) (Object) this;
		boolean active = DualWielded.isActive(self) && DualWielded.hasGhost(self);
		if (!active) {
			armory$offhandAttack = false;
			armory$nextOffhandAttack = false;
			return;
		}

		armory$offhandAttack = armory$nextOffhandAttack;
		armory$nextOffhandAttack = !armory$nextOffhandAttack;

		if (armory$offhandAttack && self.level().isClientSide()) {
			armory$pendingOffhandSwingTick = self.tickCount;
		}
	}

	@Inject(method = "attack", at = @At("RETURN"))
	private void armory$endAttack(Entity target, CallbackInfo ci) {
		armory$offhandAttack = false;
	}

	@Inject(method = "getWeaponItem", at = @At("HEAD"), cancellable = true)
	private void armory$selectWeaponHand(CallbackInfoReturnable<ItemStack> cir) {
		if (!armory$offhandAttack) {
			return;
		}
		Player self = (Player) (Object) this;
		ItemStack offhand = self.getItemBySlot(EquipmentSlot.OFFHAND);
		if (DualWielded.isGhost(offhand)) {
			cir.setReturnValue(offhand);
		}
	}

	@Override
	public boolean armory$consumeOffhandSwing() {
		Player self = (Player) (Object) this;
		boolean pending = armory$pendingOffhandSwingTick == self.tickCount;
		armory$pendingOffhandSwingTick = -1;
		return pending;
	}
}
