package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.model.JumpLocation;
import com.github.kapitanfloww.jump.persistence.InMemoryJumpRepository;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlowwJump extends JavaPlugin {

    private JumpService service;

    @Override
    public void onEnable() {
        // Enable service
        service = new JumpService(new InMemoryJumpRepository());

        var commandTree = getCommandTree();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(commandTree);
        });
    }

    @Override
    public void onDisable() {

    }

    private LiteralCommandNode<CommandSourceStack> getCommandTree() {
        // Setup commands
        var root = Commands.literal("jump")
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.createJump(jumpName, targetBlock);
                                    player.sendMessage("Create jump \"%s\"".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            final var sender = ctx.getSource().getSender();
                            final var jumpNames = service.getAll()
                                    .stream()
                                    .map(Jump::getName)
                                    .toList();
                            sender.sendMessage("The following jumps are registered:");
                            jumpNames.forEach(sender::sendMessage);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    service.deleteJump(jumpName);
                                    final var sender = ctx.getSource().getSender();
                                    sender.sendMessage("Deleted jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.literal("start")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.addLocationToJump(jumpName, JumpLocation.Type.START, targetBlock);
                                    player.sendMessage("Set start for jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("finish")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.addLocationToJump(jumpName, JumpLocation.Type.FINISH, targetBlock);
                                    player.sendMessage("Set finish for jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("reset")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.addLocationToJump(jumpName, JumpLocation.Type.RESET, targetBlock);
                                    player.sendMessage("Set reset-location for jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("checkpoints")
                        .then(Commands.literal("add")
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.addLocationToJump(jumpName, JumpLocation.Type.CHECKPOINT, targetBlock);
                                    player.sendMessage("Added checkpoint to jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var sender = ctx.getSource().getSender();
                                    final var checkPoints = service.getCheckpointsForJump(jumpName);
                                    sender.sendMessage("Checkpoints for jump %s".formatted(jumpName));
                                    checkPoints.forEach(it -> sender.sendMessage("[%s, %s, %s]".formatted(it.getX(), it.getY(), it.getZ())));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.string()))
                                .requires(source -> source.getSender() instanceof Player)
                                .executes(ctx -> {
                                    final var jumpName = StringArgumentType.getString(ctx, "name");
                                    final var player = (Player) ctx.getSource().getSender();
                                    final var targetBlock = player.getTargetBlock(null, 10);
                                    service.removeCheckpointForJump(jumpName, targetBlock);
                                    player.sendMessage("Removed checkpoint from jump %s".formatted(jumpName));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
        return root;
    }
}
