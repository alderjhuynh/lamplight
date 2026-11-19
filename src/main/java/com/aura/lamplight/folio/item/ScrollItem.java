package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.SpellSchool;
import com.aura.lamplight.folio.spell.Spells;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class ScrollItem extends Item {

    private final SpellSchool school;

    public ScrollItem(final Item.Properties properties, final SpellSchool school) {
        super(properties);
        this.school = school;
    }

    public SpellSchool school() {
        return school;
    }

     public static @Nullable Spell getSpell(final ItemStack stack) {
        Identifier spellId = stack.get(ModDataComponents.SPELL);
        Spell spell = spellId == null ? null : Spells.byId(spellId);
        if (spell == null) {
            return null;
        }
        Item item = stack.getItem();
        if (item instanceof ScrollItem scroll && scroll.school() != spell.school()) {
            return null;
        }
        return spell;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Spell spell = getSpell(stack);
        if (spell == null) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            spell.cast(serverLevel, player, stack);
            stack.consume(1, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
        final ItemStack itemStack,
        final Item.TooltipContext context,
        final TooltipDisplay display,
        final Consumer<Component> builder,
        final TooltipFlag tooltipFlag
    ) {
        Spell spell = getSpell(itemStack);
        if (spell != null) {
            builder.accept(spell.name().copy().withStyle(ChatFormatting.AQUA));
        } else {
            builder.accept(Component.translatable(itemStack.getItem().getDescriptionId() + ".blank").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
