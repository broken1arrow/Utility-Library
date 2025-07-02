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

public class AnimateTitleTask<T> extends BukkitRunnable {

    private final Supplier<?> animateTitle;
    private final MenuUtility<T> menuUtility;
    private final Player player;
    private int taskId;
    private volatile boolean cancelled = false;

    public AnimateTitleTask(@Nonnull final MenuUtility<T> menuUtility, @Nonnull final Player player) {
        this(menuUtility.getAnimateTitle(), menuUtility, player);
    }


    public AnimateTitleTask(@Nullable final Supplier<?> animateTitle, @Nonnull final MenuUtility<T> menuUtility, @Nonnull final Player player) {
        this.animateTitle = animateTitle;
        this.menuUtility = menuUtility;
        this.player = player;
    }

    public void runTask(long delay) {
        taskId = runTaskTimerAsynchronously(menuUtility.getPlugin(), 1L, delay).getTaskId();
    }

    public boolean isRunning() {
        return taskId > 0 &&
                (Bukkit.getScheduler().isCurrentlyRunning(taskId) ||
                        Bukkit.getScheduler().isQueued(taskId));
    }

    public void stopTask() {
        if (this.isRunning()) {
            this.cancelled = true;
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
    }

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

    private boolean itShouldNotAnimateTitle(Object text) {
        if (player != null && (!player.isOnline() || hasNotInventoryWithTitle()))
            return true;

        return text == null || (ServerVersion.atLeast(ServerVersion.V1_9) && this.isCancelled());
    }

    private boolean hasNotInventoryWithTitle() {
        InventoryType inventoryType = player.getOpenInventory().getType();
        return inventoryType == InventoryType.PLAYER || inventoryType == InventoryType.CREATIVE;
    }

}
