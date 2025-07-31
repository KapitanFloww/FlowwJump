package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.listeners.ButtonClickListener;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        var commandTree = getCommandTree();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(commandTree));

        // Register listeners
        getServer().getPluginManager().registerEvents(new ButtonClickListener(getServer().getPluginManager(), jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerStartJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerFinishJumpListener(jumpPlayerService, jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerReachesCheckpointJumpListener(), this);
    }

    @Override
    public void onDisable() {

    }

    private LiteralCommandNode<CommandSourceStack> getCommandTree() {
        // Setup commands
        var root = Commands.literal("jump")
                .executes(ctx -> {
                    final var sender = ctx.getSource().getSender();
                    sender.sendMessage(ChatColor.YELLOW + "--- Jump Help ---");

                    sender.sendMessage(ChatColor.YELLOW + "/jump create <name>");
                    sender.sendMessage(ChatColor.GRAY + "Creates a new jump with the given name. With start location on target block");
                    sender.sendMessage(ChatColor.YELLOW + "/jump list");
                    sender.sendMessage(ChatColor.GRAY + "Lists all jumps");
                    sender.sendMessage(ChatColor.YELLOW + "/jump info <name>");
                    sender.sendMessage(ChatColor.GRAY + "Prints information about the jump");
                    sender.sendMessage(ChatColor.YELLOW + "/jump delete <name>");
                    sender.sendMessage(ChatColor.GRAY + "Removes jump");
                    sender.sendMessage(ChatColor.YELLOW + "/jump set <start|finish|reset> <name>");
                    sender.sendMessage(ChatColor.GRAY + "Sets the start, finish or reset position of a jump");
                    sender.sendMessage(ChatColor.YELLOW + "/jump checkpoints <add|list|delete> <name> - Adds, lists or removes checkpoints from a jump");
                    sender.sendMessage(ChatColor.GRAY + "Adds, lists or removes checkpoints from a jump");
                    return Command.SINGLE_SUCCESS;
                })
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
                                    jumpService.createJump(jumpName, targetBlock);
                                    player.sendMessage(ChatColor.YELLOW + "Created jump \"%s\"".formatted(jumpName));
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
                                    sender.sendMessage(ChatColor.YELLOW + "Info about jump \"%s\"".formatted(jumpName));
                                    sender.sendMessage(Component.text(ChatColor.GRAY + "Start: ").append(jump.getStart() != null ? jump.getStart().toClickableLink() : Component.text("-- Start not set --")));
                                    sender.sendMessage(Component.text(ChatColor.GRAY + "Finish: ").append(jump.getFinish() != null ? jump.getFinish().toClickableLink() : Component.text("-- Finish not set --")));
                                    sender.sendMessage(Component.text(ChatColor.GRAY + "Reset: ").append(jump.getReset() != null ? jump.getReset().toClickableLink() : Component.text("-- Reset not set --")));
                                    sender.sendMessage(ChatColor.YELLOW + "Checkpoints:");
                                    jump.getCheckpoints().stream().map(JumpLocation::toClickableLink).forEach(sender::sendMessage);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            final var sender = ctx.getSource().getSender();
                            final var jumps = jumpService.getAll();
                            sender.sendMessage(ChatColor.YELLOW + "The following jumps are registered:");
                            jumps.forEach(it -> sender.sendMessage(Component.text(ChatColor.GRAY + "Jump " + ChatColor.YELLOW + it.getName() + ChatColor.GRAY + " starting at ").append(it.getStart().toClickableLink())));
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
                                    sender.sendMessage(ChatColor.YELLOW + "Deleted jump %s".formatted(jumpName));
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
                                            jumpService.addLocationToJump(jumpName, JumpLocationType.START, targetBlock);
                                            player.sendMessage(ChatColor.YELLOW + "Set start for jump %s".formatted(jumpName));
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
                                            jumpService.addLocationToJump(jumpName, JumpLocationType.FINISH, targetBlock);
                                            player.sendMessage(ChatColor.YELLOW + "Set finish for jump %s".formatted(jumpName));
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
                                            player.sendMessage(ChatColor.YELLOW + "Set reset-location for jump %s".formatted(jumpName));
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
                                            jumpService.addLocationToJump(jumpName, JumpLocationType.CHECKPOINT, targetBlock);
                                            player.sendMessage(ChatColor.YELLOW + "Added checkpoint to jump %s".formatted(jumpName));
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
                                            sender.sendMessage(ChatColor.YELLOW + "Checkpoints for jump %s".formatted(jumpName));
                                            checkPoints.forEach(it -> sender.sendMessage(it.toClickableLink()));
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
                                            jumpService.removeCheckpointForJump(jumpName, targetBlock);
                                            player.sendMessage(ChatColor.YELLOW + "Removed checkpoint from jump %s".formatted(jumpName));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .build();
        return root;
    }
}
