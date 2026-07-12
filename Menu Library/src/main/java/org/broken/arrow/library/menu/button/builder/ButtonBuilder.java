package org.broken.arrow.library.menu.button.builder;

import org.broken.arrow.library.logging.Validate;
import org.broken.arrow.library.menu.button.MenuButton;
import org.broken.arrow.library.menu.button.logic.ButtonUpdateAction;
import org.broken.arrow.library.menu.button.logic.ClickAction;
import org.broken.arrow.library.menu.button.logic.ClickContext;
import org.broken.arrow.library.menu.holder.HolderUtility;
import org.broken.arrow.library.menu.holder.MenuHolderPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A factory-style builder designed to streamline the construction of {@link MenuButton} instances.
 * <p>
 * This class eliminates the boilerplate of implementing anonymous {@code MenuButton} classes.
 * It provides a fluent API for defining click logic, item generation (both static and dynamic),
 * and animation timing.
 * </p>
 */
public class ButtonBuilder {
    private final ClickAction clickAction;
    private final HolderUtility<?> holderUtility;
    private Supplier<ItemStack> itemSupplier;
    private Function<Integer, ItemStack> function;
    private boolean shouldUpdateButtons;
    private int updateInterval = -1;

    /**
     * Create the button instance.
     *
     * @param holderUtility the menu holder instance, allows for automatic button refreshing.
     * @param clickAction   the handler containing the click logic.
     */
    private ButtonBuilder(@Nullable final HolderUtility<?> holderUtility, @Nonnull final ClickAction clickAction) {
        this.holderUtility = holderUtility;
        this.clickAction = clickAction;
    }

    /**
     * Initializes a new {@code ButtonBuilder} without an attached {@link HolderUtility}.
     * <p>
     * Use this factory method when you want to handle menu refreshes manually inside
     * your {@link ClickAction}.
     * </p>
     *
     * @param clickAction the handler containing the click logic.
     * @return a new instance of {@link ButtonBuilder}.
     */
    public static ButtonBuilder make(@Nonnull final ClickAction clickAction) {
        return new ButtonBuilder(null, clickAction);
    }

    /**
     * Initializes a new {@code ButtonBuilder} with an attached {@link HolderUtility}.
     * <p>
     * Use this factory method when you want the {@link ButtonUpdateAction} returned
     * by your click action to automatically trigger refreshes on the menu holder.
     * </p>
     *
     * @param holderUtility the menu holder instance, allows for automatic button refreshing.
     * @param clickAction   the handler containing the click logic.
     * @return a new instance of {@link ButtonBuilder}.
     */
    public static ButtonBuilder make(@Nullable final HolderUtility<?> holderUtility, @Nonnull final ClickAction clickAction) {
        return new ButtonBuilder(holderUtility, clickAction);
    }

    /**
     * Creates a pre-configured builder for a page-forward navigation button.
     *
     * @param pageMenu the paginated menu holder instance.
     * @return a configured ButtonBuilder ready for customization or building.
     */
    public static ButtonBuilder next(@Nonnull final MenuHolderPage<?> pageMenu) {
        return next(pageMenu, (player, clickType, clickContext) -> ButtonUpdateAction.NONE);
    }

    /**
     * Creates a pre-configured builder for a page-backward navigation button.
     *
     * @param pageMenu    the paginated menu holder instance.
     * @param clickAction the handler containing the click logic.
     * @return a configured ButtonBuilder ready for customization or building.
     */
    public static ButtonBuilder next(@Nonnull final MenuHolderPage<?> pageMenu, @Nonnull final ClickAction clickAction) {
        return new ButtonBuilder(pageMenu, (player, click, context) -> {
            clickAction.apply(player, click, context);
            pageMenu.nextPage();
            return ButtonUpdateAction.NONE;
        }).item(() -> new ItemStack(Material.ARROW));
    }


    /**
     * Creates a pre-configured builder for a page-backward navigation button.
     *
     * @param pageMenu the paginated menu holder instance.
     * @return a configured ButtonBuilder ready for customization or building.
     */
    public static ButtonBuilder previous(@Nonnull final MenuHolderPage<?> pageMenu) {
        return previous(pageMenu, (player, clickType, clickContext) -> ButtonUpdateAction.NONE);
    }

    /**
     * Creates a pre-configured builder for a page-backward navigation button.
     *
     * @param pageMenu    the paginated menu holder instance.
     * @param clickAction the handler containing the click logic.
     * @return a configured ButtonBuilder ready for customization or building.
     */
    public static ButtonBuilder previous(@Nonnull final MenuHolderPage<?> pageMenu, @Nonnull final ClickAction clickAction) {
        return new ButtonBuilder(pageMenu, (player, click, context) -> {
            clickAction.apply(player, click, context);
            pageMenu.previousPage();
            return ButtonUpdateAction.NONE;
        }).item(() -> new ItemStack(Material.ARROW));
    }


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
     * Constructs a {@link MenuButton} based on the current configuration.
     * <p>
     * If a {@link HolderUtility} was provided during construction, the resulting
     * button will automatically invoke menu updates based on the {@link ButtonUpdateAction}
     * returned by the click handler.
     * </p>
     *
     * @return a newly constructed {@link MenuButton}.
     */
    public MenuButton build() {
        return new MenuButton() {
            @Override
            public void onClickInsideMenu(@NonNull final Player player, @NonNull final ClickType click, @NonNull final ClickContext clickContext) {
                Validate.checkNotNull(clickAction, "Your click action can't be null for the button.");
                ButtonUpdateAction buttonUpdateAction = clickAction.apply(player, click, clickContext);
                if (holderUtility == null) return;

                switch (buttonUpdateAction) {
                    case ALL:
                        holderUtility.updateButtons();
                        break;
                    case THIS:
                        holderUtility.updateButton(this);
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
