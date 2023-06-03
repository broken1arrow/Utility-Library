package org.broken.arrow.utility.library;

import org.broken.arrow.menu.library.button.GenericMenuButton;
import org.broken.arrow.menu.library.holder.GenericMenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class menutesting extends GenericMenuHolder<String> {


	public menutesting(final List<String> fillItems) {
		super(fillItems);

		new GenericMenuButton<String>() {

			@Override
			public void onClickInsideMenu(final Player player, final Inventory menu, final ClickType click, final ItemStack clickedItem, final String object) {

			}

			@Override
			public ItemStack getItem() {
				return null;
			}
		}
	}
}
