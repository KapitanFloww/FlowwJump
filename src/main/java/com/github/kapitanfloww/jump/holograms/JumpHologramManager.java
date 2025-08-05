package com.github.kapitanfloww.jump.holograms;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.score.Score;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.util.PlayerResolver;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log
public class JumpHologramManager {

    private static JumpHologramManager singleton;

    private static final String NAME_PATTERN = "floww_jump_%s";

    private final PlayerResolver playerResolver;
    private final JumpLocationService locationService;
    private final HologramManager hologramManager;

    public JumpHologramManager(PlayerResolver playerResolver, JumpLocationService locationService, HologramManager hologramManager) {
        this.playerResolver = Objects.requireNonNull(playerResolver);
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

    public void updateHighScore(Jump jump, Score score) {
        final var optionalHologram = getHologram(jump);
        if (optionalHologram.isEmpty()) {
            return;
        }
        final var hologram = optionalHologram.get();
        final var hologramData = (TextHologramData) hologram.getData();
        hologramData.setText(List.of(
                "~~ Welcome to Jump: " + jump.getName() + " ~~",
                "High-Score: %s seconds by %s".formatted(score.time(), playerResolver.getPlayer(score.playerId()).getName())));

        hologram.forceUpdate();
        hologram.queueUpdate();
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

    public static JumpHologramManager getJumpHologramManager(JumpLocationService jumpLocationService) {
        try {
            if (!FancyHologramsPlugin.isEnabled()) {
                throw new IllegalArgumentException("FancyHolograms is not enabled! Skipping integration.");
            }
            if (singleton == null) {
                singleton = new JumpHologramManager(Bukkit::getPlayer, jumpLocationService, FancyHologramsPlugin.get().getHologramManager());
            }
            return singleton;
        } catch (NoClassDefFoundError ex) {
            throw new IllegalArgumentException("FancyHolograms is not on the plugins list. Disabling integration");
        }
    }
}
