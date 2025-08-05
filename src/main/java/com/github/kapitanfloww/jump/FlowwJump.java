package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.commands.CheckpointCommand;
import com.github.kapitanfloww.jump.commands.JumpCommand;
import com.github.kapitanfloww.jump.listeners.PlayerDeathListener;
import com.github.kapitanfloww.jump.listeners.PlayerFinishJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerInteractEventListener;
import com.github.kapitanfloww.jump.listeners.PlayerReachesCheckpointJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerStartJumpListener;
import com.github.kapitanfloww.jump.persistence.FileBasedInMemoryJumpRepository;
import com.github.kapitanfloww.jump.persistence.InMemoryJumpRepository;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.github.kapitanfloww.jump.service.JumpService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Log
public final class FlowwJump extends JavaPlugin {

    private static final String path = "data";

    private JumpService jumpService;
    private JumpLocationService jumpLocationService;
    private JumpPlayerService jumpPlayerService;

    @Override
    public void onEnable() {
        // Register plugin logic
        try {
            if (getDataFolder().mkdirs()) {
                log.info("Data folder created");
            }
            final var dataFile = new File(getDataPath().toFile(), path);
            if (!dataFile.exists()) {
                Files.createFile(dataFile.toPath());
                log.info("Data file created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final var repository = new FileBasedInMemoryJumpRepository(path, new InMemoryJumpRepository());
        repository.loadFromFile();

        jumpService = new JumpService(repository);
        jumpLocationService = new JumpLocationService(jumpService, Bukkit::getWorld);
        jumpPlayerService = new JumpPlayerService();

        // Register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(JumpCommand.createCommand(jumpService, jumpLocationService, jumpPlayerService).build())); // /jump
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(CheckpointCommand.createCommand(jumpPlayerService, jumpLocationService).build())); // /checkpoint

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(getServer().getPluginManager(), jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerStartJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerFinishJumpListener(jumpPlayerService, jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerReachesCheckpointJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(jumpPlayerService, jumpLocationService), this);

        log.info("Enabled FlowwJump");
    }

    @Override
    public void onDisable() {
        log.info("Disabled FlowwJump");
    }
}
