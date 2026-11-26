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
import org.jspecify.annotations.Nullable;

public final class SpellbookContainer extends SimpleContainer {

    private final Player player;
    private final @Nullable ItemStack book;
    private final Runnable onChanged;
    private final int capacity;
    private boolean writing;

    public SpellbookContainer(
        final Player player,
        final @Nullable ItemStack book,
        final Runnable onChanged
    ) {
        super(book == null ? SpellbookItem.CAPACITY : SpellbookItem.capacity(book));
        this.player = player;
        this.book = book;
        this.onChanged = onChanged;
        this.capacity = this.getContainerSize();
        if (book != null) {
            List<Identifier> spells = SpellbookItem.getSpells(book);
            for (int i = 0; i < Math.min(this.capacity, spells.size()); i++) {
                Spell spell = com.aura.lamplight.folio.spell.Spells.byId(spells.get(i));
                if (spell != null) {
                    this.getItems().set(i, SpellbookItem.scrollFor(spell));
                }
            }
        }
    }

    public int capacity() {
        return this.capacity;
    }

    @Override
    public void setChanged() {
        if (this.writing || this.player.level().isClientSide() || this.book == null) {
            return;
        }
        this.writeBack();
    }

    public void writeBack() {
        if (this.player.level().isClientSide() || this.book == null || this.writing) {
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

            this.book.set(ModDataComponents.SPELLBOOK_SPELLS, List.copyOf(ids));
            SpellbookItem.setSelected(this.book, SpellbookItem.getSelected(this.book));

            for (int i = 0; i < this.capacity; i++) {
                Spell spell = i < ids.size() ? com.aura.lamplight.folio.spell.Spells.byId(ids.get(i)) : null;
                ItemStack canonical = spell == null ? ItemStack.EMPTY : SpellbookItem.scrollFor(spell);
                this.getItems().set(i, canonical);
            }

            this.onChanged.run();
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
