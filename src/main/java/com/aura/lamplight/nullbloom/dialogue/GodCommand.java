package com.aura.lamplight.nullbloom.dialogue;

import com.aura.lamplight.nullbloom.Nullbloom;
import com.mojang.brigadier.CommandDispatcher;
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
			.then(Commands.literal("cancel").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				GodDialogue.cancel(player);
				context.getSource().sendSuccess(() -> Component.literal("Cancelled."), false);
				return 1;
			})));
	}
}
