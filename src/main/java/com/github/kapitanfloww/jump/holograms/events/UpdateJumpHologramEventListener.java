package com.github.kapitanfloww.jump.holograms.events;

import com.github.kapitanfloww.jump.holograms.JumpHologramManager;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class UpdateJumpHologramEventListener implements Listener {

    private final JumpLocationService jumpLocationService;

    public UpdateJumpHologramEventListener(JumpLocationService jumpLocationService) {
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
    }

    @EventHandler
    public void handleUpdateJumpHologramEvent(UpdateJumpHologramEvent event) {
        final var jump = event.getJump();
        final var newText = event.getText();
        JumpHologramManager.getJumpHologramManager(jumpLocationService).updateHologramText(jump, newText);
    }
}
