package org.broken.arrow.menu.library.runnable;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.builders.ButtonData;
import org.broken.arrow.menu.library.builders.MenuDataUtility;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.button.logic.ButtonAnimationData;
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

public class ButtonAnimation<T> extends BukkitRunnable {
    private final Map<Integer, Long> timeWhenUpdatesButtons = new HashMap<>();
    private final MenuUtility<T> menuUtility;
    private final int inventorySize;
    private Supplier<ButtonAnimationData> dataSupplier;
    private int counter = 0;
    private int taskId;


    public ButtonAnimation(MenuUtility<T> menuUtility) {
        this.menuUtility = menuUtility;
        this.dataSupplier = () -> new ButtonAnimationData(menuUtility.getMenu(), menuUtility.getPageNumber());
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

    public void setDataForAnimation(@Nonnull final Supplier<ButtonAnimationData> dataSupplier) {
        if (dataSupplier.get() != null)
            this.dataSupplier = dataSupplier;
    }

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
            else if (counter >= timeLeft) {
                if (startUpdateButton(menuButton, buttonAnimationData)) return;
            }
        }
        counter++;
    }

    private boolean startUpdateButton(MenuButton menuButton, ButtonAnimationData buttonAnimationData) {
        int pageNumber = buttonAnimationData.getPage();
        final MenuDataUtility<T> menuDataUtility = menuUtility.getMenuData(pageNumber);
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
