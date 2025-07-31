package com.github.kapitanfloww.jump.service;

import com.github.kapitanfloww.jump.model.Jump;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JumpPlayerService {

    private final Map<UUID, Jump> playerJumpMap = new ConcurrentHashMap<>();

    public void registerPlayer(Player player, Jump jump) {
        playerJumpMap.put(player.getUniqueId(), jump);
    }

    public void unregisterPlayer(Player player) {
        playerJumpMap.remove(player.getUniqueId());
    }

    public Jump getCurrentJumpFor(Player player) {
        return playerJumpMap.getOrDefault(player.getUniqueId(), null);
    }
}
