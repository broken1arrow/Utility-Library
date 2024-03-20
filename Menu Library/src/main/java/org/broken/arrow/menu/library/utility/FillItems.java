package org.broken.arrow.menu.library.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillItems <T> {

    List<T> fillItems = new ArrayList<>();

    public List<T> getFillItems() {
        return Collections.unmodifiableList(fillItems);
    }

    public void remove(T item) {
        fillItems.remove(item);
    }

    public void setFillItems(List<T> fillItems) {
        this.fillItems = fillItems;
    }
}
