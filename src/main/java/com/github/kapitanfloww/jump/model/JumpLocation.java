package com.github.kapitanfloww.jump.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@With
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JumpLocation implements Serializable {

    @Serial
    private static final long serialVersionUID = 10001;

    private UUID id;

    // Coordinates

    private Integer x;
    private Integer y;
    private Integer z;
    private String worldName;

    public Component toComponent(ClickCallback<Audience> clickCallback) {
        final var hoverEvent = HoverEvent.showText(Component.text("Click to teleport", NamedTextColor.GOLD));
        return Component.text("[%s, %s, %s]".formatted(x, y, z), NamedTextColor.GOLD)
                .hoverEvent(hoverEvent)
                .clickEvent(ClickEvent.callback(clickCallback));
    }

    public boolean matches(Block block) {
        return block != null
                && block.getX() == this.x
                && block.getY() == this.y
                && block.getZ() == this.z
                && block.getWorld().getName().equals(worldName);
    }

    public boolean matches(JumpLocation location) {
        return location != null
                && Objects.equals(location.getX(), this.x)
                && Objects.equals(location.getY(), this.y)
                && Objects.equals(location.getZ(), this.z)
                && Objects.equals(location.getWorldName(), worldName);
    }

    public static JumpLocation fromBlock(Block block) {
        return new JumpLocation()
                .withId(UUID.randomUUID())
                .withX(block.getX())
                .withY(block.getY())
                .withZ(block.getZ())
                .withWorldName(block.getWorld().getName());
    }
}