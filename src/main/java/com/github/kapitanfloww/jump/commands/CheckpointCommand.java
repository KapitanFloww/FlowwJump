package com.github.kapitanfloww.jump.commands;

import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Command for /checkpoints
 * <ul>
 *     <li>Requires sender to be {@link Player}</li>
 *     <li>Requires permission {@code floww.jump.checkpoints</li>
 * </ul>
 */
public class CheckpointCommand {

    private static final String PERMISSION = "floww.jump.checkpoints";

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(JumpPlayerService jumpPlayerService, JumpLocationService jumpLocationService) {
        return Commands.literal("checkpoint")
                .requires(source -> source.getSender() instanceof Player)
                .requires(source -> source.getSender().hasPermission(PERMISSION))
                .executes(ctx -> runCommand(ctx, jumpPlayerService, jumpLocationService));
    }

    private static int runCommand(CommandContext<CommandSourceStack> ctx, JumpPlayerService jumpPlayerService, JumpLocationService jumpLocationService) {
        final var player = (Player) ctx.getSource().getSender();
        final var checkpoint = jumpPlayerService.getCheckpoint(player);

        if (checkpoint == null) {
            player.sendMessage(Component.text("You have not reached any checkpoints yet", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS; // no checkpoints
        }

        player.teleport(jumpLocationService.toLocation(checkpoint, true));
        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
        player.sendMessage(Component.text("You have been teleported back to your last checkpoint", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}
