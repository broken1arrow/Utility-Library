package org.broken.arrow.menu.library.holder.utility;

import org.broken.arrow.logging.library.Logging;
import org.broken.arrow.menu.library.MenuUtility;
import org.broken.arrow.menu.library.builders.MenuDataUtility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Responsible for rendering menu pages and managing menu items for each page.
 * This class supports both single-page rendering and caching of multiple pages.
 *
 * @param <T> The type of data being rendered as the object connected to the item.
 */
public class MenuRenderer<T> {
    private final Logging logger = new Logging(MenuRenderer.class);
    private final MenuUtility<T> utility;
    private Supplier<Double> amountOfPages;
    private int itemIndex;
    private int lastFillSlot;
    private int numberOfFillItems;
    private int requiredPages;

    /**
     * Constructs a MenuRenderer with the provided utility object.
     *
     * @param utility the MenuUtility that assists with button creation and layout.
     */
    public MenuRenderer(MenuUtility<T> utility) {
        this.utility = utility;
    }

    /**
     * Gets the current starting index for item rendering.
     *
     * @return the start item index
     */
    public int getStartItemIndex() {
        return itemIndex;
    }

    /**
     * Sets the current starting index for item rendering.
     *
     * @param startItemIndex the new start index
     */
    public void setStartItemIndex(final int startItemIndex) {
        this.itemIndex = startItemIndex;
    }

    /**
     * Resets the item index to zero.
     */
    public void resetStartItemIndex() {
        this.itemIndex = 0;
    }

    /**
     * Sets the highest slot index that is considered for item filling.
     *
     * @param highestFillSlot the highest fillable slot index
     */
    public void setHighestFillSlot(final int highestFillSlot) {
        this.lastFillSlot = highestFillSlot;
    }

    /**
     * Gets the number of pages required to render all items.
     *
     * @return the number of required pages
     */
    public int getRequiredPages() {
        return requiredPages;
    }

    /**
     * Gets the number of items rendered on one page.
     *
     * @return the number of fill items per page
     */
    public int getNumberOfFillItems() {
        return numberOfFillItems;
    }

    /**
     * Prepares and caches menu items across all available pages.
     * Calculates the number of required pages and fills them by rendering.
     *
     * @return the total number of pages rendered
     */
    public int setMenuItemsToAllPages() {
        requiredPages = Math.max((int) Math.ceil(amountOfPages()), 1);

        this.resetStartItemIndex();
        this.setHighestFillSlot(this.utility.getHighestFillSlot());

        for (int i = 0; i < requiredPages; i++) {
            this.cacheButton(i);
            if (i == 0) numberOfFillItems = this.getStartItemIndex();
        }
        this.resetStartItemIndex();
        return requiredPages;
    }

    /**
     * Prepares and caches menu items for a specific page.
     * This is useful for rendering on demand.
     *
     * @param pageNumber the index of the page to render
     * @return the total number of pages that exist
     */
    public int setMenuItemsToPage(final int pageNumber) {
        requiredPages = Math.max((int) Math.ceil(amountOfPages()), 1);
        int currentFillSlot = pageNumber * numberOfFillItems;

        this.setStartItemIndex(currentFillSlot);
        if (this.lastFillSlot <= 0)
            this.setHighestFillSlot(this.utility.getHighestFillSlot());
        this.cacheButton(pageNumber);

        if (numberOfFillItems <= 0)
            numberOfFillItems = this.getStartItemIndex();

        return requiredPages;
    }

    /**
     * Caches the button layout for a specific page, including setting and optionally storing the layout.
     * If {@code shallCacheItems()} returns false, it stores the rendered page data.
     *
     * @param pageNumber the page index to cache
     */
    public void cacheButton(final int pageNumber) {

        MenuDataUtility<T> menuDataUtility = this.renderPage(pageNumber);
        if (!this.utility.shallCacheItems()) {
            this.utility.putAddedButtonsCache(pageNumber, menuDataUtility);
        }
        this.utility.retrieveMenuButtons(pageNumber, menuDataUtility);
    }

    /**
     * Renders a menu page into a MenuDataUtility instance.
     *
     * @param pageNumber the index of the page to render
     * @return a MenuDataUtility containing the rendered items
     */
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

    /**
     * Sets the custom supplier for determining the number of pages.
     *
     * @param amountOfPages a supplier that returns a number representing the page count
     */
    public void setAmountOfPages(@Nonnull final Supplier<Double> amountOfPages) {
        this.amountOfPages = amountOfPages;
    }

    /**
     * Calculates the raw amount of pages needed for the menu.
     * This method does NOT round the result; it may return fractional pages.
     * Use {@link Math#ceil(double)} to convert to an integer.
     *
     * @return the raw number of pages required.
     */
    private double amountOfPages() {
        Double setPages = getSetPages();
        final List<T> fillItems = this.utility.getListOfFillItems();

        int perPageItems = this.utility.getItemsPerPage();
        int size = this.utility.getInventorySize();
        int itemCount = (fillItems == null || fillItems.isEmpty()) ? (size - 9) : fillItems.size();

        if (perPageItems > size) {
            this.logger.log(Level.WARNING, () ->
                    "Items per page are greater than inventory size. Items per page: " + perPageItems + ". Inventory size: " + size);
            return (double) itemCount / fallbackPerPage(size);
        } else if (perPageItems <= 0) {
            this.logger.log(Level.WARNING, () -> "Items per page must be greater than 0.");
            return 0;
        }

        double requiredPages = (double) itemCount / perPageItems;
        int manuallySetPages = this.utility.getManuallySetPages();
        if (setPages != null) {
            return Math.max(setPages, requiredPages);
        } else if (manuallySetPages > 0) {
            return Math.max(manuallySetPages, requiredPages);
        }

        return requiredPages;
    }

    /**
     * Retrieves the value from the {@code amountOfPages} supplier if valid.
     *
     * @return a valid page count, or {@code null} if not available or invalid
     */
    @Nullable
    private Double getSetPages() {
        if (this.amountOfPages != null) {
            Double pagesTotal = this.amountOfPages.get();
            if (pagesTotal != null && pagesTotal > 0)
                return pagesTotal;
        }
        return null;
    }

    /**
     * Increments the internal item index counter.
     */
    private void incrementItemIndex() {
        itemIndex++;
    }

    private int fallbackPerPage(int size) {
        int adjusted = size - 9;
        return adjusted <= 1 ? size : adjusted;
    }
}