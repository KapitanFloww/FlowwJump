package com.github.kapitanfloww.jump;

import com.github.kapitanfloww.jump.commands.CheckpointCommand;
import com.github.kapitanfloww.jump.commands.JumpCommand;
import com.github.kapitanfloww.jump.holograms.events.UpdateJumpHologramEventListener;
import com.github.kapitanfloww.jump.listeners.PlayerDeathListener;
import com.github.kapitanfloww.jump.listeners.PlayerFinishJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerFoodListener;
import com.github.kapitanfloww.jump.listeners.PlayerInteractEventListener;
import com.github.kapitanfloww.jump.listeners.PlayerInventoryListener;
import com.github.kapitanfloww.jump.listeners.PlayerReachesCheckpointJumpListener;
import com.github.kapitanfloww.jump.listeners.PlayerStartJumpListener;
import com.github.kapitanfloww.jump.persistence.FileBasedInMemoryJumpRepository;
import com.github.kapitanfloww.jump.persistence.InMemoryJumpRepository;
import com.github.kapitanfloww.jump.score.ScoreboardService;
import com.github.kapitanfloww.jump.service.JumpLocationService;
import com.github.kapitanfloww.jump.service.JumpPlayerService;
import com.github.kapitanfloww.jump.service.JumpService;
import com.github.kapitanfloww.jump.service.TimerService;
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
    private static final String pathScoreboardData = "data_scores";

    private JumpService jumpService;
    private JumpLocationService jumpLocationService;
    private JumpPlayerService jumpPlayerService;
    private TimerService timerService;
    private ScoreboardService scoreboardService;

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
            final var scoreboardData = new File(getDataPath().toFile(), pathScoreboardData);
            if (!scoreboardData.exists()) {
                Files.createFile(scoreboardData.toPath());
                log.info("Scoreboard-data file created");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final var repository = new FileBasedInMemoryJumpRepository(path, new InMemoryJumpRepository());
        repository.loadFromFile();

        jumpService = new JumpService(repository);
        jumpLocationService = new JumpLocationService(jumpService, Bukkit::getWorld);
        timerService = new TimerService(this, Bukkit.getScheduler());
        jumpPlayerService = new JumpPlayerService(timerService);
        scoreboardService = new ScoreboardService(pathScoreboardData);
        scoreboardService.loadFromFile();

        // Register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(JumpCommand.createCommand(jumpService, jumpLocationService, jumpPlayerService, getServer().getPluginManager(), scoreboardService).build())); // /jump
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(CheckpointCommand.createCommand(jumpPlayerService, jumpLocationService).build())); // /checkpoint

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerInteractEventListener(getServer().getPluginManager(), jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerStartJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerFinishJumpListener(getServer().getPluginManager(), Bukkit::getPlayer, jumpPlayerService, jumpLocationService, scoreboardService), this);
        getServer().getPluginManager().registerEvents(new PlayerReachesCheckpointJumpListener(jumpPlayerService), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(jumpPlayerService, jumpLocationService), this);
        getServer().getPluginManager().registerEvents(new PlayerFoodListener(), this); // Disable hunger
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(jumpPlayerService, jumpLocationService), this); // Disable hunger
        getServer().getPluginManager().registerEvents(new UpdateJumpHologramEventListener(jumpLocationService), this); // Disable hunger

        log.info("Enabled FlowwJump");
    }

    @Override
    public void onDisable() {
        timerService.cancelAllTasks();
        log.info("Disabled FlowwJump");
    }
}
