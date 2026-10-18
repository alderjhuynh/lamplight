package com.aura.lamplight.mixin.arcanum;

import com.aura.lamplight.arcanum.Arcanum;
import com.aura.lamplight.arcanum.enchantment.ArcanumEnchantmentHelper;
import com.aura.lamplight.arcanum.enchantment.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
    private void arcanum$injectSlotTooltip(Item.TooltipContext tooltipContext, Player player, net.minecraft.world.item.TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack) (Object) this;

        if (self.is(Items.ENCHANTED_BOOK)) {
            ItemEnchantments stored = self.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!stored.isEmpty()) {
                List<Component> original = cir.getReturnValue();
                List<Component> modified = new ArrayList<>(original);

                List<Holder<Enchantment>> holders = new ArrayList<>(stored.keySet());
                for (int hi = holders.size() - 1; hi >= 0; hi--) {
                    Holder<Enchantment> holder = holders.get(hi);
                    int lvl = stored.getLevel(holder);
                    String fullname = Enchantment.getFullname(holder, lvl).getString();
                    Optional<SlotType> slotOpt = SlotType.forEnchantment(holder);
                    if (slotOpt.isEmpty()) continue;
                    SlotType slot = slotOpt.get();
                    Component slotName = slot.displayName();

                    Component detail = Component.literal("")
                            .append(slotName.copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

                    int idx = -1;
                    for (int i = 0; i < modified.size(); i++) {
                        if (modified.get(i).getString().equals(fullname)) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx != -1) {
                        modified.add(idx + 1, detail);
                    } else {

                        modified.add(detail);
                    }
                }

                cir.setReturnValue(modified);
                return;
            }
        }

        List<SlotType> slots = ArcanumEnchantmentHelper.getSlotsForItem(self);
        if (slots.isEmpty()) return;

        List<Component> original = cir.getReturnValue();
        List<Component> modified = new ArrayList<>(original);

        Set<String> vanillaEnchantStrings = new HashSet<>();
        var enchants = self.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (Holder<Enchantment> holder : enchants.keySet()) {
            int lvl = enchants.getLevel(holder);
            Component full = Enchantment.getFullname(holder, lvl);
            vanillaEnchantStrings.add(full.getString());
        }
        modified.removeIf(c -> vanillaEnchantStrings.contains(c.getString()));

        List<Component> slotLines = new ArrayList<>();
        for (SlotType slot : slots) {
            var enchInSlot = ArcanumEnchantmentHelper.getEnchantmentInSlot(self, slot);
            if (enchInSlot.isPresent()) {
                Holder<Enchantment> holder = enchInSlot.get();
                int lvl = ArcanumEnchantmentHelper.getLevelInSlot(self, slot);
                Component enchantName = Enchantment.getFullname(holder, lvl);
                Component bookIcon = Component.literal("📖").withStyle(style -> style.withFont(new net.minecraft.network.chat.FontDescription.Resource(Arcanum.id("book"))));
                Component filled = Component.literal("[").withStyle(ChatFormatting.GRAY)
                        .append(bookIcon)
                        .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
                        .append(enchantName.copy().withStyle(ChatFormatting.GRAY));
                slotLines.add(filled);
            } else {
                Component empty = Component.literal("[]").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(slot.displayName().copy().withStyle(ChatFormatting.DARK_GRAY));
                slotLines.add(empty);
            }
        }

        int insertPos = 1;
        if (insertPos > modified.size()) insertPos = modified.size();
        modified.addAll(insertPos, slotLines);

        cir.setReturnValue(modified);
    }
}
