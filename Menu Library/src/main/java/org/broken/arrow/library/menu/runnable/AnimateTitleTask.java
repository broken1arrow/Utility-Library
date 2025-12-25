package org.broken.arrow.library.menu.runnable;

import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.utility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * A repeating asynchronous task that handles animating the title of a menu for a player.
 * <p>
 * This task periodically fetches a title from a supplied animation source and updates
 * the player's open menu title accordingly. The animation stops when the player is no longer online,
 * the menu is closed, or the animation supplier returns a terminating condition.
 * </p>
 *
 * @param <T> the type parameter used by the associated {@link MenuUtility}
 */
public class AnimateTitleTask<T> extends BukkitRunnable {

    private final Supplier<?> animateTitle;
    private final MenuUtility<T> menuUtility;
    private final Player player;
    private int taskId;
    private volatile boolean cancelled = false;

    /**
     * Constructs an AnimateTitleTask using the animate title supplier from the given {@link MenuUtility}
     * and the target player.
     *
     * @param menuUtility the utility class managing the menu, providing the animation supplier
     * @param player      the player whose menu title will be animated
     */
    public AnimateTitleTask(@Nonnull final MenuUtility<T> menuUtility, @Nonnull final Player player) {
        this(menuUtility.getAnimateTitle(), menuUtility, player);
    }

    /**
     * Constructs an AnimateTitleTask with the given animation supplier, menu utility, and player.
     *
     * @param animateTitle a supplier providing the current animated title; may return null or empty to stop animation
     * @param menuUtility  the utility class managing the menu
     * @param player       the player whose menu title will be animated
     */
    public AnimateTitleTask(@Nullable final Supplier<?> animateTitle, @Nonnull final MenuUtility<T> menuUtility, @Nonnull final Player player) {
        this.animateTitle = animateTitle;
        this.menuUtility = menuUtility;
        this.player = player;
    }

    /**
     * Starts this animation task to run asynchronously with the given delay between updates.
     *
     * @param delay the delay in ticks between each animation update
     */
    public void runTask(long delay) {
        taskId = runTaskTimerAsynchronously(menuUtility.getPlugin(), 1L, delay).getTaskId();
    }

    /**
     * Checks whether this animation task is currently running or queued.
     *
     * @return true if the task is running or queued, false otherwise
     */
    public boolean isRunning() {
        return taskId > 0 &&
                (Bukkit.getScheduler().isCurrentlyRunning(taskId) ||
                        Bukkit.getScheduler().isQueued(taskId));
    }

    /**
     * Stops this animation task if it is currently running.
     * Cancels scheduled updates and marks this task as cancelled.
     */
    public void stopTask() {
        if (this.isRunning()) {
            this.cancelled = true;
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
    }

    /**
     * Performs a single animation update cycle.
     * <p>
     * Fetches the current animated title from the supplier and updates
     * the player's menu title if appropriate. Stops the task if conditions
     * indicate the animation should end.
     * </p>
     */
    @Override
    public void run() {
        if (this.cancelled) return;

        Object text = this.animateTitle != null ? this.animateTitle.get(): null;
        if (itShouldNotAnimateTitle(text)) {
            this.cancelled = true;
            this.cancel();
            menuUtility.updateTitle(this.player);
            return;
        }
        if (text != null && !text.equals("")) {
            menuUtility.updateTitle(this.player, text);
        } else {
            this.cancelled = true;
            this.cancel();
        }
    }

    /**
     * Determines if the animation should stop based on player and text conditions.
     *
     * @param text the current animated title text to be displayed
     * @return true if animation should stop, false otherwise
     */
    private boolean itShouldNotAnimateTitle(Object text) {
        if (player != null && (!player.isOnline() || hasNotInventoryWithTitle()))
            return true;

        return text == null || (ServerVersion.atLeast(1.9) && this.isCancelled());
    }

    /**
     * Checks whether the player currently has an inventory open that supports title updates.
     * Player and creative inventory types are considered unsupported for animated titles.
     *
     * @return true if the player does not have a supported inventory with a title open; false otherwise
     */
    private boolean hasNotInventoryWithTitle() {
        InventoryType inventoryType = player.getOpenInventory().getType();
        return inventoryType == InventoryType.PLAYER || inventoryType == InventoryType.CREATIVE;
    }

}
