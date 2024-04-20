package org.broken.arrow.menu.library.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillItems<T> {

    List<T> fillItems = new ArrayList<>();

    public List<T> getFillItems() {
        return Collections.unmodifiableList(fillItems);
    }

    public T getFillItem(int index) {
        if (this.fillItems.isEmpty())
            return null;
        if (index >= this.fillItems.size())
            return null;
        if (index < 0)
            return null;
        List<T> fillItems = this.getFillItems();
        return fillItems.get(index);
    }

    public void remove(T item) {
        fillItems.remove(item);
    }

    /**
     * Set the items you want connect to a slot in the menu.
     *
     * @param fillItems list of items.
     */
    public void setFillItems(List<T> fillItems) {
        this.fillItems = fillItems;
    }
}
