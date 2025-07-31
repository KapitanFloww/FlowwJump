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
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@With
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class JumpLocation {

    private UUID id;

    private BlockType blockType;

    // Coordinates

    private Integer x;
    private Integer y;
    private Integer z;
    private String worldName;

    public Component toClickableLink() {
        final var tpEvent = ClickEvent.runCommand("tp %s %s %s".formatted(x, y + 0.5, z));
        return Component.text(ChatColor.YELLOW + "[%s, %s, %s]".formatted(x, y, z)).clickEvent(tpEvent);
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
                .withWorldName(block.getWorld().getName())
                .withBlockType(determineType(block));
    }

    public enum BlockType {
        PRESSURE_PLATE,
        BUTTON
    }

    private static JumpLocation.BlockType determineType(Block block) {
        final var material = block.getType();
        if (Tag.PRESSURE_PLATES.getValues().contains(material)) {
            return BlockType.PRESSURE_PLATE;
        }
        if (Tag.BUTTONS.getValues().contains(material)) {
            return BlockType.BUTTON;
        }
        throw new IllegalArgumentException("Cannot add location for material %s. Supported materials: %s"
                .formatted(block.getType(), Stream.of(Tag.PRESSURE_PLATES.getValues(), Tag.BUTTONS.getValues())
                        .collect(Collectors.toSet())));
    }
}