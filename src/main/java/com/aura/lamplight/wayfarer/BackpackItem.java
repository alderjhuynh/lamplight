package com.aura.lamplight.wayfarer;

import java.util.OptionalInt;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class BackpackItem extends Item {

    public BackpackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            int slotIndex = hand == InteractionHand.OFF_HAND
                    ? Inventory.SLOT_OFFHAND
                    : player.getInventory().getSelectedSlot();
            openBackpack(player, slotIndex);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack backpack, ItemStack carried, Slot slot,
            ClickAction action, Player player, SlotAccess carriedAccess) {
        if (action != ClickAction.SECONDARY || !carried.isEmpty()) {
            return false;
        }
        if (player.containerMenu instanceof BackpackMenu open && open.isBackpack(backpack)) {
            return true;
        }
        int inventorySlot = slot.getContainerSlot();
        if (slot.container != player.getInventory()
                || (inventorySlot >= Inventory.INVENTORY_SIZE && inventorySlot != Inventory.SLOT_OFFHAND)
                || slot.container.getItem(inventorySlot) != backpack) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }
        openBackpack(player, inventorySlot);
        return true;
    }

    private static void openBackpack(Player player, int inventorySlot) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemStack backpack = serverPlayer.getInventory().getItem(inventorySlot);
        if (!(backpack.getItem() instanceof BackpackItem)) {
            return;
        }
        AbstractContainerMenu previous = serverPlayer.containerMenu;
        OptionalInt opened = serverPlayer.openMenu(new ExtendedMenuProvider<Integer>() {
            @Override
            public Integer getScreenOpeningData(ServerPlayer openingPlayer) {
                return inventorySlot;
            }

            @Override
            public Component getDisplayName() {
                return backpack.getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player openingPlayer) {
                return new BackpackMenu(containerId, inventory, inventorySlot);
            }
        });
        if (previous != serverPlayer.containerMenu) {
            previous.resumeRemoteUpdates();
        }
        if (opened.isPresent()) {
            serverPlayer.level().playSound(null, serverPlayer,
                    WayfarerSounds.BACKPACK_OPEN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.lamplight.leather_backpack")
                .withStyle(ChatFormatting.GRAY));
    }
}
