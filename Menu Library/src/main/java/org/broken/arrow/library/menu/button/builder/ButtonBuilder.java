package org.broken.arrow.library.menu.button.builder;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.menu.button.MenuButton;
import org.broken.arrow.library.menu.button.logic.ButtonUpdateAction;
import org.broken.arrow.library.menu.button.logic.ClickAction;
import org.broken.arrow.library.menu.button.logic.ClickContext;
import org.broken.arrow.library.menu.holder.HolderUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A utility builder designed to streamline the creation of {@link MenuButton} instances.
 * <p>
 * This builder eliminates the boilerplate of creating anonymous {@code MenuButton}
 * classes manually. It allows for a clean, fluent API to configure a button's
 * visual item, click behavior, and animation update intervals.
 * </p>
 */
public class ButtonBuilder {
    private ClickAction clickAction;
    private Supplier<ItemStack> itemSupplier;
    private Function<Integer, ItemStack> function;
    private boolean shouldUpdateButtons;
    private int updateInterval = -1;

    /**
     * Sets a dynamic item generator for the button, which is evaluated every time the menu updates.
     *
     * @param itemSupplier the supplier generating the item to display; may be {@code null} for an invisible/empty button.
     * @return this builder instance for chaining.
     */
    public ButtonBuilder item(@Nullable final Supplier<ItemStack> itemSupplier) {
        this.itemSupplier = itemSupplier;
        return this;
    }

    /**
     * Sets a slot-aware dynamic item generator for the button, evaluated every time the menu updates.
     * <p>
     * This option provides the specific inventory slot index to the function, allowing for
     * position-dependent item generation (e.g., it should create the item on specific index, or you provide an array if stacks).
     * </p>
     *
     * @param function the function generating the item based on its slot index, may be {@code null} for an invisible/empty button.
     * @return this builder instance for chaining.
     */
    public ButtonBuilder item(@Nullable final Function<Integer, ItemStack> function) {
        this.function = function;
        return this;
    }

    /**
     * Sets the action to be executed when a player clicks this button.
     *
     * @param clickAction the handler containing the click logic.
     * @return this builder instance for chaining.
     */
    public ButtonBuilder onClick(@Nonnull final ClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    /**
     * Determines whether this button should trigger a visual update cycle for other buttons.
     *
     * @param shouldUpdateButtons {@code true} if this button triggers updates, {@code false} otherwise.
     * @return this builder instance for chaining.
     */
    public ButtonBuilder setShouldUpdateButtons(final boolean shouldUpdateButtons) {
        this.shouldUpdateButtons = shouldUpdateButtons;
        return this;
    }

    /**
     * Sets the tick interval at which this button should visually update or animate.
     *
     * @param updateInterval the delay in ticks between updates.
     * @return this builder instance for chaining.
     */
    public ButtonBuilder setUpdateInterval(final int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Builds a standard {@link MenuButton} without tying it to a specific menu utility.
     * <p>
     * Note: Buttons built this way will execute their click logic, but cannot
     * automatically trigger menu-wide visual updates. You have to manually invoke the
     * {@link HolderUtility#updateButton(MenuButton)} or {@link HolderUtility#updateButtons()}
     * inside the {@link #onClick(ClickAction)} method.
     * </p>
     *
     * @return a newly constructed {@link MenuButton}.
     */
    public MenuButton build() {
        return build(null);
    }

    /**
     * Builds a {@link MenuButton} linked to a specific {@link HolderUtility}.
     * <p>
     * Linking the utility allows the button to automatically process the
     * {@link ButtonUpdateAction} returned by its {@link ClickAction}, seamlessly
     * refreshing the current button or the entire menu.
     * </p>
     *
     * @param menuUtility the utility managing the menu this button belongs to; may be {@code null}.
     * @return a newly constructed {@link MenuButton}.
     */
    public MenuButton build(@Nullable final HolderUtility<?> menuUtility) {
        return new MenuButton() {
            @Override
            public void onClickInsideMenu(@NonNull final Player player, @NonNull final ClickType click, @NonNull final ClickContext clickContext) {
                Validate.checkNotNull(clickAction, "Your click action can't be null for the button.");
                ButtonUpdateAction buttonUpdateAction = clickAction.apply(player, click, clickContext);
                if (menuUtility == null) return;

                switch (buttonUpdateAction) {
                    case ALL:
                        menuUtility.updateButtons();
                        break;
                    case THIS:
                        menuUtility.updateButton(this);
                        break;
                    case NONE:
                        break;
                }
            }

            @Override
            public boolean shouldUpdateButtons() {
                return shouldUpdateButtons;
            }

            @Override
            public long setUpdateTime() {
                return updateInterval;
            }

            @Override
            public @Nullable ItemStack getItem(int slot) {
                if (function == null) return null;
                return function.apply(slot);
            }

            @Override
            public @Nullable ItemStack getItem() {
                if (itemSupplier == null) return null;
                return itemSupplier.get();
            }
        };
    }
}
