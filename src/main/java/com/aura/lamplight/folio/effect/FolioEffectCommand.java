package com.aura.lamplight.folio.effect;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

final class FolioEffectCommand {
    private FolioEffectCommand() {}

    static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var targets = Commands.argument("targets", EntityArgument.entities());
            for (SchoolStatus status : SchoolStatus.values()) {
                LiteralArgumentBuilder<CommandSourceStack> effect =
                    Commands.literal(status.name().toLowerCase(Locale.ROOT))
                        .executes(context -> run(context, status, 1))
                        .then(Commands.argument("stacks", IntegerArgumentType.integer(1, StackRules.MAX_STACKS))
                            .executes(context -> run(context, status, IntegerArgumentType.getInteger(context, "stacks"))));
                targets.then(effect);
            }
            dispatcher.register(Commands.literal("folioeffect")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(targets));
        });
    }

    private static int run(
        final CommandContext<CommandSourceStack> context,
        final SchoolStatus status,
        final int stacks
    ) throws CommandSyntaxException {
        LivingEntity source = context.getSource().getEntity() instanceof LivingEntity living ? living : null;
        int applied = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity target && SchoolEffects.grant(status, target, source, stacks)) {
                applied++;
            }
        }
        int count = applied;
        context.getSource().sendSuccess(
            () -> Component.literal("Applied " + stacks + " " + status.name().toLowerCase(Locale.ROOT)
                + " to " + count + " target(s)"), true);
        return count;
    }
}
