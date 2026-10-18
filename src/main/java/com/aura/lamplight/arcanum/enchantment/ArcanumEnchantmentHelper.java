package com.aura.lamplight.arcanum.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ArcanumEnchantmentHelper {
    private ArcanumEnchantmentHelper() {}

    public static List<SlotType> getSlotsForItem(ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        Item item = stack.getItem();

        List<SlotType> slots = new ArrayList<>();

        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq != null) {
            EquipmentSlot slot = eq.slot();
            if (slot == EquipmentSlot.HEAD) {
                slots.add(SlotType.SECONDARY);
                return List.copyOf(slots);
            } else if (slot == EquipmentSlot.CHEST) {
                slots.add(SlotType.PROTECTION);
                slots.add(SlotType.SECONDARY);
                return List.copyOf(slots);
            } else if (slot == EquipmentSlot.LEGS) {
                slots.add(SlotType.SECONDARY);
                return List.copyOf(slots);
            } else if (slot == EquipmentSlot.FEET) {
                slots.add(SlotType.SECONDARY);
                return List.copyOf(slots);
            }
        }

        boolean isTool = isTool(item);
        boolean isMelee = isMeleeWeapon(item);
        boolean isRanged = isRangedWeapon(item);

        if (!slots.isEmpty()) {
            return List.copyOf(slots);
        }

        if (isTool) {
            slots.add(SlotType.TOOL);
        }
        if (isMelee) {
            slots.add(SlotType.MAIN_DAMAGE);
            slots.add(SlotType.SECONDARY_DAMAGE);
        }
        if (isRanged) {
            slots.add(SlotType.MAIN_EFFECT);
            slots.add(SlotType.SECONDARY_EFFECT);
        }

        if (slots.isEmpty()) {
            if (isHelmet(item)) slots.add(SlotType.SECONDARY);
            else if (isChestplate(item)) { slots.add(SlotType.PROTECTION); slots.add(SlotType.SECONDARY); }
            else if (isLeggings(item)) slots.add(SlotType.SECONDARY);
            else if (isBoots(item)) slots.add(SlotType.SECONDARY);
        }

        return List.copyOf(slots);
    }

    private static boolean isHelmet(Item item) {
        return item == Items.LEATHER_HELMET || item == Items.CHAINMAIL_HELMET || item == Items.IRON_HELMET
                || item == Items.GOLDEN_HELMET || item == Items.COPPER_HELMET || item == Items.DIAMOND_HELMET
                || item == Items.NETHERITE_HELMET || item == Items.TURTLE_HELMET;
    }

    private static boolean isChestplate(Item item) {
        return item == Items.LEATHER_CHESTPLATE || item == Items.CHAINMAIL_CHESTPLATE || item == Items.IRON_CHESTPLATE
                || item == Items.GOLDEN_CHESTPLATE || item == Items.COPPER_CHESTPLATE || item == Items.DIAMOND_CHESTPLATE
                || item == Items.NETHERITE_CHESTPLATE;
    }

    private static boolean isLeggings(Item item) {
        return item == Items.LEATHER_LEGGINGS || item == Items.CHAINMAIL_LEGGINGS || item == Items.IRON_LEGGINGS
                || item == Items.GOLDEN_LEGGINGS || item == Items.COPPER_LEGGINGS || item == Items.DIAMOND_LEGGINGS
                || item == Items.NETHERITE_LEGGINGS;
    }

    private static boolean isBoots(Item item) {
        return item == Items.LEATHER_BOOTS || item == Items.CHAINMAIL_BOOTS || item == Items.IRON_BOOTS
                || item == Items.GOLDEN_BOOTS || item == Items.COPPER_BOOTS || item == Items.DIAMOND_BOOTS
                || item == Items.NETHERITE_BOOTS;
    }

    private static boolean isTool(Item item) {
        return item == Items.WOODEN_PICKAXE || item == Items.STONE_PICKAXE || item == Items.IRON_PICKAXE
                || item == Items.GOLDEN_PICKAXE || item == Items.COPPER_PICKAXE || item == Items.DIAMOND_PICKAXE
                || item == Items.NETHERITE_PICKAXE
                || item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE
                || item == Items.GOLDEN_AXE || item == Items.COPPER_AXE || item == Items.DIAMOND_AXE
                || item == Items.NETHERITE_AXE
                || item == Items.WOODEN_SHOVEL || item == Items.STONE_SHOVEL || item == Items.IRON_SHOVEL
                || item == Items.GOLDEN_SHOVEL || item == Items.COPPER_SHOVEL || item == Items.DIAMOND_SHOVEL
                || item == Items.NETHERITE_SHOVEL
                || item == Items.WOODEN_HOE || item == Items.STONE_HOE || item == Items.IRON_HOE
                || item == Items.GOLDEN_HOE || item == Items.COPPER_HOE || item == Items.DIAMOND_HOE
                || item == Items.NETHERITE_HOE
                || item == Items.SHEARS || item == Items.FISHING_ROD;
    }

    private static boolean isMeleeWeapon(Item item) {
        return item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD || item == Items.COPPER_SWORD || item == Items.DIAMOND_SWORD
                || item == Items.NETHERITE_SWORD
                || item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE
                || item == Items.GOLDEN_AXE || item == Items.COPPER_AXE || item == Items.DIAMOND_AXE
                || item == Items.NETHERITE_AXE
                || item == Items.WOODEN_SPEAR || item == Items.STONE_SPEAR || item == Items.IRON_SPEAR
                || item == Items.GOLDEN_SPEAR || item == Items.COPPER_SPEAR || item == Items.DIAMOND_SPEAR
                || item == Items.NETHERITE_SPEAR
                || item == Items.MACE || item == Items.TRIDENT;
    }

    private static boolean isRangedWeapon(Item item) {
        return item == Items.BOW || item == Items.CROSSBOW;
    }

    public static boolean hasEnchantmentForSlot(ItemStack stack, SlotType slot) {
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            Optional<SlotType> t = SlotType.forEnchantment(holder);
            if (t.isPresent() && t.get() == slot) return true;
        }
        return false;
    }

    public static Optional<Holder<Enchantment>> getEnchantmentInSlot(ItemStack stack, SlotType slot) {
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            Optional<SlotType> t = SlotType.forEnchantment(holder);
            if (t.isPresent() && t.get() == slot) return Optional.of(holder);
        }
        return Optional.empty();
    }

    public static int getLevelInSlot(ItemStack stack, SlotType slot) {
        ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            Optional<SlotType> t = SlotType.forEnchantment(holder);
            if (t.isPresent() && t.get() == slot) {
                return enchants.getLevel(holder);
            }
        }
        return 0;
    }

    public static boolean isSlotEmpty(ItemStack stack, SlotType slot) {
        return !hasEnchantmentForSlot(stack, slot);
    }

    public static boolean hasPropulsion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (h.unwrapKey().map(k -> k.equals(ArcanumEnchantments.PROPULSION)).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasEnchantmentOnStack(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack == null || stack.isEmpty()) return false;
        var enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (h.unwrapKey().map(k -> k.equals(key)).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAerodynamics(ItemStack stack) {
        return hasEnchantmentOnStack(stack, ArcanumEnchantments.AERODYNAMICS);
    }

    public static boolean hasDash(ItemStack stack) {
        return hasEnchantmentOnStack(stack, ArcanumEnchantments.DASH);
    }

    public static boolean hasDoubleJump(ItemStack stack) {
        return hasEnchantmentOnStack(stack, ArcanumEnchantments.DOUBLE_JUMP);
    }

    public static boolean hasInfinity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        var enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (h.is(net.minecraft.world.item.enchantment.Enchantments.INFINITY)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAerodynamics(net.minecraft.world.entity.LivingEntity entity) {
        return hasEnchantmentOnStack(entity.getItemBySlot(EquipmentSlot.CHEST), ArcanumEnchantments.AERODYNAMICS);
    }

    public static boolean hasDash(net.minecraft.world.entity.LivingEntity entity) {
        return hasEnchantmentOnStack(entity.getItemBySlot(EquipmentSlot.LEGS), ArcanumEnchantments.DASH);
    }

    public static boolean hasDoubleJump(net.minecraft.world.entity.LivingEntity entity) {
        return hasEnchantmentOnStack(entity.getItemBySlot(EquipmentSlot.FEET), ArcanumEnchantments.DOUBLE_JUMP);
    }

    public static boolean hasWavedashPrereqs(net.minecraft.world.entity.LivingEntity entity) {
        return hasAerodynamics(entity) && hasDash(entity);
    }

    public static boolean hasFlowGlidePrereqs(net.minecraft.world.entity.LivingEntity entity) {
        return hasAerodynamics(entity) && hasDash(entity) && hasDoubleJump(entity);
    }

    public static boolean canApplyEnchantment(ItemStack target, Holder<Enchantment> enchantHolder, int level) {
        if (RemovedEnchantments.isRemoved(enchantHolder)) {
            return false;
        }
        Optional<SlotType> slotOpt = SlotType.forEnchantment(enchantHolder);
        if (slotOpt.isEmpty()) {
            return false;
        }
        SlotType slot = slotOpt.get();
        List<SlotType> slots = getSlotsForItem(target);
        if (!slots.contains(slot)) return false;
        if (!isSlotEmpty(target, slot)) return false;

        Optional<ResourceKey<Enchantment>> keyOpt = enchantHolder.unwrapKey();
        if (keyOpt.isPresent()) {
            ResourceKey<Enchantment> key = keyOpt.get();
            if (key.equals(ArcanumEnchantments.AERODYNAMICS) && !isChestplate(target.getItem())) return false;
            if (key.equals(ArcanumEnchantments.DASH) && !isLeggings(target.getItem())) return false;
            if (key.equals(ArcanumEnchantments.DOUBLE_JUMP) && !isBoots(target.getItem())) return false;
        }

        Enchantment ench = enchantHolder.value();
        if (!ench.isSupportedItem(target)) {
            return false;
        }
        if (level < ench.getMinLevel() || level > ench.getMaxLevel()) return false;

        return true;
    }

    public static boolean applyEnchantment(ItemStack target, Holder<Enchantment> holder, int level) {
        if (!canApplyEnchantment(target, holder, level)) return false;
        target.enchant(holder, level);
        return true;
    }

    public static boolean areCompatibleSlotOnly(Holder<Enchantment> first, Holder<Enchantment> second) {
        if (first.equals(second)) return false;
        if (RemovedEnchantments.isRemoved(first) || RemovedEnchantments.isRemoved(second)) {
            return false;
        }
        Optional<SlotType> a = SlotType.forEnchantment(first);
        Optional<SlotType> b = SlotType.forEnchantment(second);
        if (a.isPresent() && b.isPresent()) {
            return a.get() != b.get();
        }
        return false;
    }

    public static boolean isSingleEnchantBook(ItemStack stack) {
        if (!stack.is(Items.ENCHANTED_BOOK)) return false;
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        return stored.size() == 1;
    }

    public static Optional<Holder<Enchantment>> getSingleStoredEnchantment(ItemStack book) {
        if (!book.is(Items.ENCHANTED_BOOK)) return Optional.empty();
        ItemEnchantments stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (stored.size() != 1) return Optional.empty();
        return stored.keySet().stream().findFirst();
    }

    public static int getSingleStoredLevel(ItemStack book) {
        ItemEnchantments stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        Optional<Holder<Enchantment>> h = stored.keySet().stream().findFirst();
        return h.map(stored::getLevel).orElse(0);
    }

    public static boolean enforceSingleEnchantment(ItemStack book) {
        if (!book.is(Items.ENCHANTED_BOOK)) return false;
        ItemEnchantments stored = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (stored.size() <= 1) return false;
        Holder<Enchantment> first = stored.keySet().iterator().next();
        int lvl = stored.getLevel(first);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(first, lvl);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return true;
    }

    public static boolean stripRemovedEnchantments(ItemStack stack) {
        boolean stripped = false;
        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!ench.isEmpty()) {
            boolean hasRemoved = ench.keySet().stream().anyMatch(RemovedEnchantments::isRemoved);
            if (hasRemoved) {
                ItemEnchantments.Mutable clean = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                for (Holder<Enchantment> h : ench.keySet()) {
                    if (!RemovedEnchantments.isRemoved(h)) {
                        clean.set(h, ench.getLevel(h));
                    }
                }
                stack.set(DataComponents.ENCHANTMENTS, clean.toImmutable());
                stripped = true;
            }
        }
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            boolean hasRemovedStored = stored.keySet().stream().anyMatch(RemovedEnchantments::isRemoved);
            if (hasRemovedStored) {
                ItemEnchantments.Mutable cleanStored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                for (Holder<Enchantment> h : stored.keySet()) {
                    if (!RemovedEnchantments.isRemoved(h)) {
                        cleanStored.set(h, stored.getLevel(h));
                    }
                }
                ItemEnchantments result = cleanStored.toImmutable();
                if (result.isEmpty()) {
                     stack.set(DataComponents.STORED_ENCHANTMENTS, result);
                } else {
                    stack.set(DataComponents.STORED_ENCHANTMENTS, result);
                }
                stripped = true;
            }
        }
        return stripped;
    }
}
