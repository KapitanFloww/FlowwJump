package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.events.PlayerFinishJumpEvent;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerFinishJumpListener implements Listener {

    private final JumpPlayerService jumpPlayerService;
    private final JumpLocationService jumpLocationService;

    public PlayerFinishJumpListener(JumpPlayerService jumpPlayerService,
                                    JumpLocationService jumpLocationService) {
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
    }

    @EventHandler
    public void handlePlayerFinishJumpEvent(PlayerFinishJumpEvent event) {
        final var player = event.getPlayer();
        final var jump = event.getJump();

        // Verify player is doing a jump
        final var currentPlayerJump = jumpPlayerService.getCurrentJumpFor(player);
        if (currentPlayerJump == null) {
            return;
        }
        // Verify players current jump matches the finished one
        if (!currentPlayerJump.getId().equals(jump.getId())) {
            return;
        }

        final var resetLocation = jumpLocationService.toLocation(jump.getReset(), false);
        player.teleport(resetLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.sendMessage(Component.text("Congratulations, you've mastered jump ", NamedTextColor.GREEN).append(Component.text(jump.getName(), NamedTextColor.GOLD)));
        final var totalTime = jumpPlayerService.unregisterPlayer(player);
        player.sendMessage(Component.text("Your total time: ", NamedTextColor.GREEN).append(Component.text(totalTime + " seconds", NamedTextColor.GOLD)));
    }
}
