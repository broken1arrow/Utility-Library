package org.broken.arrow.menu.library.builders;

import org.broken.arrow.menu.library.button.MenuButtonI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MenuDataUtility<T> {

	private final Map<Integer, ButtonData<T>> buttons = new HashMap<>();
	private Map<Integer, MenuButtonI<T>> fillMenuButtons;
	private MenuButtonI<T> fillMenuButton;

	
	public MenuDataUtility<T> putButton(final int slot, @Nonnull final ButtonData<T> buttonData) {
		return putButton(slot, buttonData, this.getFillMenuButton());
	}

	public MenuDataUtility<T> putButton(final int slot, @Nonnull final ButtonData<T> buttonData, @Nullable final MenuButtonI<T> fillMenuButton) {
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

	public MenuDataUtility<T> setFillMenuButton(final MenuButtonI<T> fillMenuButton) {
		this.fillMenuButton = fillMenuButton;
		return this;
	}

	@Nullable
	public MenuButtonI<T> getSimilarFillMenuButton(@Nullable final MenuButtonI<T> button) {
		final MenuButtonI<T> menuButton = this.fillMenuButton;
		if (menuButton == null || button == null) return null;
		if (menuButton.getId() != button.getId()) return null;

		return menuButton;
	}

	@Nullable
	public MenuButtonI<T> getFillMenuButton(@Nonnull MenuButtonI<T> menuButton) {
		if (this.getFillMenuButton() != null && this.getFillMenuButton().getId() == menuButton.getId()) {
			return this.getFillMenuButton();
		}
		if (fillMenuButtons != null) {
			for (MenuButtonI<T> button : getFillMenuButtons().values())
				if (button.getId() == menuButton.getId())
					return button;
		}
		return null;
	}

	@Nullable
	public MenuButtonI<T> getFillMenuButton(int slot) {
		MenuButtonI<T> menuButton = null;
		if (fillMenuButtons != null)
			menuButton = fillMenuButtons.get(slot);
		if (menuButton == null)
			menuButton = this.getFillMenuButton();
		return menuButton;
	}

	@Nullable
	private MenuButtonI<T> getFillMenuButton() {
		return fillMenuButton;
	}

	@Nullable
	public ButtonData<T> getButton(final int slot) {
		return buttons.get(slot);
	}

	public Map<Integer, ButtonData<T>> getButtons() {
		return Collections.unmodifiableMap(buttons);
	}

	public Map<Integer, MenuButtonI<T>> getFillMenuButtons() {
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