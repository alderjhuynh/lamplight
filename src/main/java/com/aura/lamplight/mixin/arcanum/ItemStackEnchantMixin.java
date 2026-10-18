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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        SlotType slot = slotOpt.get();
        var existing = ArcanumEnchantmentHelper.getEnchantmentInSlot(self, slot);
        if (existing.isPresent()) {

            if (existing.get().equals(enchantment)) {

                return;
            } else {

                ci.cancel();
                return;
            }
        }

        if (!enchantment.value().isSupportedItem(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", at = @At("RETURN"))
    private <T> void arcanum$afterSet(net.minecraft.core.component.DataComponentType<T> type, T value, CallbackInfoReturnable<T> cir) {
        ItemStack self = (ItemStack) (Object) this;

        ArcanumEnchantmentHelper.stripRemovedEnchantments(self);
        if (type == DataComponents.STORED_ENCHANTMENTS) {
            if (self.is(Items.ENCHANTED_BOOK)) {
                ArcanumEnchantmentHelper.enforceSingleEnchantment(self);

                ArcanumEnchantmentHelper.stripRemovedEnchantments(self);
            }
        }
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void arcanum$onStackedOnOther(Slot slot, ClickAction clickAction, Player player, CallbackInfoReturnable<Boolean> cir) {
        if (clickAction != ClickAction.SECONDARY) return;
        ItemStack carried = (ItemStack) (Object) this;
        ItemStack target = slot.getItem();

        if (!isBookOntoTarget(carried, target)) return;
        boolean applied = tryApplyBookToTarget(carried, target, slot, player);
        if (applied) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void arcanum$onOtherStackedOnMe(ItemStack otherStack, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess, CallbackInfoReturnable<Boolean> cir) {
        if (clickAction != ClickAction.SECONDARY) return;
        ItemStack target = (ItemStack) (Object) this;
        ItemStack book = otherStack;

        if (!isBookOntoTarget(book, target)) return;
        boolean applied = tryApplyBookToTargetWithAccess(book, target, slot, player, slotAccess);
        if (applied) {
            cir.setReturnValue(true);
        }
    }

    private static boolean isBookOntoTarget(ItemStack book, ItemStack target) {
        if (book.isEmpty() || target.isEmpty()) return false;
        if (!book.is(Items.ENCHANTED_BOOK)) return false;
        if (target.is(Items.ENCHANTED_BOOK)) return false;

        if (book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).size() != 1) {

            ArcanumEnchantmentHelper.enforceSingleEnchantment(book);
            if (book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).size() != 1) return false;
        }

        var storedOpt = ArcanumEnchantmentHelper.getSingleStoredEnchantment(book);
        if (storedOpt.isPresent() && RemovedEnchantments.isRemoved(storedOpt.get())) return false;

        return !ArcanumEnchantmentHelper.getSlotsForItem(target).isEmpty();
    }

    private static boolean tryApplyBookToTarget(ItemStack book, ItemStack target, Slot slot, Player player) {
        Optional<Holder<Enchantment>> enchOpt = ArcanumEnchantmentHelper.getSingleStoredEnchantment(book);
        if (enchOpt.isEmpty()) return false;
        Holder<Enchantment> holder = enchOpt.get();
        int level = ArcanumEnchantmentHelper.getSingleStoredLevel(book);

        if (!ArcanumEnchantmentHelper.canApplyEnchantment(target, holder, level)) {
            return false;
        }

        boolean ok = ArcanumEnchantmentHelper.applyEnchantment(target, holder, level);
        if (!ok) return false;

        if (!player.getAbilities().instabuild) {
            book.shrink(1);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

        slot.setChanged();

        return true;
    }

    private static boolean tryApplyBookToTargetWithAccess(ItemStack book, ItemStack target, Slot slot, Player player, SlotAccess access) {
        Optional<Holder<Enchantment>> enchOpt = ArcanumEnchantmentHelper.getSingleStoredEnchantment(book);
        if (enchOpt.isEmpty()) return false;
        Holder<Enchantment> holder = enchOpt.get();
        int level = ArcanumEnchantmentHelper.getSingleStoredLevel(book);

        if (!ArcanumEnchantmentHelper.canApplyEnchantment(target, holder, level)) {
            return false;
        }

        boolean ok = ArcanumEnchantmentHelper.applyEnchantment(target, holder, level);
        if (!ok) return false;

        if (!player.getAbilities().instabuild) {
            book.shrink(1);
        }

        access.set(book);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
        slot.setChanged();
        return true;
    }
}
