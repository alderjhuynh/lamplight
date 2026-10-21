package com.aura.lamplight.arcanum.enchantment;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("all")
public final class ArcanumEnchantmentHelper {
    private static final TagKey<Item> ARCANUM_ARMOR =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "armor/master"));

    private ArcanumEnchantmentHelper() {}

    public static List<SlotType> getSlotsForItem(ItemStack stack) {
        if (stack.isEmpty()) return List.of();

        Equippable eq = stack.get(DataComponents.EQUIPPABLE);
        if (eq != null && stack.is(ARCANUM_ARMOR)) {
            EquipmentSlot slot = eq.slot();
            return switch (slot) {
                case HEAD -> List.of(SlotType.WARD, SlotType.RITE);
                case CHEST -> List.of(SlotType.WARD, SlotType.RITE);
                case LEGS -> List.of(SlotType.WARD, SlotType.STRIDE);
                case FEET -> List.of(SlotType.STRIDE, SlotType.RITE);
                default -> List.of();
            };
        }

        if (isRangedWeapon(stack)) {
            return List.of(SlotType.MAIN_EFFECT, SlotType.SECONDARY_EFFECT, SlotType.FLOURISH);
        }

        boolean melee = isMeleeWeapon(stack);
        boolean tool = isTool(stack);

        // axes are both tools and weapons so we get to make them weird
        if (melee && tool && stack.is(ItemTags.AXES)) {
            return List.of(SlotType.EDGE, SlotType.FLOURISH, SlotType.METHOD);
        }
        if (melee) {
            return List.of(SlotType.EDGE, SlotType.TEMPO, SlotType.FLOURISH);
        }
        if (tool) {
            return List.of(SlotType.METHOD, SlotType.BOON);
        }

        return List.of();
    }

    private static boolean isTool(ItemStack stack) {
        return stack.is(ItemTags.MINING_ENCHANTABLE)
                || stack.is(Items.FISHING_ROD)
                || stack.is(Items.SHEARS);
    }

    private static boolean isMeleeWeapon(ItemStack stack) {
        return stack.is(ItemTags.SHARP_WEAPON_ENCHANTABLE)
                || stack.is(Items.MACE)
                || stack.is(Items.TRIDENT);
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        return stack.is(ItemTags.BOW_ENCHANTABLE)
                || stack.is(ItemTags.CROSSBOW_ENCHANTABLE);
    }

    public static boolean hasEnchantmentForSlot(ItemStack stack, SlotType slot) {
        Optional<Holder<Enchantment>> holder = getEnchantmentInSlot(stack, slot);
        return holder.isPresent();
    }

    public static Optional<Holder<Enchantment>> getEnchantmentInSlot(ItemStack stack, SlotType slot) {
        ItemEnchantments enchants = getEnchantments(stack);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            Optional<SlotType> t = SlotType.forEnchantment(holder);
            if (t.isPresent() && t.get() == slot) return Optional.of(holder);
        }
        return Optional.empty();
    }

    public static int getLevelInSlot(ItemStack stack, SlotType slot) {
        ItemEnchantments enchants = getEnchantments(stack);
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

    public static ItemEnchantments getEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemEnchantments.EMPTY;
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public static ItemEnchantments getStoredEnchantments(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ItemEnchantments.EMPTY;
        return stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public static boolean hasCapstone(ItemStack stack) {
        for (Holder<Enchantment> holder : getEnchantments(stack).keySet()) {
            if (ArcanumEnchantmentTags.isCapstone(holder)) {
                return true;
            }
        }
        return false;
    }

    public static int countCapstones(ItemStack stack) {
        int count = 0;
        for (Holder<Enchantment> holder : getEnchantments(stack).keySet()) {
            if (ArcanumEnchantmentTags.isCapstone(holder)) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasPropulsion(ItemStack stack) {
        return hasEnchantmentOnStack(stack, ArcanumEnchantments.PROPULSION);
    }

    public static boolean hasEnchantmentOnStack(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack == null || stack.isEmpty()) return false;
        for (Holder<Enchantment> h : getEnchantments(stack).keySet()) {
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
        for (Holder<Enchantment> h : getEnchantments(stack).keySet()) {
            if (h.is(net.minecraft.world.item.enchantment.Enchantments.INFINITY)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAerodynamics(net.minecraft.world.entity.LivingEntity entity) {
        // chest is kept as a legacy location for migration, but I imagine I don't have to do this cus not a single person is downloading ts
        return hasEnchantmentOnStack(entity.getItemBySlot(EquipmentSlot.FEET), ArcanumEnchantments.AERODYNAMICS)
                || hasEnchantmentOnStack(entity.getItemBySlot(EquipmentSlot.CHEST), ArcanumEnchantments.AERODYNAMICS);
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
        return hasDash(entity) && hasDoubleJump(entity);
    }

    public static boolean wearsEnchantment(net.minecraft.world.entity.LivingEntity entity, ResourceKey<Enchantment> key) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (hasEnchantmentOnStack(entity.getItemBySlot(slot), key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean wearsCapstone(net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null) return false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (hasCapstone(entity.getItemBySlot(slot))) {
                return true;
            }
        }
        return false;
    }

    public static boolean wearsAny(net.minecraft.world.entity.LivingEntity entity, ResourceKey<Enchantment>... keys) {
        if (entity == null) return false;
        for (ResourceKey<Enchantment> key : keys) {
            if (wearsEnchantment(entity, key)) return true;
        }
        return false;
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
        if (!getSlotsForItem(target).contains(slot)) {
            return false;
        }
        if (!isSlotEmpty(target, slot)) {
            return false;
        }
        if (ArcanumEnchantmentTags.isCapstone(enchantHolder) && hasCapstone(target)) {
            return false;
        }

        Enchantment ench = enchantHolder.value();
        if (!ench.isSupportedItem(target)) {
            return false;
        }
        if (level < ench.getMinLevel() || level > ench.getMaxLevel()) {
            return false;
        }

        return true;
    }

    public static boolean applyEnchantment(ItemStack target, Holder<Enchantment> holder, int level) {
        if (!canApplyEnchantment(target, holder, level)) return false;
        target.enchant(holder, level);
        return true;
    }

    public static boolean canApplyAll(ItemStack target, ItemEnchantments stored) {
        if (stored.isEmpty()) return false;

        List<SlotType> itemSlots = getSlotsForItem(target);
        if (itemSlots.isEmpty()) return false;

        Set<SlotType> usedSlots = new HashSet<>();
        int capstones = countCapstones(target);

        for (Holder<Enchantment> holder : stored.keySet()) {
            int level = stored.getLevel(holder);
            if (RemovedEnchantments.isRemoved(holder)) return false;

            Optional<SlotType> roleOpt = SlotType.forEnchantment(holder);
            if (roleOpt.isEmpty()) return false;

            SlotType role = roleOpt.get();
            if (!itemSlots.contains(role)) return false;
            if (!isSlotEmpty(target, role)) return false;
            if (!usedSlots.add(role)) return false;

            if (ArcanumEnchantmentTags.isCapstone(holder)) {
                capstones++;
                if (capstones > 1) return false;
            }

            Enchantment ench = holder.value();
            if (!ench.isSupportedItem(target)) return false;
            if (level < ench.getMinLevel() || level > ench.getMaxLevel()) return false;
        }

        return true;
    }

    public static boolean areCompatibleSlotOnly(Holder<Enchantment> first, Holder<Enchantment> second) {
        if (first.equals(second)) return false;
        if (RemovedEnchantments.isRemoved(first) || RemovedEnchantments.isRemoved(second)) {
            return false;
        }
        if (ArcanumEnchantmentTags.isCapstone(first) && ArcanumEnchantmentTags.isCapstone(second)) {
            return false;
        }

        List<SlotType> a = SlotType.rolesFor(first);
        List<SlotType> b = SlotType.rolesFor(second);
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        for (SlotType role : a) {
            if (b.contains(role)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasStoredEnchantments(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) && !getStoredEnchantments(stack).isEmpty();
    }

    public static boolean isExtraction(ItemStack target, ItemStack addition) {
        if (target.isEmpty() || addition.isEmpty()) return false;
        if (!addition.is(Items.BOOK)) return false;
        if (getSlotsForItem(target).isEmpty()) return false;
        return hasExtractableEnchantments(target);
    }

    public static boolean hasExtractableEnchantments(ItemStack target) {
        List<SlotType> slots = getSlotsForItem(target);
        if (slots.isEmpty()) return false;
        for (Holder<Enchantment> holder : getEnchantments(target).keySet()) {
            if (RemovedEnchantments.isRemoved(holder)) continue;
            Optional<SlotType> role = SlotType.forEnchantment(holder);
            if (role.isPresent() && slots.contains(role.get())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack createExtractionBook(ItemStack target) {
        ItemEnchantments enchantments = getEnchantments(target);
        if (enchantments.isEmpty()) return ItemStack.EMPTY;

        List<SlotType> slots = getSlotsForItem(target);
        ItemEnchantments.Mutable stored = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (RemovedEnchantments.isRemoved(holder)) continue;
            Optional<SlotType> role = SlotType.forEnchantment(holder);
            if (role.isEmpty() || !slots.contains(role.get())) continue;
            stored.set(holder, enchantments.getLevel(holder));
        }
        if (stored.keySet().isEmpty()) return ItemStack.EMPTY;

        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        book.set(DataComponents.STORED_ENCHANTMENTS, stored.toImmutable());
        return book;
    }

    public static int extractionCost(ItemStack target) {
        ItemEnchantments enchantments = getEnchantments(target);
        List<SlotType> slots = getSlotsForItem(target);
        int count = 0;
        int totalLevels = 0;
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (RemovedEnchantments.isRemoved(holder)) continue;
            Optional<SlotType> role = SlotType.forEnchantment(holder);
            if (role.isEmpty() || !slots.contains(role.get())) continue;
            count++;
            totalLevels += enchantments.getLevel(holder);
        }
        return Math.min(30, 1 + count + totalLevels / 2);
    }

    public static void extractFittedEnchantments(ItemStack target) {
        ItemEnchantments enchantments = getEnchantments(target);
        List<SlotType> slots = getSlotsForItem(target);
        ItemEnchantments.Mutable remaining = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            if (RemovedEnchantments.isRemoved(holder)) continue;
            Optional<SlotType> role = SlotType.forEnchantment(holder);
            boolean fitted = role.isPresent() && slots.contains(role.get());
            if (!fitted) {
                remaining.set(holder, enchantments.getLevel(holder));
            }
        }
        target.set(DataComponents.ENCHANTMENTS, remaining.toImmutable());
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
                stack.set(DataComponents.STORED_ENCHANTMENTS, cleanStored.toImmutable());
                stripped = true;
            }
        }
        return stripped;
    }

        public static List<Identifier> appliesToSprites(Holder<Enchantment> holder) {
        Enchantment ench = holder.value();
        List<Identifier> sprites = new ArrayList<>(4);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_HELMET))) sprites.add(NETHERITE_HELMET);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_CHESTPLATE))) sprites.add(NETHERITE_CHESTPLATE);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_LEGGINGS))) sprites.add(NETHERITE_LEGGINGS);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_BOOTS))) sprites.add(NETHERITE_BOOTS);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_SWORD))) sprites.add(NETHERITE_SWORD);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_AXE))) sprites.add(NETHERITE_AXE);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_SPEAR))) sprites.add(NETHERITE_SPEAR);
        if (ench.isSupportedItem(new ItemStack(Items.MACE))) sprites.add(MACE);
        if (ench.isSupportedItem(new ItemStack(Items.TRIDENT))) sprites.add(TRIDENT);
        if (ench.isSupportedItem(new ItemStack(Items.BOW))) sprites.add(BOW);
        if (ench.isSupportedItem(new ItemStack(Items.CROSSBOW))) sprites.add(CROSSBOW);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_PICKAXE))) sprites.add(NETHERITE_PICKAXE);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_SHOVEL))) sprites.add(NETHERITE_SHOVEL);
        if (ench.isSupportedItem(new ItemStack(Items.DIAMOND_HOE))) sprites.add(NETHERITE_HOE);
        if (ench.isSupportedItem(new ItemStack(Items.SHEARS))) sprites.add(SHEARS);
        if (ench.isSupportedItem(new ItemStack(Items.FISHING_ROD))) sprites.add(FISHING_ROD);
        return List.copyOf(sprites);
    }

    private static final Identifier NETHERITE_HELMET = Identifier.withDefaultNamespace("item/netherite_helmet");
    private static final Identifier NETHERITE_CHESTPLATE = Identifier.withDefaultNamespace("item/netherite_chestplate");
    private static final Identifier NETHERITE_LEGGINGS = Identifier.withDefaultNamespace("item/netherite_leggings");
    private static final Identifier NETHERITE_BOOTS = Identifier.withDefaultNamespace("item/netherite_boots");
    private static final Identifier NETHERITE_SWORD = Identifier.withDefaultNamespace("item/netherite_sword");
    private static final Identifier NETHERITE_AXE = Identifier.withDefaultNamespace("item/netherite_axe");
    private static final Identifier NETHERITE_SPEAR = Identifier.withDefaultNamespace("item/netherite_spear");
    private static final Identifier MACE = Identifier.withDefaultNamespace("item/mace");
    private static final Identifier TRIDENT = Identifier.withDefaultNamespace("item/trident");
    private static final Identifier BOW = Identifier.withDefaultNamespace("item/bow");
    private static final Identifier CROSSBOW = Identifier.withDefaultNamespace("item/crossbow");
    private static final Identifier NETHERITE_PICKAXE = Identifier.withDefaultNamespace("item/netherite_pickaxe");
    private static final Identifier NETHERITE_SHOVEL = Identifier.withDefaultNamespace("item/netherite_shovel");
    private static final Identifier NETHERITE_HOE = Identifier.withDefaultNamespace("item/netherite_hoe");
    private static final Identifier SHEARS = Identifier.withDefaultNamespace("item/shears");
    private static final Identifier FISHING_ROD = Identifier.withDefaultNamespace("item/fishing_rod");
}
