package com.github.kapitanfloww.jump.service;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.model.JumpLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpPlayerService {

    private final Map<UUID, Jump> playerJumpMap = new ConcurrentHashMap<>();
    private final Map<UUID, JumpLocation> playerCheckpointMap = new ConcurrentHashMap<>();

    public void registerPlayer(Player player, Jump jump) {
        playerJumpMap.put(player.getUniqueId(), jump);
    }

    public void unregisterPlayer(Player player) {
        playerJumpMap.remove(player.getUniqueId());
        playerCheckpointMap.remove(player.getUniqueId());
    }

    public Jump getCurrentJumpFor(Player player) {
        return playerJumpMap.getOrDefault(player.getUniqueId(), null);
    }

    public void setCheckpoint(Player player, JumpLocation checkpoint) {
        playerCheckpointMap.put(player.getUniqueId(), checkpoint);
    }

    public JumpLocation getCheckpoint(Player player) {
        final var currentJump = getCurrentJumpFor(player);
        if (currentJump == null) {
            player.sendMessage(Component.text("You are not registered in a jump", NamedTextColor.RED));
            return null;
        }
        return playerCheckpointMap.getOrDefault(player.getUniqueId(), currentJump.getStart());
    }
}
