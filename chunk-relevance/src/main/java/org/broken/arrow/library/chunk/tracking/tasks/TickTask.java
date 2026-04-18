package org.broken.arrow.library.chunk.tracking.tasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

public class TickTask implements Runnable {
    private static final AtomicLong tick = new AtomicLong();
    private final Plugin plugin;
    private BukkitTask task;


    public TickTask(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this, 0L, 1L);
    }

    public void stop() {
        if (this.task == null) {
            return;
        }
        this.task.cancel();
        this.task = null;
    }

    @Override
    public void run() {
        tick.incrementAndGet();
    }

    public static long getTick() {
        return tick.get();
    }
}
