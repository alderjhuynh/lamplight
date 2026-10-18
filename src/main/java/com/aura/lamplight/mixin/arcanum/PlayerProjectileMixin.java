package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerProjectileMixin {

    @Inject(method = "getProjectile", at = @At("HEAD"), cancellable = true)
    private void arcanum$preferRocketsForPropulsion(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (weapon == null || weapon.isEmpty()) return;
        if (!weapon.is(Items.CROSSBOW)) return;
        if (!ArcanumEnchantmentHelper.hasPropulsion(weapon)) return;

        Player self = (Player) (Object) this;

        ItemStack heldFirework = ProjectileWeaponItem.getHeldProjectile(self, stack -> stack.is(Items.FIREWORK_ROCKET));
        if (!heldFirework.isEmpty()) {
            cir.setReturnValue(heldFirework);
            return;
        }

        Inventory inv = self.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(Items.FIREWORK_ROCKET)) {
                cir.setReturnValue(s);
                return;
            }
        }

    }

    @Inject(method = "getProjectile", at = @At("RETURN"), cancellable = true)
    private void arcanum$infinityFindRocketInInventory(ItemStack weapon, CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue() != null && !cir.getReturnValue().isEmpty()) return;
        if (weapon == null || weapon.isEmpty()) return;
        if (!weapon.is(Items.CROSSBOW)) return;
        if (ArcanumEnchantmentHelper.hasPropulsion(weapon)) return;
        if (!ArcanumEnchantmentHelper.hasInfinity(weapon)) return;

        Player self = (Player) (Object) this;
        // Held projectiles already checked by vanilla (ARROW_OR_FIREWORK). If we are here, no held rocket/arrow and no arrows in inventory.
        // Allow Infinity crossbows to load rockets from inventory, mirroring arrow behavior.
        Inventory inv = self.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(Items.FIREWORK_ROCKET)) {
                cir.setReturnValue(s);
                return;
            }
        }
    }
}
