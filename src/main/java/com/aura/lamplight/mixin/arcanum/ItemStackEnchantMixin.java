package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.RemovedEnchantments;
import com.aura.lamplight.arcanum.enchantment.SlotType;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackEnchantMixin {

    @Inject(method = "enchant", at = @At("HEAD"), cancellable = true)
    private void arcanum$checkSlotBeforeEnchant(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
        if (RemovedEnchantments.isRemoved(enchantment)) {
            ci.cancel();
            return;
        }

        ItemStack self = (ItemStack) (Object) this;
        if (ArcanumEnchantmentHelper.getSlotsForItem(self).isEmpty()) return;

        Optional<SlotType> slotOpt = SlotType.forEnchantment(enchantment);
        if (slotOpt.isEmpty()) {
            ci.cancel();
            return;
        }

        Optional<Holder<Enchantment>> existing =
                ArcanumEnchantmentHelper.getEnchantmentInSlot(self, slotOpt.get());
        if (existing.isPresent() && existing.get().equals(enchantment)) {
            // re-apply same enchantment to raise level
            return;
        }

        if (!ArcanumEnchantmentHelper.canApplyEnchantment(self, enchantment, level)) {
            ci.cancel();
        }
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private <T> void arcanum$afterSet(net.minecraft.core.component.DataComponentType<T> type, T value,
                                      CallbackInfoReturnable<T> cir) {
        ItemStack self = (ItemStack) (Object) this;
        ArcanumEnchantmentHelper.stripRemovedEnchantments(self);
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void arcanum$onStackedOnOther(Slot slot, ClickAction clickAction, Player player,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (clickAction != ClickAction.SECONDARY) return;
        ItemStack carried = (ItemStack) (Object) this;
        ItemStack target = slot.getItem();

        if (!isBookOntoTarget(carried, target)) return;
        if (tryApplyBookToTarget(carried, target, slot, player)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void arcanum$onOtherStackedOnMe(ItemStack otherStack, Slot slot, ClickAction clickAction,
                                            Player player, SlotAccess slotAccess,
                                            CallbackInfoReturnable<Boolean> cir) {
        if (clickAction != ClickAction.SECONDARY) return;
        ItemStack target = (ItemStack) (Object) this;
        ItemStack book = otherStack;

        if (!isBookOntoTarget(book, target)) return;
        if (tryApplyBookToTargetWithAccess(book, target, slot, player, slotAccess)) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isBookOntoTarget(ItemStack book, ItemStack target) {
        if (book.isEmpty() || target.isEmpty()) return false;
        if (!book.is(Items.ENCHANTED_BOOK)) return false;
        if (target.is(Items.ENCHANTED_BOOK)) return false;

        ArcanumEnchantmentHelper.stripRemovedEnchantments(book);
        if (!ArcanumEnchantmentHelper.hasStoredEnchantments(book)) return false;

        return !ArcanumEnchantmentHelper.getSlotsForItem(target).isEmpty();
    }

    private static boolean tryApplyBookToTarget(ItemStack book, ItemStack target, Slot slot, Player player) {
        if (!applyBook(book, target, player)) return false;
        slot.setChanged();
        return true;
    }

    private static boolean tryApplyBookToTargetWithAccess(ItemStack book, ItemStack target, Slot slot,
                                                          Player player, SlotAccess access) {
        if (!applyBook(book, target, player)) return false;
        access.set(book);
        slot.setChanged();
        return true;
    }

    private static boolean applyBook(ItemStack book, ItemStack target, Player player) {
        ItemEnchantments stored = ArcanumEnchantmentHelper.getStoredEnchantments(book);
        if (!ArcanumEnchantmentHelper.canApplyAll(target, stored)) {
            return false;
        }

        ItemEnchantments original = ArcanumEnchantmentHelper.getEnchantments(target);

        for (Holder<Enchantment> holder : stored.keySet()) {
            int level = stored.getLevel(holder);
            if (!ArcanumEnchantmentHelper.applyEnchantment(target, holder, level)) {
                target.set(DataComponents.ENCHANTMENTS, original);
                return false;
            }
        }

        if (!player.getAbilities().instabuild) {
            book.shrink(1);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }
}
