package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentTags;
import com.aura.lamplight.arcanum.enchantment.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void arcanum$injectSlotTooltip(Item.TooltipContext tooltipContext, Player player,
                                           net.minecraft.world.item.TooltipFlag tooltipFlag,
                                           CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (self.is(Items.ENCHANTED_BOOK)) {
            arcanum$bookTooltip(self, cir);
            return;
        }

        List<SlotType> slots = ArcanumEnchantmentHelper.getSlotsForItem(self);
        if (slots.isEmpty()) return;

        List<Component> original = cir.getReturnValue();
        List<Component> modified = new ArrayList<>(original);

        List<Component> slotLines = new ArrayList<>();
        Set<String> displayedEnchantments = new HashSet<>();
        for (SlotType slot : slots) {
            Optional<Holder<Enchantment>> enchInSlot = ArcanumEnchantmentHelper.getEnchantmentInSlot(self, slot);
            if (enchInSlot.isPresent()) {
                Holder<Enchantment> holder = enchInSlot.get();
                int lvl = ArcanumEnchantmentHelper.getLevelInSlot(self, slot);
                Component enchantName = arcanum$displayName(holder, lvl);
                displayedEnchantments.add(enchantName.getString());
                Component filled = Component.literal("[").withStyle(ChatFormatting.GRAY)
                        .append(bookIcon())
                        .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                        .append(enchantName);
                slotLines.add(filled);
                slotLines.add(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(arcanum$descriptionFor(holder).copy().withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                Component empty = Component.literal("[]").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(slot.displayName().copy().withStyle(ChatFormatting.DARK_GRAY));
                slotLines.add(empty);
            }
        }

        modified.removeIf(c -> displayedEnchantments.contains(c.getString()));

        int insertPos = Math.min(1, modified.size());
        modified.addAll(insertPos, slotLines);

        cir.setReturnValue(modified);
    }

    private static void arcanum$bookTooltip(ItemStack self, CallbackInfoReturnable<List<Component>> cir) {
        ItemEnchantments stored = ArcanumEnchantmentHelper.getStoredEnchantments(self);
        if (stored.isEmpty()) return;

        List<Component> original = cir.getReturnValue();
        List<Component> modified = new ArrayList<>(original);
        List<Holder<Enchantment>> holders = new ArrayList<>(stored.keySet());

        for (int hi = holders.size() - 1; hi >= 0; hi--) {
            Holder<Enchantment> holder = holders.get(hi);
            int lvl = stored.getLevel(holder);
            String fullname = Enchantment.getFullname(holder, lvl).getString();
            Optional<SlotType> slotOpt = SlotType.forEnchantment(holder);
            if (slotOpt.isEmpty()) continue;

            List<Identifier> sprites = ArcanumEnchantmentHelper.appliesToSprites(holder);
            Component description = arcanum$descriptionFor(holder).copy()
                    .withStyle(ChatFormatting.DARK_GRAY);
            boolean hasIcons = !sprites.isEmpty();

            int idx = -1;
            for (int i = 0; i < modified.size(); i++) {
                if (modified.get(i).getString().equals(fullname)) {
                    idx = i;
                    break;
                }
            }
            if (idx != -1) {
                if (ArcanumEnchantmentTags.isCapstone(holder)) {
                    modified.set(idx, modified.get(idx).copy().withStyle(ChatFormatting.GOLD));
                }
                modified.add(idx + 1, description);
                if (hasIcons) {
                    modified.add(idx + 2, arcanum$appliesLine(sprites));
                }
            } else {
                modified.add(description);
                if (hasIcons) {
                    modified.add(arcanum$appliesLine(sprites));
                }
            }
        }

        cir.setReturnValue(modified);
    }

    private static Component arcanum$displayName(Holder<Enchantment> holder, int level) {
        if (ArcanumEnchantmentTags.isCapstone(holder)) {
            return Enchantment.getFullname(holder, level).copy().withStyle(ChatFormatting.GOLD);
        }
        return Enchantment.getFullname(holder, level);
    }

    private static Component arcanum$appliesLine(List<Identifier> sprites) {
        MutableComponent line = Component.empty();
        for (Identifier sprite : sprites) {
            line.append(arcanum$spriteGlyph(sprite));
        }
        return line;
    }

    private static Component arcanum$spriteGlyph(Identifier sprite) {
        return Component.literal("\uE001").withStyle(style ->
                style.withColor(ChatFormatting.WHITE)
                        .withFont(new FontDescription.AtlasSprite(AtlasIds.ITEMS, sprite)));
    }

    private static Component bookIcon() {
        return arcanum$spriteGlyph(Identifier.withDefaultNamespace("item/enchanted_book"));
    }

    private static Component arcanum$descriptionFor(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(key -> Component.translatable(key.identifier().toLanguageKey("enchantment") + ".desc"))
                .orElseGet(Component::empty);
    }
}
