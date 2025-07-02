package org.broken.arrow.library.menu.builders;

import org.broken.arrow.library.menu.button.MenuButton;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * Represents a menu button along with its current properties,
 * the display item, and the associated data object provided by the user.
 *
 * @param <T> the type of the data object linked to the button's display item.
 */
public class ButtonData<T> {

    private final ItemStack itemStack;
    private final MenuButton menuButtonLinkedToThisItem;
    private final int id;
    private final boolean isFillButton;
    private final T object;

    /**
     * Constructs a new {@link ButtonData} instance with the specified item and menu button.
     * Defaults to a non-fill button.
     *
     * @param itemStack  the item to display, or {@code null} if none.
     * @param menuButton the menu button this item is linked to.
     * @param object     optional data object related to the button, or {@code null}.
     */
    public ButtonData(@Nullable final ItemStack itemStack, final MenuButton menuButton, @Nullable final T object) {
        this(menuButton, itemStack, false, object);
    }

    /**
     * Constructs a new {@link ButtonData} instance with full customization.
     *
     * @param menuButton   the menu button this item is linked to, or {@code null}.
     * @param itemStack    the item to be rendered in the inventory slot, or {@code null}.
     * @param isFillButton set to {@code true} if this is a fill button that may span multiple slots.
     * @param object       optional data object associated with this button, or {@code null}.
     */
    public ButtonData(@Nullable final MenuButton menuButton, @Nullable final ItemStack itemStack, final boolean isFillButton, @Nullable final T object) {
        this.itemStack = itemStack;
        this.menuButtonLinkedToThisItem = menuButton;
        this.id = menuButton != null ? menuButton.getId() : -1;
        this.isFillButton = isFillButton;
        this.object = object;
    }


    /**
     * the itemstack you want to be displayed in the menu.
     *
     * @return the itemstack you added in the menu.
     */
    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * The button linked to this item.
     *
     * @return menuButton.
     */
    @Nullable
    public MenuButton getMenuButton() {
        return menuButtonLinkedToThisItem;
    }

    /**
     * Get the unique id for this button.
     *
     * @return the id or -1 if not set.
     */

    public int getId() {
        return id;
    }

    /**
     * get the data linked to this item.
     *
     * @return object data you want this item contains.
     */
    public T getObject() {
        return object;
    }

    /**
     * Checks whether this is a fill button that spans one or multiple inventory slots.
     *
     * @return {@code true} if this button is used as a fill button; {@code false} otherwise.
     */
    public boolean isFillButton() {
        return isFillButton;
    }

    /**
     * Create a copy with your new itemstack set.
     *
     * @param itemStack the itemstack to replace the old menu button item with.
     * @return new instance of this class where it copy the old button instance and the object/fill item.
     */
    public ButtonData<T> copy(final ItemStack itemStack) {
        return new ButtonData<>(this.menuButtonLinkedToThisItem, itemStack, this.isFillButton, this.object);
    }
}

