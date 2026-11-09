package de.highcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.highcraft.drug.DrugType;
import de.highcraft.item.DrugTestItem;
import de.highcraft.registry.ModAttachments;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Collection;

/**
 * /highcraft status [player]           - show addiction/tolerance/intoxication
 * /highcraft reset [player]            - reset everything (OP)
 * /highcraft addiction player type value - set addiction (OP, for testing)
 */
public final class HighCraftCommand {
    private HighCraftCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("highcraft")
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            DrugTestItem.sendReport(p, p);
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer receiver) DrugTestItem.sendReport(target, receiver);
                                    return 1;
                                })))
                .then(Commands.literal("reset")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> reset(ctx.getSource(), java.util.List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("addiction")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(DrugType.values()).map(DrugType::id), b))
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
                                                .executes(ctx -> {
                                                    DrugType t = DrugType.byId(StringArgumentType.getString(ctx, "type"));
                                                    if (t == null) {
                                                        ctx.getSource().sendFailure(Component.translatable("command.highcraft.unknown_type"));
                                                        return 0;
                                                    }
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                                    target.getData(ModAttachments.DRUG_DATA).setAddiction(t, FloatArgumentType.getFloat(ctx, "value"));
                                                    ctx.getSource().sendSuccess(() -> Component.translatable("command.highcraft.addiction_set",
                                                            target.getDisplayName(), t.displayName()), true);
                                                    return 1;
                                                }))))));
    }

    private static int reset(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer p : targets) p.getData(ModAttachments.DRUG_DATA).reset();
        source.sendSuccess(() -> Component.translatable("command.highcraft.reset", targets.size()), true);
        return targets.size();
    }
}
