package org.broken.arrow.library.chunk.tracking.tasks;

import org.broken.arrow.library.chunk.tracking.utility.ChunkState;
import org.broken.arrow.library.serialize.utility.converters.world.ChunkKey;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dispatches {@link ChunkState} updates asynchronously using a batched, debounced approach.
 *
 * <p>
 * This dispatcher collects submitted {@link ChunkState} instances and processes them after a short
 * delay. This allows multiple rapid updates to the same chunk (such as those generated during
 * player teleports or joins) to be coalesced into a single operation, drastically reducing system load.
 * </p>
 *
 * <p>
 * Updates are stored per {@link ChunkKey}. If multiple updates are submitted for the same chunk key
 * before the batch flushes, only the most recent state is retained and applied.
 * </p>
 *
 * <p>
 * Dispatch timing is controlled by two conditions:
 * <ul>
 * <li><b>Quiet period:</b> A flush occurs if no new updates have been received for {@value #QUIET_PERIOD} ticks.</li>
 * <li><b>Maximum delay:</b> A flush is forced after {@value #MAX_DELAY} ticks have passed since the first pending update arrived, ensuring updates are not starved during prolonged bursts.</li>
 * </ul>
 * </p>
 *
 * <p>
 * The dispatcher runs asynchronously on a repeating task and invokes {@link ChunkState#apply()}
 * for each unique pending state when flushing. This class is fully thread-safe.
 * </p>
 */
public class ChunkChangeDispatcher implements Runnable {
    private final Map<ChunkKey, ChunkState> pending = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private BukkitTask task;
    private final AtomicLong lastUpdateTick = new AtomicLong(0);
    private final AtomicLong firstUpdateTick = new AtomicLong(-1);
    private static final long QUIET_PERIOD = 2L;
    private static final long MAX_DELAY = 10L;

    /**
     * Creates a new dispatcher.
     *
     * @param plugin The owning plugin used to schedule the asynchronous task.
     */
    public ChunkChangeDispatcher(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the asynchronous dispatcher task.
     * <p>
     * If the task is already running, the existing task is canceled and replaced.
     * The dispatcher polls for changes once per tick.
     * </p>
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
     * <strong>Note:</strong> Any pending updates currently in the queue will not be auto-flushed
     * when stopping.
     * </p>
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Executes the dispatcher tick, checking if timing thresholds have been met.
     *
     * <p>
     * If there are pending updates, this method determines whether the quiet period or
     * maximum delay conditions have been satisfied. If either condition is met, the pending
     * states are flushed and applied asynchronously.
     * </p>
     */
    @Override
    public void run() {
        if (pending.isEmpty()) return;
        final long now = TickClock.getTick();
        final boolean quiet = (now - lastUpdateTick.get()) >= QUIET_PERIOD;

        long firstTick = firstUpdateTick.get();
        final boolean forced = firstTick != -1 && (now - firstTick) >= MAX_DELAY;

        if (!quiet && !forced) {
            return;
        }

        final Map<ChunkKey, ChunkState> current = new HashMap<>(pending);
        pending.clear();
        firstUpdateTick.set(-1);

        for (ChunkState state : current.values()) {
            state.apply();
        }
    }

    /**
     * Submits a chunk state for deferred, batched processing.
     *
     * <p>
     * If a state for the same {@link ChunkKey} is already queued, it will be overwritten
     * by this newer state. Submission updates the internal timestamps to maintain the
     * quiet period and maximum delay windows.
     * </p>
     *
     * @param state The chunk state to submit for batching.
     */
    public void submit(@Nonnull final ChunkState state) {
        pending.put(state.getKey(), state);
        final long now = TickClock.getTick();
        firstUpdateTick.compareAndSet(-1, now);
        lastUpdateTick.set(now);
    }
}

