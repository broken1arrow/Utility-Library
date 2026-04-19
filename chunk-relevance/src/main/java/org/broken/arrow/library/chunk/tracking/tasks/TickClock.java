package org.broken.arrow.library.chunk.tracking.tasks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provides a global tick-based time source using the server tick loop.
 *
 * <p>
 * {@code TickTask} maintains a monotonically increasing counter that is
 * incremented once per server tick. This allows systems to track time
 * using ticks instead of real-world time, which is often preferable for
 * game-related logic where consistency with the server tick rate is required.
 *
 * <p>
 * The current tick value can be accessed via {@link #getTick()} and is
 * shared globally across all instances of this class.
 *
 * <p>
 * This class must be started using {@link #start()} to begin tracking ticks.
 * If not started, the tick value will not advance.
 *
 * <p>
 * Typical use cases include:
 * <ul>
 *     <li>Scheduling or delaying operations in tick units</li>
 *     <li>Measuring elapsed time relative to server ticks</li>
 *     <li>Coordinating batching or debounce logic (e.g. dispatchers)</li>
 * </ul>
 *
 * <p>
 * This class is thread-safe. The tick counter is stored in an
 * {@link java.util.concurrent.atomic.AtomicLong} to allow safe access
 * from asynchronous threads.
 */
public class TickClock implements Runnable {
    private static final AtomicLong tick = new AtomicLong();
    private final Plugin plugin;
    private BukkitTask task;

    /**
     * Returns the current global tick value.
     *
     * <p>
     * The value increases by one for each server tick after the task
     * has been started.
     *
     * @return the current tick count
     */
    public static long getTick() {
        return tick.get();
    }

    /**
     * Creates a new tick task.
     *
     * @param plugin the owning plugin used to schedule the task
     */
    public TickClock(@Nonnull final Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the tick counter.
     *
     * <p>
     * If already running, the existing task is canceled and restarted.
     * The counter is incremented once per server tick (1 tick interval).
     */
    public void start() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this, 0L, 1L);
    }

    /**
     * Stops the tick counter.
     *
     * <p>
     * Stopping this task prevents further increments of the global tick value.
     * The current tick value is retained and can still be queried.
     */
    public void stop() {
        if (this.task == null) {
            return;
        }
        this.task.cancel();
        this.task = null;
    }

    /**
     * Advances the tick counter by one.
     *
     * <p>
     * This method is invoked automatically by the scheduler once per tick
     * and should not be called manually.
     */
    @Override
    public void run() {
        tick.incrementAndGet();
    }

}
