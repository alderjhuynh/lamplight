package com.aura.lamplight.tinkerer.client;

import com.aura.lamplight.mixin.tinkerer.PlayerInventoryMenuAccessor;
import com.aura.lamplight.tinkerer.JewelingPatternAppliedPayload;
import com.aura.lamplight.tinkerer.JewelingTableMenu;
import com.aura.lamplight.tinkerer.ModMenus;
import com.aura.lamplight.tinkerer.charm.CharmDeckLayout;
import com.aura.lamplight.tinkerer.charm.CharmMaterials;
import com.aura.lamplight.tinkerer.charm.CharmSyncPayload;
import com.aura.lamplight.tinkerer.charm.CharmSyncRequestPayload;
import com.aura.lamplight.tinkerer.charm.CharmTemplates;
import com.aura.lamplight.tinkerer.charm.CharmSlotTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;
import java.util.Objects;

public class TinkererClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.JEWELING_TABLE, JewelingTableScreen::new);
		ModelLoadingPlugin.register(new CharmItemModels());
		ClientPlayNetworking.registerGlobalReceiver(CharmSyncPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				List<Identifier> wellsBefore = CharmDeckLayout.clientWellIds();
				CharmMaterials.applySnapshot(payload.materials());
				CharmTemplates.applySnapshot(payload.templates());
				CharmSlotTypes.applySnapshot(payload.slots());
				CharmDeckLayout.setClientCapacities(payload.capacities());
				CharmDeckLayout.setClientAcquired(payload.acquired());
				CharmDeckLayout.setClientWells(payload.wells());
				if (!payload.wells().equals(wellsBefore)) {
					rebuildInventoryMenu();
					ClientPlayNetworking.send(new CharmSyncRequestPayload());
				}
				if (client.gui.screen() instanceof JewelingTableScreen jeweler) {
					jeweler.refreshAvailable();
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(JewelingPatternAppliedPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Minecraft client = context.client();
				if (client.player == null
						|| !(client.player.containerMenu instanceof JewelingTableMenu menu)
						|| menu.containerId != payload.containerId()) {
					return;
				}
				Identifier templateId = payload.templateId().orElse(null);
				if (!Objects.equals(menu.patternId(), templateId)) {
					menu.setPattern(templateId);
				}
				if (client.gui.screen() instanceof JewelingTableScreen jeweler) {
					jeweler.applyAuthoritative(payload.containerId(), payload.templateId());
				}
			});
		});
		CharmTooltips.initialize();
	}

	private static void rebuildInventoryMenu() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}
		InventoryMenu oldMenu = client.player.inventoryMenu;
		Inventory inventory = client.player.getInventory();
		InventoryMenu rebuilt = new InventoryMenu(inventory, true, client.player);
		PlayerInventoryMenuAccessor accessor = (PlayerInventoryMenuAccessor) client.player;
		AbstractContainerMenu current = client.player.containerMenu;
		accessor.setInventoryMenu(rebuilt);
		if (current == null || current == oldMenu) {
			client.player.containerMenu = rebuilt;
			if (current != null && client.gui.screen() != null) {
				client.gui.setScreen(null);
			}
		}
	}
}
