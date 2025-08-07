package com.github.kapitanfloww.jump.listeners;

import com.github.kapitanfloww.jump.events.PlayerFinishJumpEvent;
import com.github.kapitanfloww.jump.holograms.JumpHologramManager;
import com.github.kapitanfloww.jump.score.Score;
import com.github.kapitanfloww.jump.score.ScoreboardService;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.github.kapitanfloww.jump.util.PlayerResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerFinishJumpListener implements Listener {

    private final PlayerResolver playerResolver;
    private final JumpPlayerService jumpPlayerService;
    private final JumpLocationService jumpLocationService;
    private final ScoreboardService scoreboardService;

    public PlayerFinishJumpListener(PlayerResolver playerResolver,
                                    JumpPlayerService jumpPlayerService,
                                    JumpLocationService jumpLocationService,
                                    ScoreboardService scoreboardService) {
        this.playerResolver = Objects.requireNonNull(playerResolver);
        this.jumpPlayerService = Objects.requireNonNull(jumpPlayerService);
        this.jumpLocationService = Objects.requireNonNull(jumpLocationService);
        this.scoreboardService = Objects.requireNonNull(scoreboardService);
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

        // Update the scoreboard
        final var score = Score.from(player, totalTime);
        final var isHighScore = scoreboardService.isNewHighScore(jump.getId(), score);
        if (isHighScore) {
            player.sendMessage(Component.text("You have set a new high-score for jump %s!".formatted(jump.getName()), NamedTextColor.GOLD));
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1.0f, 1.0f);
            JumpHologramManager.getJumpHologramManager(jumpLocationService).updateHighScore(jump, score);
            return;
        }

        final var highscore = scoreboardService.getHighScore(jump);
        player.sendMessage(Component.text("Current high-score: %s seconds by player %s".formatted(highscore.time(), playerResolver.getPlayer(highscore.playerId()).getName()), NamedTextColor.GRAY));
    }
}
