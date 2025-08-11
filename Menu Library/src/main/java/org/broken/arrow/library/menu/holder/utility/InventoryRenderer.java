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

/**
 * A utility class responsible for rendering and redrawing Bukkit inventories
 * based on a provided {@link MenuUtility} instance.
 *
 * <p>This class handles inventory creation with correct size and type,
 * clearing and setting items according to the current page of buttons.</p>
 *
 * @param <T> The type parameter used by the associated {@link MenuUtility}.
 */
public class InventoryRenderer<T> {

    private final MenuUtility<T> utility;
    private final Logging logger = new Logging(MenuUtility.class);

    /**
     * Creates a new InventoryRenderer linked to the specified MenuUtility.
     *
     * @param utility The MenuUtility instance providing inventory data and buttons.
     */
    public InventoryRenderer(MenuUtility<T> utility) {
        this.utility = utility;
    }

    /**
     * Redraws the inventory for the current page.
     *
     * <p>This method clears the existing inventory and populates it with
     * button items from the current page. If the inventory is missing or its
     * size is smaller than required, a new inventory will be created.</p>
     *
     * @return The updated {@link Inventory} instance ready to be displayed.
     */
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
                ButtonData<T> data = buttons.get(i);
                menu.setItem(i, data != null ? data.getItemStack() : null);
            }
        }

        return menu;
    }

    /**
     * Creates a new Bukkit inventory with the appropriate type and size
     * based on the {@link MenuUtility} configuration.
     *
     * <p>If the inventory type is specified, it will be used directly.
     * Otherwise, the size will be validated: if it is 5, a hopper inventory
     * is created; if it is a multiple of 9, a chest inventory is created.
     * If the size is invalid, a warning is logged and a default size of 9
     * slots instead.</p>
     *
     * @return A newly created {@link Inventory} instance.
     */
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