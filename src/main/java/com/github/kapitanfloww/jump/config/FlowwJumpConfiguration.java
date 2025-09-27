package com.github.kapitanfloww.jump.config;

import lombok.extern.java.Log;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

@Log
public class FlowwJumpConfiguration {

    private final FileConfiguration fileConfiguration;

    public FlowwJumpConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = Objects.requireNonNull(fileConfiguration);
        validate();
    }

    public StorageBackend getStorageType() {
        return StorageBackend.valueOf(fileConfiguration.getString("storage-backend"));
    }

    private void validate() {
        try (final var in = new InputStreamReader(Objects.requireNonNull(FlowwJumpConfiguration.class.getResourceAsStream("/config.yml")))) {
            final var expectedConfiguration = new YamlConfiguration();
            expectedConfiguration.load(in);

            // Check if any key is missing in actual configuration
            final var missingKeys = new ArrayList<String>();
            final var expectedKeys = expectedConfiguration.getKeys(true);
            final var actualKeys = fileConfiguration.getKeys(true);
            for (String expectedKey : expectedKeys) {
                if (!actualKeys.contains(expectedKey)) {
                    log.severe("Configuration is missing key: " + expectedKey);
                    missingKeys.add(expectedKey);
                }
            }
            if (!missingKeys.isEmpty()) {
                throw new IllegalStateException("Configuration is missing keys: %s".formatted(String.join(", ", missingKeys)));
            }
        } catch (IOException | InvalidConfigurationException ex) {
            log.severe("Could not load config.yml from resource folder: " + ex);
        }
    }
}
