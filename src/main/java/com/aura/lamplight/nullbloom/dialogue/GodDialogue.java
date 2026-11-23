package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.Nullbloom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class GodDialogue {
	public enum Result {
		ACCEPTED,
		REFUSED,
		COMPLETED,
		CANCELLED
	}

	private enum Phase {
		INTRO,
		BRANCH
	}

	private static final class Session {
		final int id;
		final ServerPlayer player;
		final ResourceKey<Level> dimension;
		final GodScript script;
		final BiConsumer<ServerPlayer, Result> onEnd;
		Phase phase = Phase.INTRO;
		boolean answeredYes;
		int idleTicks;

		Session(int id, ServerPlayer player, GodScript script, BiConsumer<ServerPlayer, Result> onEnd) {
			this.id = id;
			this.player = player;
			this.dimension = player.level().dimension();
			this.script = script;
			this.onEnd = onEnd;
		}
	}

	private static final int IDLE_TIMEOUT_TICKS = 20 * 60 * 5;

	private static final Map<UUID, Session> SESSIONS = new HashMap<>();
	private static int nextSessionId = 1;

	private GodDialogue() {
	}

	public static void initialize() {
		PayloadTypeRegistry.clientboundPlay().register(GodDialoguePayload.TYPE, GodDialoguePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(GodDialogueCancelPayload.TYPE, GodDialogueCancelPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(GodDialogueResponsePayload.TYPE, GodDialogueResponsePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(GodDialogueResponsePayload.TYPE,
			(payload, context) -> {
				ServerPlayer player = context.player();
				context.server().execute(() -> handleResponse(player, payload));
			});
		ServerTickEvents.END_SERVER_TICK.register(GodDialogue::tick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GodCommand.register(dispatcher));
	}

	public static void start(ServerPlayer player, GodScript script, BiConsumer<ServerPlayer, Result> onEnd) {
		Session previous = SESSIONS.remove(player.getUUID());
		if (previous != null) {
			end(previous, Result.CANCELLED, true);
		}
		Session session = new Session(nextSessionId++, player, script, onEnd);
		List<GodLine> lines = GodContext.resolve(player, script.intro());
		if (lines.isEmpty()) {
			Nullbloom.LOGGER.warn("God dialogue for {} has no intro lines; ending immediately", player.getName().getString());
			end(session, script.askChoice() ? Result.CANCELLED : Result.COMPLETED, false);
			return;
		}
		SESSIONS.put(player.getUUID(), session);
		Nullbloom.LOGGER.info("Started God dialogue for {} (session {}): {} intro lines, askChoice={}",
			player.getName().getString(), session.id, lines.size(), script.askChoice());
		ServerPlayNetworking.send(player, new GodDialoguePayload(session.id, lines, script.askChoice()));
	}

	public static boolean isTalking(ServerPlayer player) {
		return SESSIONS.containsKey(player.getUUID());
	}

	public static void cancel(ServerPlayer player) {
		Session session = SESSIONS.remove(player.getUUID());
		if (session != null) {
			end(session, Result.CANCELLED, true);
		}
	}

	private static void handleResponse(ServerPlayer player, GodDialogueResponsePayload response) {
		Session session = SESSIONS.get(player.getUUID());
		if (session == null) {
			Nullbloom.LOGGER.warn("God response from {} ignored: no active session (sessionId={})",
				player.getName().getString(), response.sessionId());
			return;
		}
		if (session.id != response.sessionId()) {
			Nullbloom.LOGGER.warn("God response from {} ignored: session id mismatch (got {}, expected {})",
				player.getName().getString(), response.sessionId(), session.id);
			return;
		}
		if (!session.player.getUUID().equals(player.getUUID())) {
			Nullbloom.LOGGER.warn("God response from {} ignored: player mismatch", player.getName().getString());
			return;
		}
		Nullbloom.LOGGER.info("God response from {} (session {}): kind={}, phase={}",
			player.getName().getString(), session.id, response.kind(), session.phase);
		session.idleTicks = 0;
		boolean choice = response.kind() == GodDialogueResponsePayload.YES || response.kind() == GodDialogueResponsePayload.NO;

		if (session.phase == Phase.INTRO && choice && session.script.askChoice()) {
			session.answeredYes = response.kind() == GodDialogueResponsePayload.YES;
			Nullbloom.LOGGER.info("God {} answered {} (session {}); resolving {} branch",
				player.getName().getString(), session.answeredYes ? "YES" : "NO", session.id,
				session.answeredYes ? "onYes" : "onNo");
			List<GodLine> branch = GodContext.resolve(player, session.answeredYes ? session.script.onYes() : session.script.onNo());
			if (branch.isEmpty()) {
				Nullbloom.LOGGER.info("Branch empty; ending as {} (session {})", session.answeredYes ? "ACCEPTED" : "REFUSED", session.id);
				SESSIONS.remove(player.getUUID());
				end(session, session.answeredYes ? Result.ACCEPTED : Result.REFUSED, true);
				return;
			}
			session.phase = Phase.BRANCH;
			Nullbloom.LOGGER.info("Sending branch of {} lines (session {})", branch.size(), session.id);
			ServerPlayNetworking.send(player, new GodDialoguePayload(session.id, branch, false));
		} else if (response.kind() == GodDialogueResponsePayload.DONE
			&& (session.phase == Phase.BRANCH || !session.script.askChoice())) {
			SESSIONS.remove(player.getUUID());
			Result result = !session.script.askChoice() ? Result.COMPLETED
				: session.answeredYes ? Result.ACCEPTED : Result.REFUSED;
			Nullbloom.LOGGER.info("God dialogue DONE (session {}): result={}", session.id, result);
			end(session, result, false);
		} else {
			Nullbloom.LOGGER.warn("God response from {} ignored: kind={}, phase={}, askChoice={} (session {})",
				player.getName().getString(), response.kind(), session.phase, session.script.askChoice(), session.id);
		}
	}

	private static void tick(MinecraftServer server) {
		if (SESSIONS.isEmpty()) {
			return;
		}
		List<Session> dropped = new ArrayList<>();
		for (Session session : SESSIONS.values()) {
			ServerPlayer current = server.getPlayerList().getPlayer(session.player.getUUID());
			boolean gone = current == null || !current.getUUID().equals(session.player.getUUID())
				|| !session.player.isAlive()
				|| !session.player.level().dimension().equals(session.dimension)
				|| ++session.idleTicks > IDLE_TIMEOUT_TICKS;
			if (gone) {
				dropped.add(session);
			}
		}
		for (Session session : dropped) {
			Nullbloom.LOGGER.warn("Dropping God dialogue for {} (session {}): current={}, alive={}, sameDimension={}, idleTicks>timeout={}",
				session.player.getName().getString(), session.id,
				server.getPlayerList().getPlayer(session.player.getUUID()) == null ? "offline" : "online",
				session.player.isAlive(),
				session.player.level().dimension().equals(session.dimension),
				session.idleTicks > IDLE_TIMEOUT_TICKS);
			SESSIONS.remove(session.player.getUUID());
			end(session, Result.CANCELLED, true);
		}
	}

	private static void end(Session session, Result result, boolean notifyClient) {
		Nullbloom.LOGGER.info("Ending God dialogue for {} (session {}): result={}, notifyClient={}",
			session.player.getName().getString(), session.id, result, notifyClient);
		if (notifyClient) {
			try {
				ServerPlayNetworking.send(session.player, new GodDialogueCancelPayload(session.id));
			} catch (RuntimeException ignored) {
			}
		}
		try {
			session.onEnd.accept(session.player, result);
		} catch (RuntimeException e) {
			Nullbloom.LOGGER.error("God dialogue callback failed", e);
		}
	}
}
