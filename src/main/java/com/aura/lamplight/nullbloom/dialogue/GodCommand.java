package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.GodCall;
import com.aura.lamplight.nullbloom.GodMemory;
import com.aura.lamplight.nullbloom.Nullbloom;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

final class GodCommand {
	private GodCommand() {
	}

	static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("god")
			.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("talk").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				GodDialogue.start(player, GodScripts.FIRST_MEETING,
					(p, result) -> Nullbloom.LOGGER.info("God dialogue with {} ended: {}", p.getName().getString(), result));
				return 1;
			}))
			.then(Commands.literal("call").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				GodCall.Outcome outcome = GodCall.call(player);
				String message = switch (outcome) {
					case STARTED -> "God is listening.";
					case BRIEF_NO_DEAL -> "God is... not interested (no bargain struck).";
					case BRIEF_TOO_SOON -> "God is thinking (called too soon).";
					case BUSY -> "You are already in audience.";
					case WRONG_DIMENSION -> "God only answers in the overworld.";
					case DEAD -> "God does not speak to the dead.";
				};
				context.getSource().sendSuccess(() -> Component.literal(message), false);
				return outcome == GodCall.Outcome.STARTED ? 1 : 0;
			}))
			.then(Commands.literal("cancel").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				GodDialogue.cancel(player);
				context.getSource().sendSuccess(() -> Component.literal("Cancelled."), false);
				return 1;
			}))
			.then(Commands.literal("debug")
				.then(Commands.literal("returns").then(Commands.argument("count", IntegerArgumentType.integer(0))
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						int count = IntegerArgumentType.getInteger(context, "count");
						GodMemory.get(context.getSource().getServer()).update(player.getUUID(),
							e -> new GodMemory.Entry(true, count, e.deathsAtLastVisit(), e.contacts()));
						context.getSource().sendSuccess(() -> Component.literal(
							"Return visits set to " + count + " (the next arrival is return visit " + (count + 1) + ")."), false);
						return 1;
					})))
				.then(Commands.literal("contacts").then(Commands.argument("count", IntegerArgumentType.integer(0))
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						int count = IntegerArgumentType.getInteger(context, "count");
						GodMemory.get(context.getSource().getServer()).update(player.getUUID(),
							e -> new GodMemory.Entry(e.met(), e.returnVisits(), e.deathsAtLastVisit(), count));
						context.getSource().sendSuccess(() -> Component.literal(
							"Contacts set to " + count + " (the next call is contact " + (count + 1) + ")."), false);
						return 1;
					})))
				.then(Commands.literal("forget").executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					GodMemory.get(context.getSource().getServer()).forget(player.getUUID());
					context.getSource().sendSuccess(() -> Component.literal("God has forgotten you."), false);
					return 1;
				}))));
	}
}
