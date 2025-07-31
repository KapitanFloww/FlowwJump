package com.github.kapitanfloww.jump.service;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.model.JumpLocation;
import com.github.kapitanfloww.jump.model.JumpLocationType;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.Objects;

public class JumpLocationService {

    private final JumpService jumpService;
    private final WorldResolver worldResolver;

    public JumpLocationService(JumpService jumpService, WorldResolver worldResolver) {
        this.jumpService = Objects.requireNonNull(jumpService);
        this.worldResolver = Objects.requireNonNull(worldResolver);
    }

    public Pair<JumpLocationType, Jump> getJumpForBlock(Block block) {
        final var jumps = jumpService.getAll();
        for (Jump jump : jumps) {
            if (!jump.isSetupComplete()) {
                continue;
            }
            if (jump.getStart().matches(block)) {
                return Pair.of(JumpLocationType.START, jump);
            }
            if (jump.getFinish().matches(block)) {
                return Pair.of(JumpLocationType.FINISH, jump);
            }
            if (jump.getReset().matches(block)) {
                return Pair.of(JumpLocationType.RESET, jump);
            }
            if (jump.getCheckpoints().stream().anyMatch(it -> it.matches(block))) {
                return Pair.of(JumpLocationType.CHECKPOINT, jump);
            }
        }
        return null;
    }

    public Location toLocation(JumpLocation jumpLocation) {
        return new Location(worldResolver.getWorld(jumpLocation.getWorldName()), jumpLocation.getX(), jumpLocation.getY() + 1, jumpLocation.getZ());
    }
}
