package com.github.kapitanfloww.jump.score;

import com.github.kapitanfloww.jump.model.Jump;
import lombok.extern.java.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class ScoreboardService {

    private final String path;
    private Map<UUID, Score> highScores = new ConcurrentHashMap<>();

    public ScoreboardService(String path) {
        this.path = Objects.requireNonNull(path);
    }

    public boolean isNewHighScore(UUID jumpId, Score score) {
        final var currentHighscore = highScores.get(jumpId);
        if (currentHighscore == null ) {
            highScores.put(jumpId, score);
            log.info("Highscore for jump %s has been created. New highscore = %s".formatted(jumpId, score.time()));
            serialize();
            return true; // New high-score
        }
        if (currentHighscore.time() > score.time()) {
            highScores.put(jumpId, score);
            log.info("Highscore for jump %s has been updated. New highscore = %s".formatted(jumpId, score.time()));
            serialize();
            return true; // New high-score
        }
        return false; // No new high-score
    }

    public Score getHighScore(Jump jump) {
        return highScores.get(jump.getId());
    }

    public void loadFromFile() {
        log.info("Loading scoreboard-data from file");
        if (!Files.exists(Paths.get(path))) {
            log.warning("Scoreboard-data file does not exist");
            return;
        }
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(path))) {
            final var scores = (Map<UUID, Score>) inputStream.readObject();
            log.info("Loaded %s scores".formatted(scores.size()));
            highScores = scores;
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException(ex.getLocalizedMessage());
        }
    }

    private void serialize() {
        log.info("Writing %s scores to data file".formatted(highScores.size()));
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path))) {
            outputStream.writeObject(highScores);
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getLocalizedMessage());
        }
    }
}
