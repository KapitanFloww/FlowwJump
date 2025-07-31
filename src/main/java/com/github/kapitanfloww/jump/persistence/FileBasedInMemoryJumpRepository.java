package com.github.kapitanfloww.jump.persistence;

import com.github.kapitanfloww.jump.model.Jump;
import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Log
public class FileBasedInMemoryJumpRepository implements JumpRepository {

    private final String path;
    private final JumpRepository delegate;

    public FileBasedInMemoryJumpRepository(String file, JumpRepository delegate) {
        this.path = Objects.requireNonNull(file);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Jump save(Jump jump) {
        final var persisted = delegate.save(jump);
        serialize();
        return persisted;
    }

    @Override
    public Jump merge(Jump jump) {
        final var merged = delegate.merge(jump);
        serialize();
        return merged;

    }

    @Override
    public Optional<Jump> find(String name) {
        return delegate.find(name);
    }

    @Override
    public Set<Jump> findAll() {
        return delegate.findAll();
    }

    @Override
    public void delete(Jump jump) {
        delegate.delete(jump);
        serialize();
    }

    @Override
    public Set<String> findAllJumpNames() {
        return delegate.findAllJumpNames();
    }

    public void loadFromFile() {
        log.info("Loading jumps from data file");
        if (!Files.exists(Paths.get(path))) {
            log.warning("Data file does not exist");
            return;
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            final var jumps = (Set<Jump>) inputStream.readObject();
            log.info("Loaded %s jumps".formatted(jumps.size()));
            jumps.forEach(delegate::save);
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException(ex.getLocalizedMessage());
        }
    }

    private void serialize() {
        final var jumps = delegate.findAll();
        log.info("Writing %s jumps to data file".formatted(jumps.size()));
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(jumps);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getLocalizedMessage());
        }
    }
}
