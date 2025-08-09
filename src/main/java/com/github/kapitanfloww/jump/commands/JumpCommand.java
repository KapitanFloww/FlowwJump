package com.github.kapitanfloww.jump.commands;

import com.github.kapitanfloww.jump.holograms.JumpHologramManager;
import com.github.kapitanfloww.jump.holograms.events.UpdateJumpHologramEvent;
import com.github.kapitanfloww.jump.model.JumpLocation;
import com.github.kapitanfloww.jump.model.JumpLocationType;
import com.github.kapitanfloww.jump.score.ScoreboardService;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.github.kapitanfloww.jump.service.JumpService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import lombok.extern.java.Log;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.PluginManager;

import java.util.function.Predicate;

/**
 * Command for /jump
 */
@Log
public class JumpCommand {

    private static final String USE_PERMISSION = "floww.jump.use";
    private static final String MAINTAIN_PERMISSION = "floww.jump.maintain";

    private static final Predicate<CommandSourceStack> HAS_USE_PERMISSION = commandSourceStack -> commandSourceStack.getSender().hasPermission(USE_PERMISSION);
    private static final Predicate<CommandSourceStack> HAS_MAINTAIN_PERMISSION = commandSourceStack -> commandSourceStack.getSender().hasPermission(MAINTAIN_PERMISSION);

    private static final Predicate<CommandSourceStack> IS_PLAYER = source -> source.getSender() instanceof Player;

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(JumpService jumpService, JumpLocationService jumpLocationService, JumpPlayerService jumpPlayerService, PluginManager pluginManager, ScoreboardService scoreboardService) {
        return Commands.literal("jump")
                .then(Commands.literal("help")
                        .requires(HAS_USE_PERMISSION)
                        .executes(JumpCommand::runHelpCommand)
                )

                .then(Commands.literal("cancel")
                        .requires(IS_PLAYER)
                        .requires(HAS_USE_PERMISSION)
                        .executes(ctx -> runCancelJumpCommand(ctx, jumpPlayerService, jumpLocationService))
                )

                .then(Commands.literal("reset-score")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(HAS_MAINTAIN_PERMISSION)
                                .suggests(getJumpNameSuggestions(jumpService))
                                .executes(ctx -> runResetScoreCommand(ctx, jumpService, pluginManager, scoreboardService))
                        )
                )

                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(IS_PLAYER)
                                .requires(HAS_MAINTAIN_PERMISSION)
                                .suggests(getJumpNameSuggestions(jumpService))
                                .executes(ctx -> runCreateJumpCommand(ctx, jumpService, jumpLocationService))
                        )
                )

                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(HAS_MAINTAIN_PERMISSION)
                                .suggests(getJumpNameSuggestions(jumpService))
                                .executes(ctx -> runInfoJumpCommand(ctx, jumpService, jumpLocationService))
                        )
                )

                .then(Commands.literal("list")
                        .requires(HAS_USE_PERMISSION)
                        .executes(ctx -> runListJumpCommand(ctx, jumpService, jumpLocationService))
                )

                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(IS_PLAYER)
                                .requires(HAS_MAINTAIN_PERMISSION)
                                .suggests(getJumpNameSuggestions(jumpService))
                                .executes(ctx -> runDeleteJumpCommand(ctx, jumpService, jumpLocationService))
                        )
                )

                .then(Commands.literal("holograms")
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    final var sender = ctx.getSource().getSender();
                                    try {
                                        final var hologramManager = JumpHologramManager.getJumpHologramManager(jumpLocationService);
                                        final var jumps = jumpService.getAll();
                                        jumps.forEach(it -> {
                                            if (hologramManager.getHologram(it).isEmpty()) {
                                                hologramManager.createHologram(it);
                                                sender.sendMessage(Component.text("Created Hologram for jump " + it.getName(), NamedTextColor.GREEN));
                                            }
                                        });
                                        sender.sendMessage(Component.text("Hologram reload complete", NamedTextColor.GREEN));
                                    } catch (IllegalArgumentException ex) {
                                        sender.sendMessage(Component.text(ex.getLocalizedMessage(), NamedTextColor.RED));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )

                .then(Commands.literal("set")
                        .then(Commands.literal("start")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(IS_PLAYER)
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runSetStartCommand(ctx, jumpService, jumpLocationService))
                                ))
                        .then(Commands.literal("finish")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(IS_PLAYER)
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runSetFinishCommand(ctx, jumpService))
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(IS_PLAYER)
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runSetResetCommand(ctx, jumpService))
                                )
                        )
                )

                .then(Commands.literal("checkpoints")
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(IS_PLAYER)
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runCheckpointAddCommand(ctx, jumpService))
                                )
                        )
                        .then(Commands.literal("list")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runCheckpointListCommand(ctx, jumpService, jumpLocationService))
                                )
                        )
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .requires(IS_PLAYER)
                                        .requires(HAS_MAINTAIN_PERMISSION)
                                        .suggests(getJumpNameSuggestions(jumpService))
                                        .executes(ctx -> runCheckpointDeleteCommand(ctx, jumpService))
                                )
                        )
                );
    }

    private static SuggestionProvider<CommandSourceStack> getJumpNameSuggestions(JumpService jumpService) {
        return (context, builder) -> {
            final var jumpNames = jumpService.getJumpNames();
            jumpNames.forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    // /jump help
    private static int runHelpCommand(CommandContext<CommandSourceStack> ctx) {
        final var sender = ctx.getSource().getSender();
        sender.sendMessage(Component.text("--- Jump Help ---", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/jump cancel", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Cancel your current current jump", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump create <name>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Creates a new jump with the given name. With start location on target block", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump list", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Lists all jumps", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump info <name>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Prints information about the jump", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump delete <name>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Removes jump", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump set <start|finish|reset> <name>", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Sets the start, finish or reset position of a jump", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/jump checkpoints <add|list|delete> <name> - Adds, lists or removes checkpoints from a jump", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Adds, lists or removes checkpoints from a jump", NamedTextColor.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    // /jump cancel
    private static int runCancelJumpCommand(CommandContext<CommandSourceStack> ctx, JumpPlayerService jumpPlayerService, JumpLocationService jumpLocationService) {
        final var player = (Player) ctx.getSource().getSender();

        // Teleport player back to reset
        final var jump = jumpPlayerService.getCurrentJumpFor(player);
        if (jump != null) {
            player.teleport(jumpLocationService.toLocation(jump.getReset(), false), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        // Unregister player
        jumpPlayerService.unregisterPlayer(player);

        player.sendMessage(Component.text("Cancelled current jump", NamedTextColor.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    private static int runResetScoreCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, PluginManager pluginManager, ScoreboardService scoreboardService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var jump = jumpService.getJump(jumpName);

        // Remove the score
        scoreboardService.resetScore(jump.getId());

        // Update the hologram
        pluginManager.callEvent(new UpdateJumpHologramEvent(jump, JumpHologramManager.DEFAULT_TEXT_FN.apply(jump)));

        ctx.getSource().getSender().sendMessage(Component.text("Successfully reset score for jump ", NamedTextColor.GREEN).append(Component.text(jumpName, NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    // /jump create <name>
    private static int runCreateJumpCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);

        if (isInvalidMaterial(targetBlock)) {
            throw new SimpleCommandExceptionType(new LiteralMessage("You must be looking at a button or pressure-plate to execute this command")).create();
        }

        final var jump = jumpService.createJump(jumpName, targetBlock);
        player.sendMessage(Component.text("Created jump \"%s\"".formatted(jumpName), NamedTextColor.YELLOW));

        // Create Hologram
        try {
            JumpHologramManager.getJumpHologramManager(jumpLocationService).createHologram(jump);
            player.sendMessage(Component.text("Created Hologram for jump " + jump.getName(), NamedTextColor.GREEN));
        } catch (IllegalArgumentException ex) {
            log.warning(ex.getLocalizedMessage());
        }

        return Command.SINGLE_SUCCESS;
    }

    // /jump info
    private static int runInfoJumpCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) throws CommandSyntaxException {
        final var sender = ctx.getSource().getSender();
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var jump = jumpService.getJump(jumpName);
        sender.sendMessage(Component.text("Info about jump \"%s\"".formatted(jumpName), NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Start: ", NamedTextColor.GRAY).append(jump.getStart() != null ? jump.getStart().toComponent(getTeleportCallback(jumpLocationService, jump.getStart(), sender)) : Component.text("-- Start not set --")));
        sender.sendMessage(Component.text("Finish: ", NamedTextColor.GRAY).append(jump.getFinish() != null ? jump.getFinish().toComponent(getTeleportCallback(jumpLocationService, jump.getFinish(), sender)) : Component.text("-- Finish not set --")));
        sender.sendMessage(Component.text("Reset: ", NamedTextColor.GRAY).append(jump.getReset() != null ? jump.getReset().toComponent(getTeleportCallback(jumpLocationService, jump.getReset(), sender)) : Component.text("-- Reset not set --")));
        sender.sendMessage(Component.text("Checkpoints:", NamedTextColor.YELLOW));
        jump.getCheckpoints().stream().map(it -> it.toComponent(getTeleportCallback(jumpLocationService, it, sender))).forEach(sender::sendMessage);
        return Command.SINGLE_SUCCESS;
    }

    // /jump list
    private static int runListJumpCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) {
        final var sender = ctx.getSource().getSender();
        final var jumps = jumpService.getAll();
        sender.sendMessage(Component.text("The following jumps are known: ", NamedTextColor.YELLOW));
        jumps.forEach(it -> sender.sendMessage(
                Component.text(it.getName(), NamedTextColor.GOLD).append(
                        Component.text(" starting at ", NamedTextColor.YELLOW).append(
                                it.getStart().toComponent(getTeleportCallback(jumpLocationService, it.getStart(), sender))
                        )
                )
        ));
        return Command.SINGLE_SUCCESS;
    }

    // /jump delete <name>
    private static int runDeleteJumpCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var jump = jumpService.deleteJump(jumpName);
        final var sender = ctx.getSource().getSender();
        sender.sendMessage(Component
                .text("Deleted jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));

        // Remove hologram
        try {
            JumpHologramManager.getJumpHologramManager(jumpLocationService).removeHologram(jump);
        } catch (IllegalArgumentException ex) {
            log.warning(ex.getLocalizedMessage());
        }

        return Command.SINGLE_SUCCESS;
    }

    // /jump set start <name>
    private static int runSetStartCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);

        if (isInvalidMaterial(targetBlock)) {
            player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        final var jump = jumpService.addLocationToJump(jumpName, JumpLocationType.START, targetBlock);
        player.sendMessage(Component
                .text("Set start-location for jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));

        // Move Hologram
        try {
            JumpHologramManager.getJumpHologramManager(jumpLocationService).moveHologram(jump);
        } catch (IllegalArgumentException ex) {
            log.warning(ex.getLocalizedMessage());
        }

        return Command.SINGLE_SUCCESS;
    }

    // /jump set finish <name>
    private static int runSetFinishCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);

        if (isInvalidMaterial(targetBlock)) {
            player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        jumpService.addLocationToJump(jumpName, JumpLocationType.FINISH, targetBlock);
        player.sendMessage(Component
                .text("Set finish-location for jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    // /jump set reset <name>
    private static int runSetResetCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);
        jumpService.addLocationToJump(jumpName, JumpLocationType.RESET, targetBlock);
        player.sendMessage(Component
                .text("Set reset-location for jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }


    // /jump checkpoints add <name>
    private static int runCheckpointAddCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);

        if (isInvalidMaterial(targetBlock)) {
            player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        jumpService.addLocationToJump(jumpName, JumpLocationType.CHECKPOINT, targetBlock);
        player.sendMessage(Component
                .text("Added checkpoint to jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }


    // /jump checkpoints add <name>
    private static int runCheckpointListCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService, JumpLocationService jumpLocationService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var sender = ctx.getSource().getSender();
        final var checkPoints = jumpService.getCheckpointsForJump(jumpName);
        sender.sendMessage(Component
                .text("Known checkpoints for jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD))
                .append(Component.text(":", NamedTextColor.YELLOW))
        );
        checkPoints.forEach(it -> sender.sendMessage(it.toComponent(getTeleportCallback(jumpLocationService, it, sender))));
        return Command.SINGLE_SUCCESS;
    }

    // /jump checkpoints delete
    private static int runCheckpointDeleteCommand(CommandContext<CommandSourceStack> ctx, JumpService jumpService) throws CommandSyntaxException {
        final var jumpName = StringArgumentType.getString(ctx, "name");
        final var player = (Player) ctx.getSource().getSender();
        final var targetBlock = player.getTargetBlockExact(10);

        if (isInvalidMaterial(targetBlock)) {
            player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        jumpService.removeCheckpointForJump(jumpName, targetBlock);
        player.sendMessage(Component
                .text("Deleted checkpoint from jump ", NamedTextColor.YELLOW)
                .append(Component.text(jumpName, NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }

    private static ClickCallback<Audience> getTeleportCallback(JumpLocationService jumpLocationService, JumpLocation location, CommandSender sender) {
        return audience -> {
            if (sender instanceof Player player) {
                player.teleport(jumpLocationService.toLocation(location, false));
            }
        };
    }

    private static boolean isInvalidMaterial(Block targetBlock) {
        // Verify target block is not null
        if (targetBlock == null) {
            return true;
        }
        // Verify target block is either pressure plate or button
        return !Tag.BUTTONS.isTagged(targetBlock.getType()) && !Tag.PRESSURE_PLATES.isTagged(targetBlock.getType());
    }
}
