package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.model.Jump;
import com.github.kapitanfloww.jump.model.JumpLocation;
import com.github.kapitanfloww.jump.model.JumpLocationType;
import com.github.kapitanfloww.jump.persistence.JumpRepository;
import lombok.extern.java.Log;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Log
public class JumpService {

    private final JumpRepository repository;

    public JumpService(JumpRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void createJump(String jumpName, Block start) {
        // Check if jump with the name exists
        if (findJump(jumpName).isPresent()) {
            throw new IllegalArgumentException("Jump with name \"%s\" already exists".formatted(jumpName));
        }
        // Create jump with start location
        final var jump = new Jump()
                .withId(UUID.randomUUID())
                .withName(jumpName);
        final var startLocation = JumpLocation.fromBlock(start);
        jump.setStart(startLocation);

        repository.save(jump);
        log.info("Created jump %s".formatted(jump));
    }

    public void addLocationToJump(String jumpName, JumpLocationType type, Block location) {
        final var jump = getJump(jumpName);
        final var newLocation = JumpLocation.fromBlock(location);
        switch (type) {
            case START -> jump.setStart(newLocation); // can only have one start
            case FINISH -> jump.setFinish(newLocation); // can only have one finish
            case RESET -> jump.setReset(newLocation); // can only have one reset
            case CHECKPOINT -> jump.addCheckpoints(newLocation); // can have multiple checkpoints
        }
        log.info("Added location %s to jump \"%s\"".formatted(type, jump));
    }

    public void removeCheckpointForJump(String jumpName, Block location) {
        final var jump = getJump(jumpName);
        final var checkpoints = jump.getCheckpoints();
        final var checkpointToRemove = checkpoints.stream()
                .filter(it -> it.getX() == location.getX() && it.getY() == location.getY() && it.getZ() == location.getZ())
                .findFirst();
        checkpointToRemove.ifPresentOrElse(
                checkpoints::remove,
                () -> {
                    throw new IllegalArgumentException("Location unknown to jump \"%s\"".formatted(jumpName));
                }
        );
    }

    public Jump getJump(String jumpName) {
        return findJump(jumpName)
                .orElseThrow(() -> new IllegalArgumentException("Jump \"%s\" not found".formatted(jumpName)));
    }

    public Optional<Jump> findJump(String jumpName) {
        return repository.find(jumpName);
    }

    public Set<Jump> getAll() {
        return repository.findAll();
    }

    public void deleteJump(String jumpName) {
        final var jump = getJump(jumpName);
        repository.delete(jump);
    }

    public List<JumpLocation> getCheckpointsForJump(String jumpName) {
        final var jump = getJump(jumpName);
        return jump.getCheckpoints();
    }
}
