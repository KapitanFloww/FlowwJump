package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.item.ItemManager;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;

public class PlayerInventoryListener implements Listener {

    private final JumpPlayerService jumpPlayerService;
    private final JumpLocationService jumpLocationService;

    public PlayerInventoryListener(JumpPlayerService jumpPlayerService, JumpLocationService jumpLocationService) {
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
    }

    @EventHandler
    public void handleItemDropEvent(PlayerDropItemEvent event) {
        final var item = event.getItemDrop().getItemStack();
        if (item.equals(ItemManager.getCheckpointItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleInventoryDragEvent(InventoryDragEvent event) {
        if (event.getOldCursor().equals(ItemManager.getCheckpointItem())) {
            event.setCancelled(true);
        }
        if (event.getCursor() != null && event.getCursor().equals(ItemManager.getCheckpointItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleInventoryClickEvent(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getCurrentItem().equals(ItemManager.getCheckpointItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // this needs to be called after PlayerInteractEventListener
    public void handleCheckpointItemClickEvent(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)) {
            return;
        }

        // Verify checkpoint item is pressed
        final var item = event.getItem();
        if (item == null) return;
        if (!item.equals(ItemManager.getCheckpointItem())) return;

        // Teleport to checkpoint
        final var player = event.getPlayer();
        final var checkpoint = jumpPlayerService.getCheckpoint(player);

        if (checkpoint == null) {
            player.sendMessage(Component.text("You have not reached any checkpoints yet", NamedTextColor.RED));
            return;
        }

        player.teleport(jumpLocationService.toLocation(checkpoint, true));
        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
        player.sendMessage(Component.text("You have been teleported back to your last checkpoint", NamedTextColor.GREEN));
    }
}
