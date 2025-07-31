package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.events.PlayerReachesCheckpointJumpEvent;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class PlayerReachesCheckpointJumpListener implements Listener {

    private final JumpPlayerService jumpPlayerService;

    public PlayerReachesCheckpointJumpListener(JumpPlayerService jumpPlayerService) {
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
    }

    @EventHandler
    public void handlePlayerReachesCheckpointJumpEvent(PlayerReachesCheckpointJumpEvent event) {
        final var player = event.getPlayer();
        final var jump = event.getJump();

        // Check if jump is set up correctly
        if (!jump.isSetupComplete()) {
            event.getPlayer().sendMessage(Component.text("This jump is not fully set up yet. Please inspect with /jump info %s".formatted(jump.getName()), NamedTextColor.RED));
            return;
        }

        // Verify player is doing a jump
        final var currentPlayerJump = jumpPlayerService.getCurrentJumpFor(player);
        if (currentPlayerJump == null) {
            return;
        }

        // Verify players current jump matches the finished one
        if (!currentPlayerJump.getId().equals(jump.getId())) {
            return;
        }

        // If already the same checkpoint - skip
        final var checkpoint = jumpPlayerService.getCheckpoint(player);
        if (checkpoint != null && checkpoint.matches(event.getCheckpoint())) {
            return;
        }

        player.sendMessage(Component.text("Reached checkpoint", NamedTextColor.GREEN));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        jumpPlayerService.setCheckpoint(player, event.getCheckpoint());
    }
}