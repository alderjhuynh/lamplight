package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.server.RebuildHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public class ProvisionerFoodMixin {

    @Unique
    private static final ThreadLocal<Boolean> LAMPLIGHT_PROVISIONER_EAT = new ThreadLocal<>();

    @Inject(method = "onConsume", at = @At("HEAD"))
    private void lamplight$stashEater(Level level, LivingEntity eater, ItemStack stack, Consumable consumable,
                                      CallbackInfo ci) {
        LAMPLIGHT_PROVISIONER_EAT.set(eater instanceof ServerPlayer player
                && RebuildHandler.armorLvl(player, ArcanumEnchantments.PROVISIONER) > 0);
    }

    @ModifyArg(method = "onConsume",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private FoodProperties lamplight$provisionerFood(FoodProperties props) {
        if (!Boolean.TRUE.equals(LAMPLIGHT_PROVISIONER_EAT.get())) return props;
        int nutrition = props.nutrition();
        int boosted = nutrition + Math.max(1, Math.round(nutrition * 0.25F));
        return new FoodProperties(boosted, props.saturation() * 1.25F, props.canAlwaysEat());
    }
}
