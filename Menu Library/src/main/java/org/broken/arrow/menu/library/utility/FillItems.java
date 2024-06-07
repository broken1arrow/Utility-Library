package org.broken.arrow.menu.library.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class handles the logic around managing fill items.
 *
 * @param <T> The generic type of the fill items added to the list.
 */
public class FillItems<T> {
    private List<T> fillItemsList = new ArrayList<>();

    /**
     * Retrieves an unmodifiable view of the fill items list.
     *
     * @return an unmodifiable list of fill items.
     */
    public List<T> getFillItems() {
        return Collections.unmodifiableList(fillItemsList);
    }

    /**
     * Retrieves the fill item at the specified index. It ensures that the index is within bounds.
     *
     * @param index the index of the fill item to retrieve.
     * @return the fill item at the specified index, or null if the index is out of bounds or the list is empty.
     */
    public T getFillItem(int index) {
        List<T> fillItems = this.getFillItems();
        if (fillItems.isEmpty())
            return null;
        if (index >= fillItems.size())
            return null;
        if (index < 0)
            return null;
        return fillItems.get(index);
    }

    /**
     * Sets the fill items for this list. This will replace any existing items.
     * This will utilize the order in the list when it populates the menu, so
     * the order matters and it also supports null values.
     *
     * @param fillItems the list of fill items to set.
     */
    public void setFillItems(List<T> fillItems) {
        this.fillItemsList = fillItems;
    }

    /**
     * Adds a fill item to the list. This method will add the
     * item at the end of the current list.
     *
     * @param fillItem the fill item to add.
     */
    public void addFillItem(T fillItem) {
        this.fillItemsList.add(fillItem);
    }

    /**
     * Removes the specified fill item from the list.
     *
     * @param item the fill item to remove.
     */
    public void remove(T item) {
        fillItemsList.remove(item);
    }

    /**
     * Clears all fill items from the list.
     */
    public void clear() {
        fillItemsList.clear();
    }

}
