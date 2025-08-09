package com.github.kapitanfloww.jump.holograms.events;

import com.github.kapitanfloww.jump.model.Jump;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Getter
public class UpdateJumpHologramEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Jump jump;
    private final List<String> text;

    public UpdateJumpHologramEvent(Jump jump, List<String> text) {
        this.jump = Objects.requireNonNull(jump);
        this.text = Objects.requireNonNull(text);
    }


    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
