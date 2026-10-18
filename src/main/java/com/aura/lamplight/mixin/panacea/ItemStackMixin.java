package com.aura.lamplight.mixin.panacea;

import com.aura.lamplight.panacea.item.TippedWeapon;
import com.aura.lamplight.panacea.registry.ModComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Spends one tipped-weapon charge per successful melee hit.
 *
 * <p>Vanilla only reaches {@code ItemStack#postHurtEnemy} after damage was dealt,
 * server-side, once per attack (sweep hits excluded), making it the exact
 * "hit landed" signal needed here.</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "postHurtEnemy", at = @At("TAIL"))
    private void panacea$consumeTippedCharge(LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (attacker.level().isClientSide() || self.isEmpty()) {
            return;
        }

        TippedWeapon tipped = self.get(ModComponents.TIPPED_WEAPON);
        if (tipped != null) {
            tipped.consumeOnHit(self, target);
        }
    }
}
