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
/**
 * Dispatches {@link ChunkState} updates asynchronously with batching.
 *
 * <p>
 * This dispatcher collects submitted {@link ChunkState} instances and
 * processes them after a short delay, allowing multiple rapid updates
 * to the same chunk to be coalesced into a single operation.
 *
 * <p>
 * Updates are stored per {@link ChunkKey}. If multiple states are submitted
 * for the same key before dispatch, only the most recent state is retained.
 *
 * <p>
 * Dispatch timing is controlled by two conditions:
 * <ul>
 *     <li><b>Quiet period:</b> Processing occurs if no new updates have been
 *     received for {@value #QUIET_PERIOD} ticks.</li>
 *     <li><b>Maximum delay:</b> Processing is forced after
 *     {@value #MAX_DELAY} ticks since the first pending update, even if
 *     updates are still arriving.</li>
 * </ul>
 *
 * <p>
 * The dispatcher runs asynchronously on a repeating task and invokes
 * {@link ChunkState#apply()} for each pending state when flushing.
 *
 * <p>
 * This class is thread-safe and designed for concurrent submissions.
 */
public class ChunkChangeDispatcher implements Runnable {
    private final Map<ChunkKey, ChunkState> pending = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private BukkitTask task;
    private volatile long lastUpdateTick;
    private volatile long firstUpdateTick;
    private static final long QUIET_PERIOD = 2L;
    private static final long MAX_DELAY = 10L;

    /**
     * Creates a new dispatcher.
     *
     * @param plugin the owning plugin used to schedule the task
     */
    public ChunkChangeDispatcher(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the dispatcher task.
     *
     * <p>
     * If already running, the existing task is canceled and replaced.
     * The dispatcher will begin polling once per tick.
     */
    public void start() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this, 0L, 1L);
    }

    /**
     * Stops the dispatcher task.
     *
     * <p>
     * Pending updates are not automatically flushed when stopping.
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Executes the dispatcher tick.
     *
     * <p>
     * If there are pending updates, this method checks whether the quiet
     * period or maximum delay conditions have been met. If so, all pending
     * states are flushed and applied.
     */
    @Override
    public void run() {
        if (pending.isEmpty()) return;
        final long now = TickClock.getTick();
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

    /**
     * Submits a chunk state for deferred processing.
     *
     * <p>
     * If a state for the same {@link ChunkKey} already exists, it is replaced
     * by the new state.
     *
     * <p>
     * The dispatcher timing is updated to reflect the new submission.
     *
     * @param state the chunk state to submit
     */
    public void submit(@Nonnull final ChunkState state) {
        pending.put(state.getKey(), state);
        final long now = TickClock.getTick();
        if (pending.size() == 1) {
            firstUpdateTick = now;
        }
        lastUpdateTick = now;
    }
}

