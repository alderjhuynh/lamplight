package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.Arcanum;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantments;
import com.aura.lamplight.arcanum.enchantment.FreezeHandler;
import com.aura.lamplight.arcanum.enchantment.SpinAttackRailData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@Mixin(TridentItem.class)
public class TridentItemInfernoMixin {

    private static final SoundEvent ARCANUM$INFERNO_LAUNCH =
            SoundEvent.createVariableRangeEvent(Arcanum.id("inferno_launch"));

    @WrapOperation(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"
        )
    )
    private boolean arcanum$tridentsAlwaysCharge(Player player, Operation<Boolean> op) {
        return true;
    }

    @WrapOperation(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"
        )
    )
    private boolean arcanum$releaseIsASpin(Player player, Operation<Boolean> op) {
        return true;
    }

    @WrapOperation(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getTridentSpinAttackStrength(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)F"
        )
    )
    private float arcanum$spinOnlyWhenApplicable(ItemStack stack, LivingEntity player, Operation<Float> op) {
        float riptide = op.call(stack, player); // 0 without riptide
        int inferno = FreezeHandler.getLevel(stack, ArcanumEnchantments.INFERNO);
        float strength = Math.max(riptide, inferno);
        if (strength <= 0) {
            return 0;
        }
        boolean canSpin = !player.isPassenger()
                && ((riptide > 0 && player.isInWaterOrRain()) || (inferno > 0 && player.isOnFire()));
        if (!canSpin) {
            return 0;
        }
        if (player instanceof SpinAttackRailData railPlayer) {
            railPlayer.arcanum$setInfernoSpinRail(inferno > 0 && player.isOnFire());
        }
        return strength;
    }

    @WrapOperation(
        method = "releaseUsing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;pickHighestLevel(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/component/DataComponentType;)Ljava/util/Optional;"
        )
    )
    private Optional<Holder<SoundEvent>> arcanum$infernoLaunchSound(
        ItemStack stack,
        DataComponentType<List<Holder<SoundEvent>>> component,
        Operation<Optional<Holder<SoundEvent>>> op
    ) {
        Optional<Holder<SoundEvent>> result = op.call(stack, component);
        if (FreezeHandler.getLevel(stack, ArcanumEnchantments.INFERNO) > 0) {
            return Optional.of(Holder.direct(ARCANUM$INFERNO_LAUNCH));
        }
        return result;
    }
}
