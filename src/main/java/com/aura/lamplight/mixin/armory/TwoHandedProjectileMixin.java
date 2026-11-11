package com.aura.lamplight.mixin.armory;

import com.aura.lamplight.armory.TwoHanded;
import java.util.function.Predicate;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// a two handed ranged weapon cannot draw ammo from the offhand: the offhand
// does not exist. mirrors the vanilla scan but skips the offhand slot.
@Mixin(Player.class)
public abstract class TwoHandedProjectileMixin {
	@Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
	private void armory$noOffhandAmmo(ItemStack heldWeapon, CallbackInfoReturnable<ItemStack> cir) {
		if (!(heldWeapon.getItem() instanceof ProjectileWeaponItem weapon) || !TwoHanded.isTwoHanded(heldWeapon)) {
			return;
		}

		Player self = (Player) (Object) this;
		ItemStack heldProjectile = ProjectileWeaponItem.getHeldProjectile(self, weapon.getSupportedHeldProjectiles());
		if (!heldProjectile.isEmpty()) {
			cir.setReturnValue(heldProjectile);
			return;
		}

		Predicate<ItemStack> supportedProjectiles = weapon.getAllSupportedProjectiles();
		Inventory inventory = self.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (i == Inventory.SLOT_OFFHAND) {
				continue;
			}
			ItemStack candidate = inventory.getItem(i);
			if (supportedProjectiles.test(candidate)) {
				cir.setReturnValue(candidate);
				return;
			}
		}

		cir.setReturnValue(self.hasInfiniteMaterials() ? new ItemStack(Items.ARROW) : ItemStack.EMPTY);
	}
}
