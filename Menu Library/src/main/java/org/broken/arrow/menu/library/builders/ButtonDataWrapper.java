package org.broken.arrow.menu.library.builders;

import org.broken.arrow.menu.library.button.MenuButton;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ButtonDataWrapper<T> {
    private final MenuButton menuButton;
    private  ItemStack itemStack;
    private  boolean isFillButton;
    private  T object;

    public ButtonDataWrapper(@Nonnull final MenuButton menuButton) {
        this.menuButton = menuButton;
    }


    public MenuButton getMenuButton() {
        return menuButton;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ButtonDataWrapper<T> setItemStack(@Nullable final ItemStack itemStack) {
        this.itemStack = itemStack;
        return this;
    }


    public boolean isFillButton() {
        return isFillButton;
    }

    public ButtonDataWrapper<T> setIsFillButton(final boolean fillButton) {
        isFillButton = fillButton;
        return this;
    }

    public T getObject() {
        return object;
    }

    public ButtonDataWrapper<T> setObject(@Nullable final T object) {
        this.object = object;
        return this;
    }

    public ButtonData<T> build() {
        return new ButtonData<>(this.menuButton, this.itemStack, this.isFillButton, this.object);
    }

}
