package com.github.kapitanfloww.jump.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerFoodListener implements Listener {

    @EventHandler
    public void handlePlayerFoodEvent(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
