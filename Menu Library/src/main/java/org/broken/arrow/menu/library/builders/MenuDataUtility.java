package org.broken.arrow.menu.library.builders;

import org.broken.arrow.menu.library.button.MenuButtonI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MenuDataUtility {

	private final Map<Integer, ButtonData<?>> buttons = new HashMap<>();
	private Map<Integer, MenuButtonI<?>> fillMenuButtons;
	private MenuButtonI<?> fillMenuButton;

	public static MenuDataUtility of() {
		return new MenuDataUtility();
	}

	public MenuDataUtility putButton(final int slot, @Nonnull final ButtonData<?> buttonData) {
		return putButton(slot, buttonData, this.getFillMenuButton());
	}

	public <T> MenuDataUtility putButton(final int slot, @Nonnull final ButtonData<T> buttonData, @Nullable final MenuButtonI<?> fillMenuButton) {
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

	public MenuDataUtility setFillMenuButton(final MenuButtonI<?> fillMenuButton) {
		this.fillMenuButton = fillMenuButton;
		return this;
	}

	@Nullable
	public MenuButtonI<?> getSimilarFillMenuButton(@Nullable final MenuButtonI<?> button) {
		final MenuButtonI<?> menuButton = this.fillMenuButton;
		if (menuButton == null || button == null) return null;
		if (menuButton.getId() != button.getId()) return null;

		return menuButton;
	}

	@Nullable
	public MenuButtonI<?> getFillMenuButton(@Nonnull MenuButtonI<?> menuButton) {
		if (this.getFillMenuButton() != null && this.getFillMenuButton().getId() == menuButton.getId()) {
			return this.getFillMenuButton();
		}
		if (fillMenuButtons != null) {
			for (MenuButtonI<?> button : getFillMenuButtons().values())
				if (button.getId() == menuButton.getId())
					return button;
		}
		return null;
	}

	@Nullable
	public MenuButtonI<?> getFillMenuButton(int slot) {
		MenuButtonI<?> menuButton = null;
		if (fillMenuButtons != null)
			menuButton = fillMenuButtons.get(slot);
		if (menuButton == null)
			menuButton = this.getFillMenuButton();
		return menuButton;
	}

	@Nullable
	private MenuButtonI<?> getFillMenuButton() {
		return fillMenuButton;
	}

	@Nullable
	public ButtonData<?> getButton(final int slot) {
		return buttons.get(slot);
	}

	public Map<Integer, ButtonData<?>> getButtons() {
		return Collections.unmodifiableMap(buttons);
	}

	public Map<Integer, MenuButtonI<?>> getFillMenuButtons() {
		if (fillMenuButtons == null)
			return new HashMap<>();
		return Collections.unmodifiableMap(fillMenuButtons);
	}

	@Nullable
	public MenuButtonI<?> getMenuButton(final int slot) {
		ButtonData<?> buttonData = this.getButton(slot);
		MenuButtonI<?> menuButton = null;
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