package com.github.kapitanfloww.jump.persistence;

import com.github.kapitanfloww.jump.model.Jump;

import java.util.Optional;
import java.util.Set;

public interface JumpRepository {

    Jump save(Jump jump);

    Optional<Jump> find(String name);

    Set<Jump> findAll();

    void delete(Jump jump);
}
