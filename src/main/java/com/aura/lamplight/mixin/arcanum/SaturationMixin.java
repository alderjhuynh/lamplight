package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SaturationMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void arcanum$saturationTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        if (!(self instanceof Player player)) return;
        // Only tick every 80 ticks (4 seconds) - scaled by level via interval
        if (player.tickCount % 80 != 0) return;

        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) return;
        int lvl = FreezeHandler.getLevel(head, ArcanumEnchantments.SATURATION);
        if (lvl <= 0) return;

        // Spec: restores food over time when food is below max, doesnt touch saturation
        // So only affect foodLevel, not saturationLevel
        if (player.getFoodData().getFoodLevel() >= 20) return;

        // Higher level restores more frequently: lvl1 every 80 ticks, lvl3 effectively faster via extra food
        // Add food: 1 per proc at lvl1, 1 + chance for extra at higher
        int foodToAdd = 1;
        if (lvl >= 2 && player.getRandom().nextFloat() < 0.5f * (lvl - 1)) {
            foodToAdd++;
        }

        // Use foodData.eat with saturation 0 to avoid touching saturation
        // eat(int food, float saturation) adds food
        player.getFoodData().eat(foodToAdd, 0.0f);
    }
}
