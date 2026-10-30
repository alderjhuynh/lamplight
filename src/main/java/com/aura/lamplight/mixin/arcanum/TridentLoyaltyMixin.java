package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.LeechHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrownTrident.class)
public class TridentLoyaltyMixin {

    @Unique
    private int arcanum$thrownFromSlot = -1;

    @Inject(method = "getLoyaltyFromItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void arcanum$alwaysLoyalty(ItemStack stack, CallbackInfoReturnable<Byte> cir) {
        cir.setReturnValue((byte) 1);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), remap = false)
    private void arcanum$rememberThrownFromSlot(Level level, LivingEntity thrower, ItemStack stack, CallbackInfo ci) {
        if (!(thrower instanceof Player player)) {
            return;
        }
        this.arcanum$thrownFromSlot = player.getUsedItemHand() == InteractionHand.OFF_HAND
            ? Inventory.SLOT_OFFHAND
            : player.getInventory().getSelectedSlot();
    }

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void arcanum$returnToThrownSlot(Player player, CallbackInfoReturnable<Boolean> cir) {
        ThrownTrident self = (ThrownTrident) (Object) this;
        if (LeechHandler.isLatched(self)) {
            cir.setReturnValue(false);
            return;
        }
        AbstractArrow arrow = (AbstractArrow) self;
        int slot = this.arcanum$thrownFromSlot;
        if (slot < 0 || self.getOwner() != player || arrow.pickup == AbstractArrow.Pickup.CREATIVE_ONLY) {
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack trident = arrow.getPickupItemStackOrigin().copy();
        if (slot < inventory.getContainerSize() && inventory.getItem(slot).isEmpty()) {
            inventory.setItem(slot, trident);
        } else if (!inventory.add(trident)) {
            if (self.level() instanceof ServerLevel server) {
                self.spawnAtLocation(server, trident, 0.1F);
            }
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void arcanum$saveThrownFromSlot(ValueOutput output, CallbackInfo ci) {
        if (this.arcanum$thrownFromSlot >= 0) {
            output.putInt("ThrownFromSlot", this.arcanum$thrownFromSlot);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void arcanum$loadThrownFromSlot(ValueInput input, CallbackInfo ci) {
        this.arcanum$thrownFromSlot = input.getIntOr("ThrownFromSlot", -1);
    }
}
