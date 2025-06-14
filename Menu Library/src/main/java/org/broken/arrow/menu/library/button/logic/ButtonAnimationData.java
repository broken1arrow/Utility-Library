package org.broken.arrow.menu.library.button.logic;

import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;
import java.util.Objects;


public class ButtonAnimationData {

    private final Inventory menu;
    private final int page;


    public ButtonAnimationData(@Nullable final Inventory inventory, final int page) {
        this.menu = inventory;
        this.page = page;
    }

    @Nullable
    public Inventory getMenu() {
        return menu;
    }

    public int getPage() {
        return page;
    }

    public boolean isSet() {
        return page >= 0 && menu != null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ButtonAnimationData that = (ButtonAnimationData) o;
        return page == that.page && Objects.equals(menu, that.menu);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(menu);
        result = 31 * result + page;
        return result;
    }

    @Override
    public String toString() {
        return "ButtonAnimationData{" +
                "menu=" + menu +
                ", page=" + page +
                '}';
    }
}
