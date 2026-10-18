package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.spell.Spell;
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

/**
 * A "host" item that can carry any ice spell in {@link Spells}, so that a
 * single registered item covers every ice spell in the school instead of
 * needing one item per spell.
 *
 * <p>Which spell a given stack casts is stored in the
 * {@link ModDataComponents#SPELL} component. Two stacks of this item only
 * merge together if that component (and everything else about them)
 * matches, so differently-inscribed scrolls never accidentally combine.</p>
 *
 * <p>In scroll form casting is instant, free, and consumes one scroll per
 * cast; mana cost and reusable spellbook storage are handled elsewhere
 * once those systems exist.</p>
 */
public class IceScrollItem extends Item {

    public IceScrollItem(final Item.Properties properties) {
        super(properties);
    }

    /** Returns the spell inscribed on this stack, or {@code null} if it's blank/unrecognized. */
    public static @Nullable Spell getSpell(final ItemStack stack) {
        Identifier spellId = stack.get(ModDataComponents.SPELL);
        return spellId == null ? null : Spells.byId(spellId);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Spell spell = getSpell(stack);
        if (spell == null) {
            // Blank scroll, or a spell id this build no longer recognizes - do nothing.
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
            builder.accept(Component.translatable("item.lamplight.ice_scroll.blank").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
