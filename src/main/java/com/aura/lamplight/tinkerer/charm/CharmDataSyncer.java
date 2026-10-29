package com.aura.lamplight.tinkerer.charm;

import com.aura.lamplight.mixin.tinkerer.PlayerInventoryMenuAccessor;
import com.aura.lamplight.mixin.tinkerer.ServerPlayerInitMenuInvoker;
import com.aura.lamplight.tinkerer.Tinkerer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings ("all")
public final class CharmDataSyncer implements SimpleSynchronousResourceReloadListener {
	private CharmDataSyncer() {}

	private static volatile Set<Identifier> lastSyncedSlotIds = Set.of();

	private static final Map<UUID, String> LAYOUTS = new HashMap<>();

	@Nullable
	private static volatile MinecraftServer currentServer;

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			currentServer = null;
			lastSyncedSlotIds = Set.of();
		});
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new CharmDataSyncer());
	}

	public static void acknowledgeJoinSync() {
		lastSyncedSlotIds = slotIds();
	}

	public static void forget(final UUID playerId) {
		LAYOUTS.remove(playerId);
	}

	public static void tickCharmSlots(final ServerPlayer player) {
		String signature = CharmDeckLayout
				.wells(player, CharmSlotAttributes.capacities(player), CharmSlotAttributes.acquired(player))
				.stream()
				.map(well -> well.id().toString())
				.collect(Collectors.joining(","));
		String previous = LAYOUTS.put(player.getUUID(), signature);
		if (signature.equals(previous)) {
			return;
		}
		ServerPlayNetworking.send(player, CharmSyncPayload.snapshot(player));
		rebuildInventoryMenu(player);
	}

	private static Set<Identifier> slotIds() {
		return CharmSlotTypes.all().stream()
				.map(CharmSlotTypes.CharmSlotType::id)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public void onResourceManagerReload(final ResourceManager manager) {
		Set<Identifier> fresh = slotIds();
		boolean slotsChanged = !fresh.equals(lastSyncedSlotIds);
		lastSyncedSlotIds = fresh;
		MinecraftServer server = currentServer;
		if (server == null) {
			return;
		}
		server.execute(() -> broadcast(server, slotsChanged));
	}

	private static void broadcast(final MinecraftServer server, final boolean slotsChanged) {
		if (server.getPlayerList().getPlayers().isEmpty()) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			CharmSlotAttributes.ensureBases(player);
			ServerPlayNetworking.send(player, CharmSyncPayload.snapshot(player));
			if (slotsChanged) {
				rebuildInventoryMenu(player);
			}
		}
	}

	private static void rebuildInventoryMenu(final ServerPlayer player) {
		AbstractContainerMenu previous = player.containerMenu;
		boolean deckInUse = previous == player.inventoryMenu;
		InventoryMenu rebuilt = new InventoryMenu(player.getInventory(), true, player);
		((PlayerInventoryMenuAccessor) player).setInventoryMenu(rebuilt);
		if (deckInUse) {
			rebuilt.transferState(previous);
			rebuilt.setCarried(previous.getCarried());
			player.containerMenu = rebuilt;
		}
		((ServerPlayerInitMenuInvoker) player).lamplight$invokeInitMenu(rebuilt);
		rebuilt.broadcastFullState();
	}

	@Override
	public Identifier getFabricId() {
		return Tinkerer.id("charm_data_syncer");
	}

	@Override
	public Collection<Identifier> getFabricDependencies() {
		return List.of(Tinkerer.id("charm_materials"), Tinkerer.id("charm_templates"),
				Tinkerer.id("charm_slots"));
	}
}
