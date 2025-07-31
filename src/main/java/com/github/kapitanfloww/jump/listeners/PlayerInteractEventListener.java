package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.events.PlayerFinishJumpEvent;
import com.github.kapitanfloww.jump.events.PlayerReachesCheckpointJumpEvent;
import com.github.kapitanfloww.jump.events.PlayerStartJumpEvent;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public class PlayerInteractEventListener implements Listener {

    private final PluginManager pluginManager;
    private final JumpLocationService jumpLocationService;

    public PlayerInteractEventListener(PluginManager pluginManager, JumpLocationService jumpLocationService) {
        this.pluginManager = Objects.requireNonNull(pluginManager);
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
    }

    @EventHandler
    public void onPlayerInteraction(PlayerInteractEvent event) {
        // Check if player performed right-click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
            return; // Not a right click or pressure plate execution
        }

        // Check if player clicked a button
        final var interactedMaterial = event.getClickedBlock().getType();
        if (!Tag.BUTTONS.isTagged(interactedMaterial) && !Tag.PRESSURE_PLATES.isTagged(interactedMaterial)) {
            return; // Not a button click
        }

        // Check if button is part of a jump
        final var jumpPair = jumpLocationService.getJumpForBlock(event.getClickedBlock());
        if (jumpPair == null) {
            return; // Not part of a jump
        }

        // Call event based on location type
        final var jump = jumpPair.getRight();
        final var type = jumpPair.getLeft();
        switch (type) {
            case START -> pluginManager.callEvent(new PlayerStartJumpEvent(jump, event.getPlayer())); // start the jump
            case FINISH -> pluginManager.callEvent(new PlayerFinishJumpEvent(jump, event.getPlayer())); // finish the jump
            case CHECKPOINT -> pluginManager.callEvent(new PlayerReachesCheckpointJumpEvent(jump, event.getPlayer(), event.getClickedBlock())); // set last checkpoint
            case RESET -> {} // do nothing
        }
    }
}
