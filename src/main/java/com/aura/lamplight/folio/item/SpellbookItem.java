package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.Spells;
import com.aura.lamplight.tinkerer.CharmItem;
import com.aura.lamplight.tinkerer.TinkererSounds;
import com.aura.lamplight.tinkerer.charm.CharmAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class SpellbookItem extends CharmItem {

    public static final String SLOT_TYPE = "spellbook";
    public static final int CAPACITY = 6;

    public SpellbookItem(final Properties properties) {
        super(SLOT_TYPE, properties);
    }

    public static int capacity(final ItemStack book) {
        return CAPACITY;
    }

    public static List<Identifier> getSpells(final ItemStack stack) {
        List<Identifier> list = stack.get(ModDataComponents.SPELLBOOK_SPELLS);
        return list == null ? List.of() : list;
    }

    public static int getSelected(final ItemStack book) {
        int size = getSpells(book).size();
        if (size == 0) {
            return -1;
        }
        Integer stored = book.get(ModDataComponents.SPELLBOOK_SELECTED);
        int index = stored == null ? 0 : stored;
        return Math.floorMod(index, size);
    }

    public static void setSelected(final ItemStack book, final int index) {
        int size = getSpells(book).size();
        book.set(ModDataComponents.SPELLBOOK_SELECTED, size == 0 ? 0 : Math.floorMod(index, size));
    }

    public static @Nullable Spell spellAt(final ItemStack book, final int index) {
        List<Identifier> spells = getSpells(book);
        if (index < 0 || index >= spells.size()) {
            return null;
        }
        return Spells.byId(spells.get(index));
    }

    public static @Nullable Spell spellOfScroll(final ItemStack scroll) {
        if (!(scroll.getItem() instanceof ScrollItem)) {
            return null;
        }
        Identifier spellId = scroll.get(ModDataComponents.SPELL);
        return spellId == null ? null : Spells.byId(spellId);
    }

    public static ItemStack scrollFor(final Spell spell) {
        Item item = BuiltInRegistries.ITEM.getOptional(spell.school().scrollItemId()).orElse(null);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        stack.set(ModDataComponents.SPELL, spell.id());
        return stack;
    }

    public static boolean contains(final ItemStack book, final Identifier spellId) {
        return getSpells(book).contains(spellId);
    }

    public static boolean addSpell(final ItemStack book, final Spell spell) {
        List<Identifier> current = new ArrayList<>(getSpells(book));
        if (current.contains(spell.id()) || current.size() >= capacity(book)) {
            return false;
        }
        current.add(spell.id());
        book.set(ModDataComponents.SPELLBOOK_SPELLS, List.copyOf(current));
        return true;
    }

    public static boolean inscribe(final ItemStack book, final ItemStack scroll) {
        Spell spell = spellOfScroll(scroll);
        if (spell == null) {
            return false;
        }
        if (!addSpell(book, spell)) {
            return false;
        }
        return true;
    }

    public static void sync(final Player player, final Equipped equipped) {
       CharmAttachments.setCharm(player, equipped.slotId(), ItemStack.EMPTY);
        CharmAttachments.setCharm(player, equipped.slotId(), equipped.stack());
    }

    public record Equipped(Identifier slotId, ItemStack stack) {
        public void sync(final Player player) {
            SpellbookItem.sync(player, this);
        }
    }

    public static @Nullable Equipped findEquipped(final Player player) {
        for (var entry : CharmAttachments.wornCharms(player).entrySet()) {
            ItemStack stack = entry.getValue();
            if (!(stack.getItem() instanceof SpellbookItem)) {
                continue;
            }
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id != null) {
                return new Equipped(id, stack);
            }
        }
        return null;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer server)) {
            return InteractionResult.PASS;
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND
            ? InteractionHand.OFF_HAND
            : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);
        Spell heldSpell = spellOfScroll(other);
        if (heldSpell != null) {
            if (contains(stack, heldSpell.id())) {
                server.sendSystemMessage(Component.translatable("message.lamplight.spellbook.duplicate")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            if (!inscribe(stack, other)) {
                server.sendSystemMessage(Component.translatable("message.lamplight.spellbook.full")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            other.shrink(1);
            server.sendSystemMessage(Component.translatable(
                "message.lamplight.spellbook.inscribed", heldSpell.name()
            ).withStyle(ChatFormatting.AQUA), true);
            server.containerMenu.broadcastChanges();
            return InteractionResult.CONSUME;
        }

        Equipped equipped = findEquipped(server);
        if (equipped == null || equipped.stack() != stack) {
            if (!equip(server, stack)) {
                return InteractionResult.PASS;
            }
            equipped = findEquipped(server);
        }
        if (equipped == null) {
            return InteractionResult.PASS;
        }

        Equipped opened = equipped;
        server.openMenu(new net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<Integer>() {
            @Override
            public Integer getScreenOpeningData(final ServerPlayer openingPlayer) {
                return 0;
            }

            @Override
            public Component getDisplayName() {
                return opened.stack().getHoverName();
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                final int containerId,
                final net.minecraft.world.entity.player.Inventory inventory,
                final Player openingPlayer
            ) {
                return new com.aura.lamplight.folio.menu.SpellbookMenu(containerId, inventory);
            }
        });
        return InteractionResult.CONSUME;
    }

    private boolean equip(final ServerPlayer player, final ItemStack stack) {
        Identifier target = firstMatchingSlot(player, stack);
        if (target == null) {
            player.sendSystemMessage(Component.translatable("message.lamplight.spellbook.no_slot")
                .withStyle(ChatFormatting.RED), true);
            return false;
        }
        ItemStack worn = CharmAttachments.charmIn(player, target);
        CharmAttachments.setCharm(player, target, stack.copyWithCount(1));
        if (!worn.isEmpty()) {
            player.addItem(worn);
        }
        stack.shrink(1);
        player.level().playSound(null, player.blockPosition(), TinkererSounds.CHARM_EQUIP,
            SoundSource.PLAYERS, 1.0F, 0.8F);
        player.containerMenu.broadcastChanges();
        return true;
    }

    @Override
    public void appendHoverText(
        final ItemStack book,
        final Item.TooltipContext context,
        final TooltipDisplay display,
        final Consumer<Component> builder,
        final TooltipFlag flag
    ) {
        List<Identifier> spells = getSpells(book);
        builder.accept(Component.translatable("tooltip.lamplight.spellbook.slots", spells.size(), capacity(book))
            .withStyle(ChatFormatting.GRAY));

        if (spells.isEmpty()) {
            builder.accept(Component.translatable("tooltip.lamplight.spellbook.empty")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        } else {
            int selected = getSelected(book);
            for (int i = 0; i < spells.size(); i++) {
                Spell spell = Spells.byId(spells.get(i));
                Component name = spell == null
                    ? Component.literal(spells.get(i).toString())
                    : spell.name();
                boolean isSelected = i == selected;
                Component line = Component.translatable(
                    isSelected ? "tooltip.lamplight.spellbook.selected" : "tooltip.lamplight.spellbook.page",
                    name
                );
                builder.accept(((net.minecraft.network.chat.MutableComponent) line)
                    .withStyle(isSelected ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            }
        }
        builder.accept(Component.translatable("tooltip.lamplight.spellbook.hint")
            .withStyle(ChatFormatting.DARK_GRAY));
    }
}
