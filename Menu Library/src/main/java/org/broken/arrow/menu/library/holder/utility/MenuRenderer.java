package org.broken.arrow.menu.library.holder.utility;

import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.builders.MenuDataUtility;

import java.util.List;

public class MenuRenderer<T> {

    private final MenuUtility<T> utility;
    private int itemIndex;
    private int lastFillSlot;

    public MenuRenderer(MenuUtility<T> utility) {
        this.utility = utility;
    }

    public int getStartItemIndex() {
        return  itemIndex;
    }

    public void setStartItemIndex(final int startItemIndex) {
        this.itemIndex = startItemIndex;
    }

    public void resetStartItemIndex() {
        this.itemIndex = 0;
    }

    public void setHighestFillSlot(final int highestFillSlot) {
        this.lastFillSlot = highestFillSlot;
    }

    public MenuDataUtility<T> renderPage(final int pageNumber) {
        MenuDataUtility<T> data = new MenuDataUtility<>();
        List<Integer> fillSlots = this.utility.getFillSpace();

        for (int slot = 0; slot < this.utility.getInventorySize(); slot++) {
            boolean isFillButton = fillSlots.contains(slot);

            this.utility.setButton(pageNumber, data, slot, this.itemIndex, slot > this.lastFillSlot);
            if (isFillButton) incrementItemIndex();
        }
        return data;
    }

    private void incrementItemIndex() {
        itemIndex++;
    }

}