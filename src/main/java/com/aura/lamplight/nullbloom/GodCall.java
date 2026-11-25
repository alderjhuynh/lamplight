package com.aura.lamplight.nullbloom;

import com.aura.lamplight.nullbloom.dialogue.GodDialogue;
import com.aura.lamplight.nullbloom.dialogue.GodScript;
import com.aura.lamplight.nullbloom.dialogue.GodScripts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

@SuppressWarnings ("all")
public final class GodCall {
	public enum Outcome {
		STARTED,
		BRIEF_NO_DEAL,
		BRIEF_TOO_SOON,
		BUSY,
		WRONG_DIMENSION,
		DEAD
	}

	private enum Stage {
		PREROLL,
		TALKING,
		OUTRO
	}

	private static final class Call {
		final boolean full;
		final GodScript script;
		Stage stage = Stage.PREROLL;
		int ticksLeft;

		Call(boolean full, GodScript script, int ticksLeft) {
			this.full = full;
			this.script = script;
			this.ticksLeft = ticksLeft;
		}
	}

	private static final Map<UUID, Call> CALLS = new HashMap<>();
	private static final Map<UUID, Long> LAST_CALL_END = new HashMap<>();

	private GodCall() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(GodCallPayload.TYPE, GodCallPayload.CODEC);
		ServerTickEvents.END_SERVER_TICK.register(GodCall::tick);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> drop(handler.getPlayer().getUUID()));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			CALLS.clear();
			LAST_CALL_END.clear();
			GodAudience.clear();
		});
	}

	public static boolean isInCall(ServerPlayer player) {
		return CALLS.containsKey(player.getUUID());
	}

	public static Outcome call(ServerPlayer player) {
		if (!player.isAlive()) {
			return Outcome.DEAD;
		}
		if (player.level().dimension() != Level.OVERWORLD) {
			return Outcome.WRONG_DIMENSION;
		}
		UUID id = player.getUUID();
		if (CALLS.containsKey(id) || GodDialogue.isTalking(player)) {
			return Outcome.BUSY;
		}
		MinecraftServer server = player.level().getServer();

		if (!GodsBargain.hasAccepted(player)) {
			begin(player, false, GodScripts.NO_DEAL);
			return Outcome.BRIEF_NO_DEAL;
		}
		Long lastEnd = LAST_CALL_END.get(id);
		if (lastEnd != null && server.overworld().getGameTime() - lastEnd < GodCallTiming.CALL_COOLDOWN_TICKS) {
			begin(player, false, GodScripts.TOO_SOON);
			return Outcome.BRIEF_TOO_SOON;
		}

		GodMemory memory = GodMemory.get(server);
		int contact = memory.get(id).contacts() + 1;
		memory.update(id, entry -> new GodMemory.Entry(entry.met(), entry.returnVisits(), entry.deathsAtLastVisit(), contact));
		begin(player, true, GodScripts.contact(contact));
		return Outcome.STARTED;
	}

	private static void begin(ServerPlayer player, boolean full, GodScript script) {
		int preroll = full ? GodCallTiming.FULL_PREROLL_TICKS : GodCallTiming.BRIEF_PREROLL_TICKS;
		CALLS.put(player.getUUID(), new Call(full, script, preroll));
		GodAudience.enter(player);
		ServerPlayNetworking.send(player, new GodCallPayload(full ? GodCallPayload.START_FULL : GodCallPayload.START_BRIEF));
		Nullbloom.LOGGER.info("{} is calling God ({}), preroll {} ticks", player.getName().getString(),
			full ? "full" : "brief", preroll);
	}

	private static void tick(MinecraftServer server) {
		if (CALLS.isEmpty()) {
			return;
		}
		for (UUID id : new ArrayList<>(CALLS.keySet())) {
			Call call = CALLS.get(id);
			if (call == null) {
				continue;
			}
			ServerPlayer player = server.getPlayerList().getPlayer(id);
			if (player == null) {
				drop(id);
				continue;
			}
			if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD) {
				CALLS.remove(id);
				abort(player, call);
				continue;
			}
			if (call.stage == Stage.TALKING) {
				continue;
			}
			if (--call.ticksLeft > 0) {
				continue;
			}
			if (call.stage == Stage.PREROLL) {
				call.stage = Stage.TALKING;
				GodDialogue.start(player, call.script, GodCall::onDialogueEnd);
			} else {
				CALLS.remove(id);
				GodAudience.leave(player);
				LAST_CALL_END.put(id, server.overworld().getGameTime());
			}
		}
	}

	private static void onDialogueEnd(ServerPlayer player, GodDialogue.Result result) {
		Call call = CALLS.get(player.getUUID());
		if (call == null) {
			return;
		}
		if (result == GodDialogue.Result.CANCELLED) {
			CALLS.remove(player.getUUID());
			abort(player, call);
			return;
		}
		call.stage = Stage.OUTRO;
		call.ticksLeft = GodCallTiming.OUTRO_TICKS;
		ServerPlayNetworking.send(player, new GodCallPayload(GodCallPayload.END));
	}

	private static void abort(ServerPlayer player, Call call) {
		if (call.stage == Stage.TALKING && GodDialogue.isTalking(player)) {
			GodDialogue.cancel(player);
		}
		GodAudience.leave(player);
		try {
			ServerPlayNetworking.send(player, new GodCallPayload(GodCallPayload.ABORT));
		} catch (RuntimeException ignored) {
		}
		LAST_CALL_END.put(player.getUUID(), player.level().getServer().overworld().getGameTime());
	}

	private static void drop(UUID id) {
		CALLS.remove(id);
		GodAudience.forget(id);
	}
}
