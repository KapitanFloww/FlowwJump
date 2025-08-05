package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

public class PlayerDeathListener implements Listener {

    private final JumpPlayerService playerService;
    private final JumpLocationService jumpLocationService;

    public PlayerDeathListener(JumpPlayerService playerService, JumpLocationService jumpLocationService) {
        this.playerService = Objects.requireNonNull(playerService);
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
    }

    @EventHandler
    public void handlePlayerDamageEvent(EntityDamageEvent event) {
        // Check if entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if damage would kill the player
        final var health = player.getHealth();
        final var damage = event.getFinalDamage();
        if (health - damage > 0) {
            return;
        }

        // Check if player is in a jump
        if(playerService.getCurrentJumpFor(player) == null) {
            return;
        }

        // Check if checkpoint has been reached
        final var checkpoint = playerService.getCheckpoint(player);
        if (checkpoint == null) {
            player.sendMessage(Component.text("You have not reached any checkpoints yet", NamedTextColor.RED));
        }

        // Cancel the event
        event.setCancelled(true);
        event.setDamage(0.0);

        // Teleport to checkpoint
        player.teleport(jumpLocationService.toLocation(checkpoint, true), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.playSound(player.getLocation(), Sound.ENTITY_PARROT_FLY, 1.0f, 1.0f);
        player.sendMessage(Component.text("You have been teleported back to your last checkpoint", NamedTextColor.GREEN));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 10, false, false));

        // Health the player to max
        final var maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        player.setHealth(maxHealth);
    }
}
