package org.broken.arrow.menu.library.runnable;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
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

public class ButtonAnimation<T> extends BukkitRunnable {
    private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();
    private final MenuUtility<T> menuUtility;
    private final Inventory menu;
    private final int inventorySize;
    private int counter = 0;
    private int taskId;

    public ButtonAnimation(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
        this.menu = menuUtility.getMenu();
        this.inventorySize = menuUtility.getInventorySize();
    }

    public void runTask(long delay) {
        taskId = runTaskTimer(menuUtility.getPlugin(), 1L, delay).getTaskId();
    }

    public boolean isRunning() {
        return taskId > 0 &&
                (Bukkit.getScheduler().isCurrentlyRunning(taskId) ||
                        Bukkit.getScheduler().isQueued(taskId));
    }

    public void stopTask() {
        if (this.isRunning()) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
    }

    @Override
    public void run() {
        for (final MenuButton menuButton : menuUtility.getButtonsToUpdate()) {

            final Long timeLeft = getTimeWhenUpdatesButton(menuButton);
            if (timeLeft != null && timeLeft == -1) continue;

            if (timeLeft == null || timeLeft == 0)
                putTimeWhenUpdatesButtons(menuButton, counter + getTime(menuButton));
            else if (counter >= timeLeft) {
                int pageNumber = menuUtility.getPageNumber();
                final MenuDataUtility<T> menuDataUtility = menuUtility.getMenuData(pageNumber);
                if (menuDataUtility == null) {
                    cancel();
                    return;
                }
                final Set<Integer> itemSlots = getItemSlotsMap(menuDataUtility, menuButton);
                if (updateButtonsData(menuButton, menuDataUtility, itemSlots)) return;
            }
        }
        counter++;
    }

    private boolean updateButtonsData(final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Set<Integer> itemSlots) {
        if (!itemSlots.isEmpty()) {
            final Iterator<Integer> slotList = itemSlots.iterator();
            setButtons(menuButton, menuDataUtility, slotList);
        }
        putTimeWhenUpdatesButtons(menuButton, counter + getTime(menuButton));
        return false;
    }

    private void setButtons(final MenuButton menuButton, final MenuDataUtility<T> menuDataUtility, final Iterator<Integer> slotList) {
        while (slotList.hasNext()) {
            final Integer slot = slotList.next();

            int slotPageCalculated = menuUtility.getSlot(slot);
            final ButtonData<T> buttonData = menuDataUtility.getButton(slotPageCalculated);
            if (buttonData == null) continue;

            final ItemStack menuItem = getMenuItemStack(menuButton, buttonData, slot);
            final ButtonData<T> newButtonData = buttonData.copy(menuItem);

            menuDataUtility.putButton(slotPageCalculated, newButtonData);
            menu.setItem(slot, menuItem);
            slotList.remove();
        }
    }

    @Nonnull
    protected Map<Integer, Long> getTimeWhenUpdatesButtons() {
        return timeWhenUpdatesButtons;
    }

    @Nullable
    public Long getTimeWhenUpdatesButton(final MenuButton menuButton) {
        return getTimeWhenUpdatesButtons().getOrDefault(menuButton.getId(), null);
    }

    protected void putTimeWhenUpdatesButtons(final MenuButton menuButton, final Long time) {
        this.getTimeWhenUpdatesButtons().put(menuButton.getId(), time);
    }

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
            int menuSlot = this.menuUtility.getSlot(slot);
            final ButtonData<T> addedButtons = menuDataMap.getButtons().get(menuSlot);
            if (addedButtons == null) continue;

            final MenuButton cacheMenuButton = addedButtons.getMenuButton();
            final MenuButton fillMenuButton = menuDataMap.getFillMenuButton(menuSlot);
            final int menuButtonId = menuButton.getId();
            if ((cacheMenuButton == null && fillMenuButton != null && fillMenuButton.getId() == menuButtonId) || (cacheMenuButton != null && Objects.equals(menuButtonId, cacheMenuButton.getId())))
                slotList.add(slot);
        }
        return slotList;
    }

}
