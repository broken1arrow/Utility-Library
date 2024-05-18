package org.broken.arrow.menu.library.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillItems<T> {

    List<T> fillItemsList = new ArrayList<>();

    public List<T> getFillItems() {
        return Collections.unmodifiableList(fillItemsList);
    }

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

    public void remove(T item) {
        fillItemsList.remove(item);
    }

    /**
     * Set the items you want connect to a slot in the menu.
     *
     * @param fillItems list of items.
     */
    public void setFillItems(List<T> fillItems) {
        this.fillItemsList = fillItems;
    }
}
