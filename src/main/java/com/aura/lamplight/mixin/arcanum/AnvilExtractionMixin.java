package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public class AnvilExtractionMixin {

    @Shadow
    @Final
    private DataSlot cost;

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void arcanum$createExtractionResult(CallbackInfo ci) {
        ItemCombinerMenuAccessor access = (ItemCombinerMenuAccessor) (Object) this;
        Container inputs = access.arcanum$getInputSlots();
        ItemStack target = inputs.getItem(0);
        ItemStack addition = inputs.getItem(1);

        if (addition.is(Items.ENCHANTED_BOOK)
                && !ArcanumEnchantmentHelper.getSlotsForItem(target).isEmpty()) {
            access.arcanum$getResultSlots().setItem(0, ItemStack.EMPTY);
            cost.set(0);
            ci.cancel();
            return;
        }

        if (!ArcanumEnchantmentHelper.isExtraction(target, addition)) {
            return;
        }

        ItemStack book = ArcanumEnchantmentHelper.createExtractionBook(target);
        if (book.isEmpty()) {
            return;
        }

        access.arcanum$getResultSlots().setItem(0, book);
        cost.set(ArcanumEnchantmentHelper.extractionCost(target));
        ci.cancel();
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void arcanum$takeExtractionResult(Player player, ItemStack result, CallbackInfo ci) {
        ItemCombinerMenuAccessor access = (ItemCombinerMenuAccessor) (Object) this;
        Container inputs = access.arcanum$getInputSlots();
        ItemStack target = inputs.getItem(0);
        ItemStack addition = inputs.getItem(1);

        if (!ArcanumEnchantmentHelper.isExtraction(target, addition)) {
            return;
        }

        if (!player.hasInfiniteMaterials()) {
            int xpCost = cost.get();
            if (xpCost > 0) {
                player.giveExperienceLevels(-xpCost);
            }
            addition.shrink(1);
            if (addition.isEmpty()) {
                inputs.setItem(1, ItemStack.EMPTY);
            }
        }

        ArcanumEnchantmentHelper.extractFittedEnchantments(target);
        inputs.setChanged();
        cost.set(0);
        ci.cancel();
    }
}
