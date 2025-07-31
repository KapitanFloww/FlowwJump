package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.listeners.PlayerInteractEventListener;
import com.github.kapitanfloww.jump.listeners.PlayerFinishJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerReachesCheckpointJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerStartJumpListener;
import com.github.kapitanfloww.jump.model.JumpLocation;
import com.github.kapitanfloww.jump.model.JumpLocationType;
import com.github.kapitanfloww.jump.persistence.InMemoryJumpRepository;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.github.kapitanfloww.jump.service.JumpService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlowwJump extends JavaPlugin {

    private JumpService jumpService;
    private JumpLocationService jumpLocationService;
    private JumpPlayerService jumpPlayerService;

    @Override
    public void onEnable() {
        // Register plugin logic
        jumpService = new JumpService(new InMemoryJumpRepository());
        jumpLocationService = new JumpLocationService(jumpService, Bukkit::getWorld);
        jumpPlayerService = new JumpPlayerService();

        // Register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(getJumpCommandTree())); // /jump
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(getCheckpointTree())); // /checkpoint

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(getServer().getPluginManager(), jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerStartJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerFinishJumpListener(jumpPlayerService, jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerReachesCheckpointJumpListener(jumpPlayerService), this);
    }

    @Override
    public void onDisable() {

    }

    private LiteralCommandNode<CommandSourceStack> getCheckpointTree() {
        return Commands.literal("checkpoint")
                .requires(source -> source.getSender() instanceof Player)
                .executes(ctx -> {
                    final var player = (Player) ctx.getSource().getSender();
                    final var checkpoint = jumpPlayerService.getCheckpoint(player);

                    if (checkpoint == null) {
                        player.sendMessage(Component.text("You have not reached any checkpoints yet", NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS; // no checkpoints
                    }

                    player.teleport(jumpLocationService.toLocation(checkpoint));
                    player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
                    player.sendMessage(Component.text("You have been teleported back to your last checkpoint", NamedTextColor.GREEN));

                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    private LiteralCommandNode<CommandSourceStack> getJumpCommandTree() {
        // Setup commands
        var root = Commands.literal("jump")
                .executes(ctx -> {
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
                })
                .then(Commands.literal("cancel")
                        .requires(source -> source.getSender() instanceof Player)
                        .executes(ctx -> {
                            final var player = (Player) ctx.getSource().getSender();
                            jumpPlayerService.unregisterPlayer(player);
                            player.sendMessage(Component.text("Cancelled current jump", NamedTextColor.YELLOW));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(source -> source.getSender() instanceof Player)
                                .suggests((context, builder) -> {
                                    final var jumpNames = jumpService.getJumpNames();
                                    jumpNames.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlockExact(10);

                                    if (isInvalidMaterial(targetBlock)) {
                                        player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    jumpService.createJump(jumpName, targetBlock);
                                    player.sendMessage(Component.text("Created jump \"%s\"".formatted(jumpName), NamedTextColor.YELLOW));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    final var jumpNames = jumpService.getJumpNames();
                                    jumpNames.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    final var sender = ctx.getSource().getSender();
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var jump = jumpService.getJump(jumpName);
                                    sender.sendMessage(Component.text("Info about jump \"%s\"".formatted(jumpName), NamedTextColor.YELLOW));
                                    sender.sendMessage(Component.text("Start: ", NamedTextColor.GRAY).append(jump.getStart() != null ? jump.getStart().toTeleportComponent() : Component.text("-- Start not set --")));
                                    sender.sendMessage(Component.text("Finish: ", NamedTextColor.GRAY).append(jump.getFinish() != null ? jump.getFinish().toTeleportComponent() : Component.text("-- Finish not set --")));
                                    sender.sendMessage(Component.text("Reset: ", NamedTextColor.GRAY).append(jump.getReset() != null ? jump.getReset().toTeleportComponent() : Component.text("-- Reset not set --")));
                                    sender.sendMessage(Component.text("Checkpoints:", NamedTextColor.YELLOW));
                                    jump.getCheckpoints().stream().map(JumpLocation::toTeleportComponent).forEach(sender::sendMessage);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            final var sender = ctx.getSource().getSender();
                            final var jumps = jumpService.getAll();
                            sender.sendMessage(Component.text("The following jumps are known: ", NamedTextColor.YELLOW));
                            jumps.forEach(it -> sender.sendMessage(
                                    Component.text(it.getName(), NamedTextColor.GOLD).append(
                                            Component.text(" starting at ", NamedTextColor.YELLOW).append(
                                                    it.getStart().toTeleportComponent()
                                            )
                                    )
                            ));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument(
                                "name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    final var jumpNames = jumpService.getJumpNames();
                                    jumpNames.forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    jumpService.deleteJump(jumpName);
                                    final var sender = ctx.getSource().getSender();
                                    sender.sendMessage(Component
                                            .text("Deleted jump ", NamedTextColor.YELLOW)
                                            .append(Component.text(jumpName, NamedTextColor.GOLD)));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.literal("start")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .requires(source -> source.getSender() instanceof Player)
                                        .executes(ctx -> {
                                            final var jumpName = StringArgumentType.getString(ctx, "name");
                                            final var player = (Player) ctx.getSource().getSender();
                                            final var targetBlock = player.getTargetBlockExact(10);

                                            if (isInvalidMaterial(targetBlock)) {
                                                player.sendMessage(Component.text("You must be looking at a button or pressure-plate to execute this command", NamedTextColor.RED));
                                                return Command.SINGLE_SUCCESS;
                                            }

                                            jumpService.addLocationToJump(jumpName, JumpLocationType.START, targetBlock);
                                            player.sendMessage(Component
                                                    .text("Set start-location for jump ", NamedTextColor.YELLOW)
                                                    .append(Component.text(jumpName, NamedTextColor.GOLD)));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                ))
                        .then(Commands.literal("finish")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .requires(source -> source.getSender() instanceof Player)
                                        .executes(ctx -> {
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
                                        })
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .requires(source -> source.getSender() instanceof Player)
                                        .executes(ctx -> {
                                            final var jumpName = StringArgumentType.getString(ctx, "name");
                                            final var player = (Player) ctx.getSource().getSender();
                                            final var targetBlock = player.getTargetBlockExact(10);
                                            jumpService.addLocationToJump(jumpName, JumpLocationType.RESET, targetBlock);
                                            player.sendMessage(Component
                                                    .text("Set reset-location for jump ", NamedTextColor.YELLOW)
                                                    .append(Component.text(jumpName, NamedTextColor.GOLD)));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("checkpoints")
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .requires(source -> source.getSender() instanceof Player)
                                        .executes(ctx -> {
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
                                        })
                                )
                        )
                        .then(Commands.literal("list")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            final var jumpName = StringArgumentType.getString(ctx, "name");
                                            final var sender = ctx.getSource().getSender();
                                            final var checkPoints = jumpService.getCheckpointsForJump(jumpName);
                                            sender.sendMessage(Component
                                                    .text("Known checkpoints for jump ", NamedTextColor.YELLOW)
                                                    .append(Component.text(jumpName, NamedTextColor.GOLD))
                                                    .append(Component.text(":", NamedTextColor.YELLOW))
                                            );
                                            checkPoints.forEach(it -> sender.sendMessage(it.toTeleportComponent()));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            final var jumpNames = jumpService.getJumpNames();
                                            jumpNames.forEach(builder::suggest);
                                            return builder.buildFuture();
                                        })
                                        .requires(source -> source.getSender() instanceof Player)
                                        .executes(ctx -> {
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
                                        })
                                )
                        )
                )
                .build();
        return root;
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
