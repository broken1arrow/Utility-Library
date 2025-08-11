package org.broken.arrow.library.menu.builders;

import org.broken.arrow.library.menu.button.MenuButton;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A wrapper for configuring {@link ButtonData} in a controlled manner.
 * <p>
 * This class allows internal customization of button properties without exposing
 * unnecessary complexity or mutation options to the end user.
 * It helps decouple setup logic from direct {@link ButtonData} construction.
 * </p>
 *
 * @param <T>  The type of data being rendered as the object connected to the item.
 */
public class ButtonDataWrapper<T> {
    private final MenuButton menuButton;
    private  ItemStack itemStack;
    private  boolean isFillButton;
    private  T object;

    /**
     * Constructs a new {@code ButtonDataWrapper} for the specified menu button.
     *
     * @param menuButton the menu button associated with this wrapper; must not be {@code null}.
     */
    public ButtonDataWrapper(@Nonnull final MenuButton menuButton) {
        this.menuButton = menuButton;
    }

    /**
     * Gets the {@link MenuButton} associated with this wrapper.
     *
     * @return the menu button; never {@code null}.
     */
    public MenuButton getMenuButton() {
        return menuButton;
    }

    /**
     * Gets the current {@link ItemStack} set to display for this button.
     *
     * @return the item stack, or {@code null} if none is set.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Sets the item to display for the player viewing the menu.
     *
     * @param itemStack the item stack to set (maybe {@code null}).
     * @return the current wrapper instance for chaining.
     */
    public ButtonDataWrapper<T> setItemStack(@Nullable final ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }

    /**
     * Returns whether this button is designated as a fill button.
     *
     * @return {@code true} if this is a fill button, {@code false} otherwise.
     */
    public boolean isFillButton() {
        return isFillButton;
    }

    /**
     * Marks whether this button is used as a fill button (either for fill slots or a single fill slot).
     *
     * @param fillButton set to {@code true} if this is a fill button; default is {@code false}.
     * @return the current wrapper instance for method chaining.
     */
    public ButtonDataWrapper<T> setFillButton(final boolean fillButton) {
        isFillButton = fillButton;
        return this;
    }

    /**
     * Gets the data object associated with this button.
     *
     * @return the associated data object, or {@code null} if none is set.
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the data object associated with this button.
     *
     * @param object the object to associate, may be {@code null}
     * @return the current wrapper for chaining
     */
    public ButtonDataWrapper<T> setObject(@Nullable final T object) {
        this.object = object;
        return this;
    }

    /**
     * Builds and returns an immutable {@link ButtonData} instance using the configured values.
     *
     * @return a constructed {@link ButtonData} object
     */
    public ButtonData<T> build() {
        return new ButtonData<>(this.menuButton, this.itemStack, this.isFillButton, this.object);
    }

}
