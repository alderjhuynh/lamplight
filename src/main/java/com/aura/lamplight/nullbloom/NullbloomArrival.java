package com.aura.lamplight.nullbloom;

import com.aura.lamplight.nullbloom.dialogue.GodContext;
import com.aura.lamplight.nullbloom.dialogue.GodDialogue;
import com.aura.lamplight.nullbloom.dialogue.GodScripts;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;

public final class NullbloomArrival {
	public static final boolean ENABLED = true;
	private static final int GREETING_DELAY_TICKS = 60;
	private static final int HOME_DELAY_TICKS = 50;

	private static final Map<UUID, Boolean> WAS_INSIDE = new HashMap<>();
	private static final Map<UUID, Integer> AWAITING_GREETING = new HashMap<>();
	private static final Map<UUID, Integer> AWAITING_HOME = new HashMap<>();

	private NullbloomArrival() {
	}

	public static void initialize() {
		if (!ENABLED) {
			return;
		}
		ServerTickEvents.END_SERVER_TICK.register(NullbloomArrival::tick);
	}

	private static void tick(MinecraftServer server) {
		detectArrivals(server);
		countDown(server, AWAITING_GREETING, NullbloomArrival::greet);
		countDown(server, AWAITING_HOME, NullbloomArrival::sendHome);
		ensureSoulboundItems(server);
	}

	private static void detectArrivals(MinecraftServer server) {
		WAS_INSIDE.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean inside = NullbloomDimension.is(player);
			boolean was = WAS_INSIDE.getOrDefault(player.getUUID(), false);
			if (inside && !was) {
				AWAITING_GREETING.put(player.getUUID(), GREETING_DELAY_TICKS);
			} else if (!inside) {
				AWAITING_GREETING.remove(player.getUUID());
			}
			WAS_INSIDE.put(player.getUUID(), inside);
		}
	}

	private static void countDown(MinecraftServer server, Map<UUID, Integer> timers, java.util.function.Consumer<ServerPlayer> action) {
		Iterator<Map.Entry<UUID, Integer>> it = timers.entrySet().iterator();
		java.util.List<ServerPlayer> due = new java.util.ArrayList<>();
		while (it.hasNext()) {
			Map.Entry<UUID, Integer> entry = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
			if (player == null) {
				it.remove();
			} else if (entry.getValue() <= 1) {
				it.remove();
				due.add(player);
			} else {
				entry.setValue(entry.getValue() - 1);
			}
		}
		due.forEach(action);
	}

	private static void greet(ServerPlayer player) {
		if (!NullbloomDimension.is(player) || !player.isAlive() || GodDialogue.isTalking(player)) {
			Nullbloom.LOGGER.info("Skipping greet for {}: inNullbloom={}, alive={}, talking={}",
				player.getName().getString(), NullbloomDimension.is(player), player.isAlive(), GodDialogue.isTalking(player));
			return;
		}
		MinecraftServer server = player.level().getServer();
		GodMemory memory = GodMemory.get(server);
		GodMemory.Entry entry = memory.get(player.getUUID());
		boolean accepted = GodsBargain.hasAccepted(player);
		int deaths = GodContext.deaths(player);

		if (!entry.met() && !accepted) {
			Nullbloom.LOGGER.info("Greeting {} with first meeting", player.getName().getString());
			GodDialogue.start(player, GodScripts.FIRST_MEETING, (p, result) -> {
				Nullbloom.LOGGER.info("God dialogue with {} ended: {}", p.getName().getString(), result);
				switch (result) {
					case ACCEPTED -> {
						GodsBargain.strike(p);
						rememberFirstMeeting(server, p);
						AWAITING_HOME.put(p.getUUID(), HOME_DELAY_TICKS);
					}
					case REFUSED, COMPLETED -> {
						rememberFirstMeeting(server, p);
						AWAITING_HOME.put(p.getUUID(), HOME_DELAY_TICKS);
					}
					case CANCELLED -> {
					}
				}
			});
			return;
		}

		int returnVisit = entry.returnVisits() + 1;
		boolean diedSince = entry.deathsAtLastVisit() >= 0 && deaths > entry.deathsAtLastVisit();
		memory.update(player.getUUID(), e -> new GodMemory.Entry(true, returnVisit, deaths, e.contacts()));
		Nullbloom.LOGGER.info("Greeting {} with return visit {} (accepted={}, diedSince={})",
			player.getName().getString(), returnVisit, accepted, diedSince);
		GodDialogue.start(player, GodScripts.returnVisit(player, returnVisit, diedSince, accepted), (p, result) -> {
			Nullbloom.LOGGER.info("God dialogue with {} ended: {}", p.getName().getString(), result);
			switch (result) {
				case ACCEPTED -> {
					GodsBargain.strike(p);
					AWAITING_HOME.put(p.getUUID(), HOME_DELAY_TICKS);
				}
				case REFUSED, COMPLETED -> AWAITING_HOME.put(p.getUUID(), HOME_DELAY_TICKS);
				case CANCELLED -> {
				}
			}
		});
	}

	private static void rememberFirstMeeting(MinecraftServer server, ServerPlayer player) {
		int deaths = GodContext.deaths(player);
		GodMemory.get(server).update(player.getUUID(),
			e -> new GodMemory.Entry(true, e.returnVisits(), deaths, e.contacts()));
	}

	public static void sendHome(ServerPlayer player) {
		if (!NullbloomDimension.is(player) || !player.isAlive()) {
			Nullbloom.LOGGER.info("Skipping sendHome for {}: inNullbloom={}, alive={}",
				player.getName().getString(), NullbloomDimension.is(player), player.isAlive());
			return;
		}
		Nullbloom.LOGGER.info("Sending {} home to respawn", player.getName().getString());
		TeleportTransition home = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
		player.teleport(home);
	}

	private static void ensureSoulboundItems(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!GodsBargain.hasAccepted(player)) {
				continue;
			}
			// Re-apply the heart-taken modifier if it was lost (e.g. on respawn).
			AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
			Identifier heartId = Nullbloom.id("god_heart_taken");
			if (maxHealth != null && !maxHealth.hasModifier(heartId)) {
				maxHealth.addPermanentModifier(new AttributeModifier(heartId, -2.0, AttributeModifier.Operation.ADD_VALUE));
				player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
			}
			boolean has = false;
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				if (player.getInventory().getItem(i).getItem() == NullbloomBlocks.NULLBLOOM_ITEM) {
					has = true;
					break;
				}
			}
			if (!has) {
				ItemStack flower = new ItemStack(NullbloomBlocks.NULLBLOOM_ITEM);
				player.getInventory().add(flower);
			}
		}
	}

}
