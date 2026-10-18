package com.aura.lamplight.mixin.arcanum;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class NoDurabilityMixin {

    @Inject(method = "isDamageableItem", at = @At("HEAD"), cancellable = true)
    private void arcanum$neverDamageable(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "isDamaged", at = @At("HEAD"), cancellable = true)
    private void arcanum$neverDamaged(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "getDamageValue", at = @At("HEAD"), cancellable = true)
    private void arcanum$zeroDamage(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Inject(method = "setDamageValue", at = @At("HEAD"), cancellable = true)
    private void arcanum$ignoreSetDamage(int damage, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void arcanum$cancelHurtAndBreakLevel(int amount, ServerLevel level, ServerPlayer player, Consumer<Item> onBroken, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"), cancellable = true)
    private void arcanum$cancelHurtAndBreakSlot(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), cancellable = true)
    private void arcanum$cancelHurtAndBreakHand(int amount, LivingEntity entity, InteractionHand hand, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "hurtWithoutBreaking", at = @At("HEAD"), cancellable = true)
    private void arcanum$cancelHurtWithoutBreaking(int amount, net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "nextDamageWillBreak", at = @At("HEAD"), cancellable = true)
    private void arcanum$neverWillBreak(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
