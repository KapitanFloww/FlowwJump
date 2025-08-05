package com.github.kapitanfloww.jump.service;

import com.github.kapitanfloww.jump.util.Timer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimerService {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;
    private final Map<UUID, Timer> playerTimerMap = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> runningTaskMap = new ConcurrentHashMap<>();

    public TimerService(Plugin plugin, BukkitScheduler scheduler) {
        this.plugin = Objects.requireNonNull(plugin);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    public void start(Player player) {
        final var timer = new Timer();
        playerTimerMap.put(player.getUniqueId(), timer);
        final var taskId = scheduler.scheduleSyncRepeatingTask(plugin, () -> {
            final var currentSeconds = timer.getTime();
            player.setLevel(Math.toIntExact(currentSeconds));
        }, 0, 20L);
        runningTaskMap.put(player.getUniqueId(), taskId);
    }

    public long stop(Player player) {
        final var timer = playerTimerMap.getOrDefault(player.getUniqueId(), null);
        if (timer == null) {
            throw new IllegalStateException("No timer found for player " + player.getUniqueId());
        }
        final var taskId = runningTaskMap.getOrDefault(player.getUniqueId(), 0);
        scheduler.cancelTask(taskId);
        player.setLevel(0);
        return timer.stop();
    }

    public void cancelAllTasks() {
        runningTaskMap.forEach((uuid, taskId) -> scheduler.cancelTask(taskId));
    }
}
