package org.broken.arrow.library.menu.utility.metadata;

public final class Identifier {

	private int id;

	public Identifier() {
		this.id = -1;
	}

	/**
	 * Set your own unique ID for your custom tag. Will only be applied if value is more than zero.
	 *
	 * @param id your id for a specific key.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retrieves the unique identifier for this metadata tag.
	 * <p>
	 * The returned value can range from {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}.
	 * </p>
	 *
	 * @return the integer ID representing this metadata tag.
	 */
	public int getId() {
		return id;
	}
}