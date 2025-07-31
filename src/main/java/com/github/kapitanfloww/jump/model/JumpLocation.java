package com.github.kapitanfloww.jump.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;

import java.util.UUID;

@With
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JumpLocation {

    private UUID id;

    // Coordinates

    private Integer x;
    private Integer y;
    private Integer z;
    private String worldName;

    public Component toTeleportComponent() {
        final var tpEvent = ClickEvent.runCommand("tp %s %s %s".formatted(x, y + 0.5, z));
        return Component.text("[%s, %s, %s]".formatted(x, y, z), NamedTextColor.GOLD).clickEvent(tpEvent);
    }

    public boolean matches(Block block) {
        return block != null
                && block.getX() == this.x
                && block.getY() == this.y
                && block.getZ() == this.z
                && block.getWorld().getName().equals(worldName);
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