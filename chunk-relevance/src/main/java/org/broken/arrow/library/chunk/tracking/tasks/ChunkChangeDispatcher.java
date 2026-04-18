package org.broken.arrow.library.chunk.tracking.tasks;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.utility.ChunkState;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkChangeDispatcher implements Runnable {
    private final Map<ChunkKey, ChunkState> pending = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private BukkitTask task;
    private volatile long lastUpdateTick;
    private volatile long firstUpdateTick;
    private static final long QUIET_PERIOD = 2L;
    private static final long MAX_DELAY = 10L;

    public ChunkChangeDispatcher(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        if (pending.isEmpty()) return;
        final long now = TickTask.getTick();
        final boolean quiet = (now - lastUpdateTick) >= QUIET_PERIOD;
        final boolean forced = (now - firstUpdateTick) >= MAX_DELAY;

        if (!quiet && !forced) {
            return;
        }

        final Map<ChunkKey, ChunkState> current = new HashMap<>(pending);
        pending.clear();

        for (ChunkState state : current.values()) {
            state.apply();
        }
    }

    public void submit(ChunkState task) {
        pending.put(task.getKey(), task);
        final long now = TickTask.getTick();
        if (pending.size() == 1) {
            firstUpdateTick = now;
        }
        lastUpdateTick = now;
    }
}

