package com.aura.lamplight.folio.menu;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.item.SpellbookItem;
import com.aura.lamplight.folio.spell.Spell;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SpellbookContainer extends SimpleContainer {

    private final Player player;
    private final SpellbookItem.@org.jspecify.annotations.Nullable Equipped equipped;
    private final int capacity;
    private boolean writing;

    public SpellbookContainer(final Player player, final SpellbookItem.@org.jspecify.annotations.Nullable Equipped equipped) {
        super(capacityOf(equipped));
        this.player = player;
        this.equipped = equipped;
        this.capacity = this.getContainerSize();
        if (equipped != null) {
            List<Identifier> spells = SpellbookItem.getSpells(equipped.stack());
            for (int i = 0; i < Math.min(this.capacity, spells.size()); i++) {
                Spell spell = com.aura.lamplight.folio.spell.Spells.byId(spells.get(i));
                if (spell != null) {
                    this.getItems().set(i, SpellbookItem.scrollFor(spell));
                }
            }
        }
    }

    private static int capacityOf(final SpellbookItem.@org.jspecify.annotations.Nullable Equipped equipped) {
        return equipped == null ? SpellbookItem.CAPACITY : SpellbookItem.capacity(equipped.stack());
    }

    public SpellbookItem.@org.jspecify.annotations.Nullable Equipped equipped() {
        return this.equipped;
    }

    public int capacity() {
        return this.capacity;
    }

    @Override
    public void setChanged() {
        if (this.writing || this.player.level().isClientSide() || this.equipped == null) {
            return;
        }
        this.writeBack();
    }

    public void writeBack() {
        if (this.player.level().isClientSide() || this.equipped == null || this.writing) {
            return;
        }
        this.writing = true;
        try {
            List<Identifier> ids = new ArrayList<>();
            for (int i = 0; i < this.capacity; i++) {
                Spell spell = SpellbookItem.spellOfScroll(this.getItem(i));
                if (spell != null && !ids.contains(spell.id())) {
                    ids.add(spell.id());
                }
            }

            ItemStack book = this.equipped.stack();
            book.set(ModDataComponents.SPELLBOOK_SPELLS, List.copyOf(ids));
            SpellbookItem.setSelected(book, SpellbookItem.getSelected(book));

            for (int i = 0; i < this.capacity; i++) {
                Spell spell = i < ids.size() ? com.aura.lamplight.folio.spell.Spells.byId(ids.get(i)) : null;
                ItemStack canonical = spell == null ? ItemStack.EMPTY : SpellbookItem.scrollFor(spell);
                this.getItems().set(i, canonical);
            }

            this.equipped.sync(this.player);
        } finally {
            this.writing = false;
        }
    }

    @Override
    public boolean stillValid(final Player p) {
        return true;
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.setChanged();
    }
}
