package com.github.kapitanfloww.jump.util;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerResolver {

    Player getPlayer(UUID playerId);
}
