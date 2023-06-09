package org.broken.arrow.title.update.library;

public class FieldName {
	private final int inventorySize;
	private final String fieldName;

	public FieldName(final int size, final String fieldName) {
		this.inventorySize = size;
		this.fieldName = fieldName;
	}

	/**
	 * Get the size of the inventory.
	 *
	 * @return inventory size.
	 */
	public int getInventorySize() {
		return inventorySize;
	}

	/**
	 * Get the field name for the current inventory.
	 *
	 * @return the name.
	 */
	public String getFieldName() {
		return fieldName;
	}
}