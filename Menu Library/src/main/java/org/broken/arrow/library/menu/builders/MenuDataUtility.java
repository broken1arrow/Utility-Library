package org.broken.arrow.library.menu.builders;

import org.broken.arrow.library.menu.button.MenuButton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A utility class for managing menu data such as buttons and their fill behavior within specific slots.
 * <p>
 * This class stores both explicitly placed buttons and optionally applied fill buttons,
 * particularly useful when populating a menu from a dataset like a list or map.
 * It allows configuring buttons on a per-slot basis and provides support for a shared fill button
 * or per-slot fill buttons when needed.
 *
 * @param <T> the type associated with each button, used for context-specific data.
 */
public final class MenuDataUtility<T> {

	private final Map<Integer, ButtonData<T>> buttons = new HashMap<>();
	private Map<Integer, MenuButton> fillMenuButtons;
	private MenuButton fillMenuButton;

	/**
	 * Adds a button to the specified slot and uses a matching fill button for that slot if available.
	 *
	 * @param slot       the slot to place the button in
	 * @param buttonData the data for the button
	 * @return the current instance for chaining
	 */
	public MenuDataUtility<T> putButton(final int slot, @Nonnull final ButtonData<T> buttonData) {
		return putButton(slot, buttonData, this.getFillMenuButton(slot));
	}

	/**
	 * Adds a button to the specified slot with an optional custom fill button.
	 * <p>
	 * If the provided fill button is {@code null}, it is ignored.
	 * If it matches the {@link #fillMenuButton}, it will not be added to the per-slot fill map.
	 * Otherwise, it is stored in the {@link #fillMenuButtons} map.
	 * </p>
	 *
	 * @param slot           the slot to place the button in
	 * @param buttonData     the data for the button
	 * @param fillMenuButton an optional fill button for the given slot
	 * @return the current instance for chaining
	 */
	public MenuDataUtility<T> putButton(final int slot, @Nonnull final ButtonData<T> buttonData, @Nullable final MenuButton fillMenuButton) {
		buttons.put(slot, buttonData);
		if (fillMenuButton != null) {
			if (this.getFillMenuButton() != null && this.getFillMenuButton().getId() != fillMenuButton.getId()) {
				if (this.fillMenuButtons == null)
					this.fillMenuButtons = new HashMap<>();
				this.fillMenuButtons.put(slot, fillMenuButton);
			} else
				return this.setFillMenuButton(fillMenuButton);
		}
		return this;
	}

	/**
	 * Adds a {@link MenuButton} to the specified slot and allows customization of its associated {@link ButtonData} via a {@link ButtonDataWrapper}.
	 * <p>
	 * This is useful when dynamically constructing buttons with inline configuration logic.
	 * </p>
	 * <p>
	 * If {@link ButtonDataWrapper#isFillButton()} is {@code true}, the method checks whether the provided button should be treated
	 * as the shared {@link #fillMenuButton} or stored in the per-slot {@link #fillMenuButtons} map:
	 * </p>
	 * <ul>
	 *   <li>If the shared {@code fillMenuButton} is {@code null} or matches the provided button, it is set as the shared fill button.</li>
	 *   <li>Otherwise, the provided button is added to the {@code fillMenuButtons} map for the specific slot.</li>
	 * </ul>
	 *
	 * @param slot        the slot to place the button in.
	 * @param menuButton  the button to display.
	 * @param buttonData  a consumer to configure the {@link ButtonDataWrapper} for this button.
	 * @return the current instance for chaining.
	 */
	public MenuDataUtility<T> putButton(final int slot, @Nonnull  final MenuButton menuButton ,@Nonnull final Consumer<ButtonDataWrapper<T>> buttonData) {
		final ButtonDataWrapper<T> buttonDataWrapper = new ButtonDataWrapper<>(menuButton);
		buttonData.accept(buttonDataWrapper);
		buttons.put(slot, buttonDataWrapper.build());

		if (buttonDataWrapper.isFillButton()) {
			if (this.getFillMenuButton() != null && this.getFillMenuButton().getId() != menuButton.getId()) {
				if (this.fillMenuButtons == null)
					this.fillMenuButtons = new HashMap<>();
				this.fillMenuButtons.put(slot, menuButton);
			} else
				return this.setFillMenuButton(menuButton);
		}
		return this;
	}

	/**
	 * Sets a shared fill menu button that can be reused across all slots unless overridden.
	 *
	 * @param fillMenuButton the fill menu button to use as default
	 * @return the current instance for chaining
	 */
	public MenuDataUtility<T> setFillMenuButton(final MenuButton fillMenuButton) {
		this.fillMenuButton = fillMenuButton;
		return this;
	}

	/**
	 * Returns the shared fill menu button if it matches the given button.
	 *
	 * @param button the button to compare with the shared fill menu button
	 * @return the shared fill menu button if it's the same, otherwise {@code null}
	 */
	@Nullable
	public MenuButton getSimilarFillMenuButton(@Nullable final MenuButton button) {
		final MenuButton menuButton = this.fillMenuButton;
		if (menuButton == null || button == null) return null;
		if (menuButton.getId() != button.getId()) return null;

		return menuButton;
	}

	/**
	 * Returns the fill menu button for a specific slot, matching the provided button.
	 *
	 * @param menuButton the button to match against
	 * @return the matching fill menu button for that slot, or {@code null} if not found
	 */
	@Nullable
	public MenuButton getFillMenuButton(@Nonnull MenuButton menuButton) {
		MenuButton fillButton = this.getFillMenuButton();
		if (fillButton != null && fillButton.getId() == menuButton.getId()) {
			return fillButton;
		}
		if (fillMenuButtons != null) {
			for (MenuButton button : getFillMenuButtons().values())
				if (button.getId() == menuButton.getId())
					return button;
		}
		return null;
	}

	/**
	 * Gets the fill menu button associated with the given slot, if defined.
	 * Falls back to the shared fill button if a slot-specific one isn't present.
	 *
	 * @param slot the slot index
	 * @return the fill menu button for the slot, or the shared one if not explicitly defined
	 */
	@Nullable
	public MenuButton getFillMenuButton(int slot) {
		MenuButton menuButton = null;
		if (fillMenuButtons != null)
			menuButton = fillMenuButtons.get(slot);
		if (menuButton == null)
			menuButton = this.getFillMenuButton();
		return menuButton;
	}

	/**
	 * Internal accessor for the global fill button.
	 *
	 * @return the fill menu button, or {@code null} if none is set.
	 */
	@Nullable
	private MenuButton getFillMenuButton() {
		return fillMenuButton;
	}

	/**
	 * Retrieves the button data associated with a specific slot.
	 *
	 * @param slot the slot index.
	 * @return the button data for that slot, or {@code null} if not set
	 */
	@Nullable
	public ButtonData<T> getButton(final int slot) {
		return buttons.get(slot);
	}

	/**
	 * Returns an unmodifiable view of all registered buttons.
	 *
	 * @return the button map.
	 */
	public Map<Integer, ButtonData<T>> getButtons() {
		return Collections.unmodifiableMap(buttons);
	}

	/**
	 * Returns an unmodifiable view of the per-slot fill menu buttons.
	 * If none are defined, returns an empty map.
	 *
	 * @return the fill menu button map.
	 */
	public Map<Integer, MenuButton> getFillMenuButtons() {
		if (fillMenuButtons == null)
			return new HashMap<>();
		return Collections.unmodifiableMap(fillMenuButtons);
	}

	/**
	 * Retrieves the {@link MenuButton} shown at the given slot,
	 * either from button data or as a fallback fill button.
	 *
	 * @param slot the slot index
	 * @return the menu button for that slot, or {@code null} if none is applicable
	 */
	@Nullable
	public MenuButton getMenuButton(final int slot) {
		ButtonData<T> buttonData = this.getButton(slot);
		MenuButton menuButton = null;
		if (buttonData != null) {
			menuButton = buttonData.getMenuButton();
			if (menuButton == null)
				menuButton = getFillMenuButton(slot);
		}
		return menuButton;
	}

	@Override
	public String toString() {
		return "MenuDataUtility{" +
				"buttons=" + buttons +
				", fillMenuButton=" + fillMenuButton +
				", fillMenuButtons=" + fillMenuButtons +
				'}';
	}
}