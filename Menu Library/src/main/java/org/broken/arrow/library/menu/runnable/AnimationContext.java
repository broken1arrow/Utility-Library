package org.broken.arrow.library.menu.runnable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents data for button animations within a paginated menu.
 * <p>
 * Holds a reference to the menu inventory and the current page number.
 * This class helps track animation states linked to a specific inventory and page.
 * </p>
 */
public class AnimationContext {

    private final Inventory menu;
    private final Player player;
    private final int page;

    /**
     * Constructs a new {@code AnimationContext} instance.
     *
     * @param inventory the inventory menu associated with the animation; may be {@code null}.
     * @param player    The player that currently interact with the page.
     * @param page      the current page number, typically zero or positive, negative values mean unset.
     */
    public AnimationContext(@Nullable final Inventory inventory, @Nullable final Player player, final int page) {
        this.menu = inventory;
        this.player = player;
        this.page = page;
    }

    /**
     * Returns the menu inventory associated with this animation data.
     *
     * @return the inventory menu, or {@code null} if not set.
     */
    @Nullable
    public Inventory getMenu() {
        return menu;
    }

    /**
     * Returns the current page number this animation data refers to.
     *
     * @return the page number; may be negative if unset.
     */
    public int getPage() {
        return page;
    }

    /**
     * Retrieve the player that viewing the page.
     *
     * @return The player or null if not used or a player object is not needed.
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * Checks if this animation data has valid inventory and page set.
     *
     * @return {@code true} if the page is zero or positive and the menu is not {@code null}, otherwise {@code false}.
     */
    public boolean isSet() {
        return page >= 0 && menu != null;
    }

    /**
     * Compares this instance to another object for equality.
     * Two {@code AnimationContext} instances are equal if their
     * menu and page are equal.
     *
     * @param o the object to compare with
     * @return {@code true} if equal; {@code false} otherwise.
     */
    /**
     * Compares this instance to another object for equality.
     * Two {@code AnimationContext} instances are equal if their
     * menu, player, and page are equal.
     *
     * @param o the object to compare with
     * @return {@code true} if equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnimationContext that = (AnimationContext) o;
        return page == that.page &&
                Objects.equals(menu, that.menu) &&
                Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(menu);
        result = 31 * result + Objects.hashCode(player);
        result = 31 * result + page;
        return result;
    }

    @Override
    public String toString() {
        return "AnimationContext{" +
                "menu=" + menu +
                ", player=" + (player != null ? player.getName() : "null") +
                ", page=" + page +
                '}';
    }
}
