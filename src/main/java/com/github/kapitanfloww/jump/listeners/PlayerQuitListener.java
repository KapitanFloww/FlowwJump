package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.service.JumpPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerQuitListener implements Listener {

    private final JumpPlayerService jumpPlayerService;

    public PlayerQuitListener(JumpPlayerService jumpPlayerService) {
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
    }

    @EventHandler
    public void handlePlayerQuit(PlayerQuitEvent event) {
        if (jumpPlayerService.getCurrentJumpFor(event.getPlayer()) != null) {
            jumpPlayerService.unregisterPlayer(event.getPlayer());
        }
    }
}
