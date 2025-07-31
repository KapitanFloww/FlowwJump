package com.github.kapitanfloww.jump.persistence;

import com.github.kapitanfloww.jump.model.Jump;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryJumpRepository implements JumpRepository {

    private final Set<Jump> jumps = new HashSet<>();

    @Override
    public Jump save(Jump jump) {
        if (jumps.add(jump)) {
            return jump;
        }
        throw new IllegalArgumentException("This jump does already exist");
    }

    @Override
    public Optional<Jump> find(String name) {
        return jumps.stream()
                .filter(jump -> Objects.equals(jump.getName(), name))
                .findFirst();
    }

    @Override
    public Set<Jump> findAll() {
        return jumps;
    }

    @Override
    public void delete(Jump jump) {
        jumps.remove(jump);
    }

    @Override
    public Set<String> findAllJumpNames() {
        return findAll().stream().map(Jump::getName).collect(Collectors.toSet());
    }
}
