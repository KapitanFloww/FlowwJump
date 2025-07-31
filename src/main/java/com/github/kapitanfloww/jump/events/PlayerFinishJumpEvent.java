package com.github.kapitanfloww.jump.events;

import com.github.kapitanfloww.jump.model.Jump;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class PlayerFinishJumpEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Jump jump;
    private final Player player;

    public PlayerFinishJumpEvent(Jump jump, Player player) {
        this.jump = Objects.requireNonNull(jump);
        this.player = Objects.requireNonNull(player);
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
