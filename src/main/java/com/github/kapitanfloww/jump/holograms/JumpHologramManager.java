package com.github.kapitanfloww.jump.holograms;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import lombok.extern.java.Log;
import org.bukkit.Color;
import org.bukkit.entity.Display;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log
public class JumpHologramManager {

    private static final String NAME_PATTERN = "floww_jump_%s";

    private final JumpLocationService locationService;
    private final HologramManager hologramManager;

    public JumpHologramManager(JumpLocationService locationService, HologramManager hologramManager) {
        this.locationService = Objects.requireNonNull(locationService);
        this.hologramManager = Objects.requireNonNull(hologramManager);
    }

    public void createHologram(Jump jump) {
        // Check if hologram already exist
        if (getHologram(jump).isPresent()) {
            throw new IllegalArgumentException("Hologram with that name already exist!");
        }
        final var location = locationService.toLocation(jump.getStart(), false).add(0.5, 1.5, 0.5);
        final var hologramData = new TextHologramData(NAME_PATTERN.formatted(jump.getName()), location);
        hologramData.setBackground(Color.fromBGR(100, 255, 79));
        hologramData.setBillboard(Display.Billboard.CENTER);
        hologramData.removeLine(0);
        hologramData.setText(List.of("~~ Welcome to Jump: " + jump.getName() + " ~~"));

        final var manager = FancyHologramsPlugin.get().getHologramManager();
        final var hologram = manager.create(hologramData);

        manager.addHologram(hologram);
        log.info("Hologram %s created at [%s, %s, %s]".formatted(NAME_PATTERN.formatted(jump.getName()), location.getX(), location.getY(), location.getZ()));
    }

    public void removeHologram(Jump jump) {
        final var hologram = getHologram(jump);
        hologram.ifPresent(hologramManager::removeHologram);
        log.info("Hologram %s removed".formatted(NAME_PATTERN.formatted(jump.getName())));
    }

    public void moveHologram(Jump jump) {
        final var optionalHologram = getHologram(jump);
        if (optionalHologram.isEmpty()) {
            throw new IllegalArgumentException("Hologram for jump %s does not exist".formatted(jump.getName()));
        }

        final var hologram = optionalHologram.get();
        final var hologramData = hologram.getData();

        final var location = locationService.toLocation(jump.getStart(), false).add(0.5, 1.5, 0.5);
        hologramData.setLocation(location);

        hologram.forceUpdate();
        hologram.queueUpdate();
    }

    public Optional<Hologram> getHologram(Jump jump) {
        return hologramManager.getHologram(NAME_PATTERN.formatted(jump.getName()));
    }
}
