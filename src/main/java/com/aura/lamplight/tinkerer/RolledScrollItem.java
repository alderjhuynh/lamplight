package com.aura.lamplight.tinkerer;

import com.aura.lamplight.tinkerer.charm.CharmSlotAttributes;
import com.aura.lamplight.tinkerer.charm.CharmSlotKinds;
import com.aura.lamplight.tinkerer.charm.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

public class RolledScrollItem extends Item {

    public RolledScrollItem(final Item.Properties properties) {
        super(properties);
    }

    public static @Nullable String slotKind(final ItemStack stack) {
        String slug = stack.get(ModDataComponents.SLOT_GRANT);
        return slug != null && CharmSlotKinds.bySlug(slug) != null ? slug : null;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String slug = slotKind(stack);
        if (slug == null) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer server)) {
            return InteractionResult.SUCCESS;
        }
        if (!CharmSlotAttributes.grantSlot(server, slug)) {
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.2F);
        server.containerMenu.broadcastChanges();
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(
        final ItemStack stack,
        final TooltipContext context,
        final TooltipDisplay display,
        final Consumer<Component> builder,
        final TooltipFlag tooltipFlag
    ) {
        String slug = slotKind(stack);
        if (slug != null) {
            builder.accept(Component.translatable("tooltip.lamplight.rolled_scroll",
                    Component.translatable(CharmSlotKinds.nameKey(slug)).withStyle(ChatFormatting.BLUE))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            builder.accept(Component.translatable("tooltip.lamplight.rolled_scroll.blank")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
