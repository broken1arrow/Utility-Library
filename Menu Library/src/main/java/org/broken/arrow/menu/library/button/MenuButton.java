package org.broken.arrow.menu.library.button;

public abstract class MenuButton implements MenuButtonI<Object> {
	private static int counter = 0;
	private final int id;

	public MenuButton() {
		this.id = counter++;
	}

	@Override
	public int getId() {
		return id;
	}
}
