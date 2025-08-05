package com.github.kapitanfloww.jump.score;

import org.bukkit.entity.Player;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record Score(UUID playerId, long time, Instant timestamp) implements Serializable {

    public static Score from(Player player, long time) {
        return new Score(player.getUniqueId(), time, Instant.now());
    }
}
