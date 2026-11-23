package com.aura.lamplight.mixin.nullbloom;

import com.aura.lamplight.nullbloom.NullbloomBlocks;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public class SoulboundDeathMixin {
	@Inject(method = "die", at = @At("HEAD"))
	private void lamplight$removeSoulboundOnDeath(DamageSource cause, CallbackInfo ci) {
		net.minecraft.server.level.ServerPlayer self = (net.minecraft.server.level.ServerPlayer) (Object) this;
		Inventory inv = self.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
				inv.setItem(i, ItemStack.EMPTY);
			}
		}
	}
}
