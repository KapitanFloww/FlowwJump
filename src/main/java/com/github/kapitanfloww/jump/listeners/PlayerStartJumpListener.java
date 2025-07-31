package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.events.PlayerStartJumpEvent;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class PlayerStartJumpListener implements Listener {

    private final JumpPlayerService jumpPlayerService;

    public PlayerStartJumpListener(JumpPlayerService jumpPlayerService) {
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
    }

    @EventHandler
    public void handlePlayerStartJumpEvent(PlayerStartJumpEvent event) {
        final var player = event.getPlayer();
        final var jump = event.getJump();

        // Check if player is already doing another jump
        final var currentPlayerJump = jumpPlayerService.getCurrentJumpFor(player);
        if (currentPlayerJump != null) {
            if (currentPlayerJump.getId().equals(jump.getId())) {
                player.sendMessage(ChatColor.RED + "You are already doing this jump.");
                return;
            }
            player.sendMessage(ChatColor.RED + "You are already doing the jump %s. Please finish this jump before starting a new one or cancel your current jump with /jump cancel.".formatted(currentPlayerJump.getName()));
            return;
        }

        // Register player to jump
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Starting jump %s. Good luck!".formatted(jump.getName()));
        jumpPlayerService.registerPlayer(player, jump);
    }
}
