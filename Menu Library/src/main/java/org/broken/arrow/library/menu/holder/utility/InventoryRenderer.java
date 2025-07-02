package org.broken.arrow.library.menu.holder.utility;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.menu.MenuUtility;
import org.broken.arrow.library.menu.builders.ButtonData;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class InventoryRenderer<T> {

    private final MenuUtility<T> utility;
    private final Logging logger = new Logging(MenuUtility.class);

    public InventoryRenderer(MenuUtility<T> utility) {
        this.utility = utility;
    }

    @Nonnull
    public Inventory redraw() {
        final int page = utility.getPageNumber();
        final int size = utility.getInventorySize();
        Inventory menu = utility.getMenu();

        if (menu == null || size > menu.getSize()) {
            menu = createInventory();
        }

        menu.clear();

        Map<Integer, ButtonData<T>> buttons = utility.getMenuButtons(page);
        if (buttons != null && !buttons.isEmpty()) {
            for (int i = 0; i < menu.getSize(); i++) {
                ButtonData<?> data = buttons.get(i);
                menu.setItem(i, data != null ? data.getItemStack() : null);
            }
        }

        return menu;
    }

    @Nonnull
    private Inventory createInventory() {
        String title = Optional.ofNullable(utility.getTitle()).map(Object::toString).orElse(" ");
        InventoryType type = utility.getInventoryType();
        int size = utility.getInventorySize();

        if (type != null) return Bukkit.createInventory(null, type, title);

        if (!(size == 5 || size % 9 == 0)) {
            this.logger.log(Level.WARNING, () -> "Wrong inventory size , you has put in " + size + " it need to be valid number.");
        }

        if (size == 5) return Bukkit.createInventory(null, InventoryType.HOPPER, title);

        return Bukkit.createInventory(null, size % 9 == 0 ? size : 9, title);
    }
}