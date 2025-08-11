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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
     * Set an dynamic animation for the buttons as an option.
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
        if(buttonAnimationData == null){
            cancel();
            return;
        }
        if(!buttonAnimationData.isSet()){
            cancel();
            return;
        }

        for (final MenuButton menuButton : menuUtility.getButtonsToUpdate()) {

            final Long timeLeft = getTimeWhenUpdatesButton(menuButton);
            if (timeLeft != null && timeLeft == -1) continue;

            if (timeLeft == null || timeLeft == 0)
                putTimeWhenUpdatesButtons(menuButton, counter + getTime(menuButton));
            else if (counter >= timeLeft && startUpdateButton(menuButton, buttonAnimationData)) {
                return;
            }
        }
        counter++;
    }

    private boolean startUpdateButton(MenuButton menuButton, ButtonAnimationData buttonAnimationData) {
        int pageNumber = buttonAnimationData.getPage();
        final MenuDataUtility<T> menuDataUtility = menuUtility.getMenuData(null,pageNumber);
        if (menuDataUtility == null) {
            cancel();
            return true;
        }
        final Set<Integer> itemSlots = getItemSlotsMap(menuDataUtility, menuButton);
        return updateButtonsData(buttonAnimationData, menuButton, menuDataUtility, itemSlots);
    }

    private boolean updateButtonsData(@Nonnull final ButtonAnimationData buttonAnimationData, final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Set<Integer> itemSlots) {
        if (!itemSlots.isEmpty()) {
            final Iterator<Integer> slotList = itemSlots.iterator();
            setButtons(buttonAnimationData,menuButton, menuDataUtility, slotList);
        }
        putTimeWhenUpdatesButtons(menuButton, counter + getTime(menuButton));
        return false;
    }

    private void setButtons(@Nonnull final ButtonAnimationData buttonAnimationData, final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Iterator<Integer> slotList) {
        Inventory menu = buttonAnimationData.getMenu();
        if(menu == null)
            return;

        while (slotList.hasNext()) {
            final Integer slot = slotList.next();

            final ButtonData<T> buttonData = menuDataUtility.getButton(slot);
            if (buttonData == null) continue;

            final ItemStack menuItem = getMenuItemStack(menuButton, buttonData, slot);
            final ButtonData<T> newButtonData = buttonData.copy(menuItem);

            menuDataUtility.putButton(slot, newButtonData);
            menu.setItem(slot, menuItem);
            slotList.remove();
        }
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
    public Long getTimeWhenUpdatesButton(final MenuButton menuButton) {
        return getTimeWhenUpdatesButtons().getOrDefault(menuButton.getId(), null);
    }

    /**
     * Sets the scheduled counter tick when the specified button should update next.
     *
     * @param menuButton the button to schedule for update
     * @param time       the counter tick at which update should occur
     */
    protected void putTimeWhenUpdatesButtons(final MenuButton menuButton, final Long time) {
        this.getTimeWhenUpdatesButtons().put(menuButton.getId(), time);
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

    @Nullable
    private ItemStack getMenuItemStack(final MenuButton menuButton, final ButtonData<T> cachedButtons, final int slot) {
        return this.menuUtility.getMenuItem(menuButton, cachedButtons, slot, menuButton.shouldUpdateButtons());
    }

    /**
     * Get all slots same menu button is connected too.
     *
     * @param menuDataMap the map with all slots and menu data
     * @param menuButton  the menu buttons you want to match with.
     * @return set of slots that match same menu button.
     */
    @Nonnull
    private Set<Integer> getItemSlotsMap(final MenuDataUtility<T> menuDataMap, final MenuButton menuButton) {
        final Set<Integer> slotList = new HashSet<>();
        if (menuDataMap == null) return slotList;

        for (int slot = 0; slot < inventorySize; slot++) {
            final ButtonData<T> addedButtons = menuDataMap.getButton(slot);
            if (addedButtons == null) continue;

            final MenuButton cacheMenuButton = addedButtons.getMenuButton();
            final MenuButton fillMenuButton = menuDataMap.getFillMenuButton(slot);
            final int menuButtonId = menuButton.getId();
            if (isValidButton(cacheMenuButton, fillMenuButton, menuButtonId))
                slotList.add(slot);
        }
        return slotList;
    }

    private boolean isValidButton(final  MenuButton cacheMenuButton,final  MenuButton fillMenuButton,final  int menuButtonId) {
        return (cacheMenuButton == null && fillMenuButton != null && fillMenuButton.getId() == menuButtonId) || (cacheMenuButton != null && Objects.equals(menuButtonId, cacheMenuButton.getId()));
    }
}
