package org.broken.arrow.library.menu.runnable;

import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.builders.ButtonData;
import org.broken.arrow.library.menu.builders.MenuDataUtility;
import org.broken.arrow.library.menu.button.MenuButton;
import org.broken.arrow.library.menu.button.logic.ButtonAnimationData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Handles the periodic animation updates of buttons within a menu inventory.
 * <p>
 * This task runs on a timer and updates specific menu buttons based on configured update times,
 * animating button states or appearances dynamically.
 * </p>
 *
 * @param <T> the generic type associated with the {@link MenuUtility} used for menu management
 */
public class ButtonAnimation<T> extends BukkitRunnable {
    private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();
    private Map<Integer, ButtonAnimationGroup> itemSlots = new HashMap<>();
    private final MenuUtility<T> menuUtility;
    private final int inventorySize;
    private Supplier<ButtonAnimationData> dataSupplier;
    private int counter = 0;
    private int taskId;

    /**
     * Creates a ButtonAnimation tied to a specific {@link MenuUtility}.
     * Initializes the animation data supplier to provide current menu and page information.
     *
     * @param menuUtility the menu utility managing the menu and buttons to animate
     */
    public ButtonAnimation(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
        this.dataSupplier = () -> new ButtonAnimationData(menuUtility.getMenu(), menuUtility.getPageNumber());
        this.inventorySize = menuUtility.getInventorySize();
        this.checkMenuUpdates(menuUtility);
    }

    /**
     * Starts the animation task with a fixed delay between each run cycle.
     *
     * @param delay the delay in ticks between each animation update
     */
    public void runTask(long delay) {
        taskId = runTaskTimer(menuUtility.getPlugin(), 1L, delay).getTaskId();
    }

    /**
     * Check if the task is currently running.
     *
     * @return Returns {@code true} if the task is running.
     */
    public boolean isRunning() {
        return taskId > 0 &&
                (Bukkit.getScheduler().isCurrentlyRunning(taskId) ||
                        Bukkit.getScheduler().isQueued(taskId));
    }

    /**
     * Stops this animation task if it is running.
     * Cancels the scheduled task to cease button updates.
     */
    public void stopTask() {
        if (this.isRunning()) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
    }

    /**
     * Set a dynamic custom animation for the buttons as an option.
     *
     * @param dataSupplier the custom task to run instead of the default one.
     */
    public void setDataForAnimation(@Nonnull final Supplier<ButtonAnimationData> dataSupplier) {
        if (dataSupplier.get() != null)
            this.dataSupplier = dataSupplier;
    }

    /**
     * Called on each run cycle of the task to update buttons that are due for animation.
     * Cancels the task if animation data is unavailable or invalid.
     */
    @Override
    public void run() {
        ButtonAnimationData buttonAnimationData = this.dataSupplier.get();
        if (buttonAnimationData == null || !buttonAnimationData.isSet()) {
            cancel();
            return;
        }

        int pageNumber = buttonAnimationData.getPage();
        final MenuDataUtility<T> menuDataUtility = menuUtility.getMenuData(null, pageNumber);
        if (menuDataUtility == null) {
            cancel();
            return;
        }
        if (itemSlots.isEmpty()) {
            final Map<Integer, ButtonData<T>> buttons = menuDataUtility.getButtonsToUpdate();
            itemSlots = this.getItemSlotsMap(menuDataUtility, buttons);
        }

        for (final Map.Entry<Integer, ButtonAnimationGroup> dataEntry : itemSlots.entrySet()) {
            final MenuButton menuButton = dataEntry.getValue().getMenuButton();
            final Long timeLeft = getUpdateTime(menuButton);
            if (timeLeft != null && timeLeft == -1) continue;

            if (timeLeft == null || timeLeft == 0)
                updateScheduledTime(menuButton);
            else if (counter >= timeLeft && startUpdateButton(buttonAnimationData, dataEntry, menuDataUtility)) {
                return;
            }
        }
        counter++;
    }


    /**
     * Gets the mapping of button IDs to the counter tick at which they should next update.
     *
     * @return the map tracking update timings per button ID
     */
    @Nonnull
    protected Map<Integer, Long> getTimeWhenUpdatesButtons() {
        return timeWhenUpdatesButtons;
    }

    /**
     * Gets the scheduled counter tick when the specified button should update next.
     *
     * @param menuButton the button to query for update timing
     * @return the counter tick for next update, or null if none scheduled
     */
    @Nullable
    public Long getUpdateTime(final MenuButton menuButton) {
        return this.getTimeWhenUpdatesButtons().get(menuButton.getId());
    }

    /**
     * Sets the scheduled counter tick when the specified button should update next.
     *
     * @param menuButton the button to schedule for update
     */
    public void updateScheduledTime(final MenuButton menuButton) {
        this.getTimeWhenUpdatesButtons().put(menuButton.getId(), this.counter + getTime(menuButton));
    }

    /**
     * Sets the scheduled counter tick when the specified button should update next.
     *
     * @param menuButton  the button to schedule for update
     * @param carriedTime set time to use instead of current time or will use defoult time.
     */
    public void updateScheduledTime(final MenuButton menuButton, final long carriedTime) {
        this.getTimeWhenUpdatesButtons().compute(menuButton.getId(), (id, existingTime) -> {
            if (existingTime != null) {
                return existingTime;
            }
            if (carriedTime > 0) {
                return carriedTime;
            }
            return this.counter + getTime(menuButton);
        });
    }

    /**
     * Remove the set button time.
     *
     * @param menuButton the button to remove.
     */
    public void removeUpdateTime(@Nonnull final MenuButton menuButton) {
        this.getTimeWhenUpdatesButtons().remove(menuButton.getId());
    }

    /**
     * Determines the update interval for the specified button.
     * Returns the button's own configured update time, or the default from {@link MenuUtility} if none set.
     *
     * @param menuButton the button whose update time is requested
     * @return the update interval in ticks, or -1 for never update
     */
    private long getTime(final MenuButton menuButton) {
        if (menuButton.setUpdateTime() == -1) return this.menuUtility.getUpdateTime();
        return menuButton.setUpdateTime();
    }

    private boolean startUpdateButton(final ButtonAnimationData buttonAnimationData, final Map.Entry<Integer, ButtonAnimationGroup> dataEntry, final MenuDataUtility<T> menuDataUtility) {
        setButtons(buttonAnimationData, menuDataUtility, dataEntry);
        return false;
    }

    private void setButtons(@Nonnull final ButtonAnimationData buttonAnimationData, @Nonnull final MenuDataUtility<T> menuDataUtility, @Nonnull final Map.Entry<Integer, ButtonAnimationGroup> dataEntry) {
        final Inventory menu = buttonAnimationData.getMenu();
        final ButtonAnimationGroup entryValue = dataEntry.getValue();
        final MenuButton menuButton = entryValue.getMenuButton();

        if (menu == null)
            return;

        for (Integer slot : entryValue.getSlots()) {
            final ButtonData<T> buttonData = menuDataUtility.getButton(slot);
            if (buttonData == null) continue;

            final ItemStack menuItem = getMenuItemStack(menuButton, buttonData, slot);
            menu.setItem(slot, menuItem);
            menuDataUtility.updateButton(slot, menuButton, dataWrapper -> dataWrapper.setItemStack(menuItem));
        }
        this.updateScheduledTime(menuButton);
    }

    @Nullable
    private ItemStack getMenuItemStack(final MenuButton menuButton, final ButtonData<T> cachedButtons, final int slot) {
        return this.menuUtility.getMenuItem(menuButton, cachedButtons, slot, menuButton.shouldUpdateButtons());
    }

    private void checkMenuUpdates(MenuUtility<T> menuUtility) {
        menuUtility.addListener(() -> {
            if (this.dataSupplier == null) return;

            ButtonAnimationData buttonAnimationData = this.dataSupplier.get();
            if (buttonAnimationData == null || !buttonAnimationData.isSet()) {
                return;
            }
            int pageNumber = buttonAnimationData.getPage();
            final MenuDataUtility<T> menuDataUtility = this.menuUtility.getMenuData(null, pageNumber);
            if (menuDataUtility == null) {
                return;
            }
            final Map<Integer, ButtonData<T>> buttons = menuDataUtility.getButtonsToUpdate();
            this.itemSlots = this.getItemSlotsMap(menuDataUtility, buttons);
        });
    }


    /**
     * Get all slots same menu button is connected too.
     *
     * @param menuDataUtility the instance of the cached with all slots and menu data
     * @param buttons         the menu buttons you want to match with.
     * @return map of slots that match same menu button.
     */
    private Map<Integer, ButtonAnimationGroup> getItemSlotsMap(@Nonnull final MenuDataUtility<T> menuDataUtility, @Nonnull final Map<Integer, ButtonData<T>> buttons) {
        final Map<Integer, ButtonAnimationGroup> slotMap = new LinkedHashMap<>();
        if (buttons.isEmpty()) return slotMap;

        for (int slot = 0; slot < inventorySize; slot++) {
            final ButtonData<T> buttonData = buttons.get(slot);
            if (buttonData == null) continue;

            MenuButton resolvedButton = buttonData.getMenuButton();
            final long carriedScheduledTime = getCurrentTime(resolvedButton);
            final boolean refreshButtons = this.menuUtility.isFullyRefreshButtons();
            if (refreshButtons) {
                if (resolvedButton != null) {
                    this.removeUpdateTime(resolvedButton);
                }
                resolvedButton = this.menuUtility.getFillSpace().contains(slot)
                        ? this.menuUtility.getFillButtonAt(slot)
                        : null;

                if (resolvedButton == null)
                    resolvedButton = this.menuUtility.getButtonAt(slot);
            } else if (resolvedButton == null) {
                resolvedButton = menuDataUtility.getFillMenuButton(slot);
            }
            if (resolvedButton == null) continue;
            final int buttonID = resolvedButton.getId();
            final MenuButton finalResolvedButton = resolvedButton;

            this.updateScheduledTime(resolvedButton, carriedScheduledTime);
            slotMap.computeIfAbsent(buttonID, k -> new ButtonAnimationGroup(finalResolvedButton)).add(slot);
            menuDataUtility.updateButton(slot, finalResolvedButton, (dataWrapper) -> dataWrapper.setMenuButton(finalResolvedButton));
        }
        return slotMap;
    }


    private long getCurrentTime(MenuButton resolvedButton) {
        long time;
        if (resolvedButton == null) {
            time = 0;
        } else {
            Long updateTime = getUpdateTime(resolvedButton);
            if (updateTime != null) {
                time = updateTime;
            } else {
                time = 0;
            }
        }
        return time;
    }


    private static class ButtonAnimationGroup {
        List<Integer> slots = new ArrayList<>();
        MenuButton menuButton;

        public ButtonAnimationGroup(@Nonnull final MenuButton menuButton) {
            this.menuButton = menuButton;
        }

        public void add(int slot) {
            slots.add(slot);
        }

        @Nonnull
        public List<Integer> getSlots() {
            return slots;
        }

        @Nonnull
        public MenuButton getMenuButton() {
            return menuButton;
        }
    }
}
