package org.broken.arrow.menu.library.test;

import org.broken.arrow.menu.library.button.GenericMenuButton;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.button.MenuButtonI;
import org.broken.arrow.menu.library.holder.MenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class menutest extends MenuHolder {

	MenuButtonI<Integer> button;
	MenuButtonI<?> menuButtonDefault;

	public menutest(final List<String> fillItems) {
		super(fillItems);

		button = new GenericMenuButton<Integer>() {
			@Override
			public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem, final Integer object) {
				updateButton(this);
			}

			@Override
			public ItemStack getItem() {
				return null;
			}
		};
		menuButtonDefault = new MenuButton() {
			@Override
			public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem, final Object object) {

			}

			@Override
			public ItemStack getItem() {
				return null;
			}
		};
	}

	@Nullable
	@Override
	public MenuButtonI<Object> getFillButtonAt(@Nonnull final Object object) {
		return super.getFillButtonAt(object);
	}
}
