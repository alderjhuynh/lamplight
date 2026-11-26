package com.aura.lamplight.folio.item;

import com.aura.lamplight.folio.component.ModDataComponents;
import com.aura.lamplight.folio.server.FolioCastHandler;
import com.aura.lamplight.folio.spell.ChargeSpell;
import com.aura.lamplight.folio.spell.Spell;
import com.aura.lamplight.folio.spell.SpellSchool;
import com.aura.lamplight.folio.spell.Spells;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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

        if (spell instanceof ChargeSpell chargeSpell) {
            if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer
                && !FolioCastHandler.beginScrollHold(serverPlayer, chargeSpell, stack)) {
                return InteractionResult.FAIL;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        if (level instanceof ServerLevel serverLevel) {
            spell.cast(serverLevel, player, stack);
            stack.consume(1, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(final ItemStack stack, final LivingEntity user) {
        return getSpell(stack) instanceof ChargeSpell ? 72000 : 0;
    }

    @Override
    public boolean releaseUsing(
        final ItemStack stack,
        final Level level,
        final LivingEntity entity,
        final int remainingTime
    ) {
        if (!(level instanceof ServerLevel) || !(entity instanceof ServerPlayer player)
            || !(getSpell(stack) instanceof ChargeSpell)) {
            return false;
        }
        boolean fired = FolioCastHandler.release(player);
        if (fired) {
            stack.consume(1, player);
        }
        return fired;
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
            builder.accept(Component.empty());
            builder.accept(Component.translatable(
                "tooltip.lamplight.spell.name",
                spell.name().copy().withStyle(ChatFormatting.BLUE)
            ).withStyle(ChatFormatting.GRAY));
            builder.accept(Component.translatable(
                "tooltip.lamplight.spell.type",
                spell.type().displayName().copy().withStyle(ChatFormatting.BLUE)
            ).withStyle(ChatFormatting.GRAY));
            builder.accept(Component.empty());
            builder.accept(spell.description().copy().withStyle(ChatFormatting.DARK_GRAY));
        } else {
            builder.accept(Component.translatable(itemStack.getItem().getDescriptionId() + ".blank").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
