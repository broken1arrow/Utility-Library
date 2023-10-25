package org.broken.arrow.menu.library.button;

public abstract class GenericMenuButton<T> implements MenuButtonI<T> {

	private static int counter = 0;
	private final int id;

	protected GenericMenuButton() {
		this.id = counter++;
	}

	/**
	 * The unique id for this instance.
	 *
	 * @return the id for this instance.
	 */
	@Override
	public int getId() {
		return id;
	}
}
