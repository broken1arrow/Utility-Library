package org.broken.arrow.menu.library.holder.utility;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.utility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimateTitleTask<T> extends BukkitRunnable {

    private final MenuUtility<T> menuUtility;
    private int taskId;
    private volatile boolean cancelled = false;

    public AnimateTitleTask(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
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
        if(this.cancelled) return;

        Object text = menuUtility.getAnimateTitle().apply();
        if (text == null || (ServerVersion.atLeast(ServerVersion.V1_9) && this.isCancelled())) {
            this.cancelled = true;
            this.cancel();
            menuUtility.updateTitle();
            return;
        }
        if (!text.equals("")) {
            menuUtility.updateTitle(text);
        } else {
            this.cancelled = true;
            this.cancel();
        }
    }

}
