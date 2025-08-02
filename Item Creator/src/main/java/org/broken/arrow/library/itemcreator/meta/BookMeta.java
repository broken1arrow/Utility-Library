package org.broken.arrow.library.itemcreator.meta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BookMeta {
    private List<String> pages = new ArrayList<>();
    private org.bukkit.inventory.meta.BookMeta.Generation generation;
    private String title;
    private String author;
    static final int MAX_PAGES = Integer.MAX_VALUE;
    static final int MAX_PAGE_LENGTH = 256;

    public BookMeta() {
    }

    @Nonnull
    public static BookMeta setBookMeta(@Nonnull final org.bukkit.inventory.meta.BookMeta bukkitBookMeta) {
        BookMeta bookMeta = new BookMeta();
        bookMeta.setAuthor(bukkitBookMeta.getAuthor());
        bookMeta.setTitle(bukkitBookMeta.getTitle());
        bookMeta.setGeneration(bukkitBookMeta.getGeneration());
        bookMeta.setPages(bukkitBookMeta.getPages());

        return bookMeta;
    }


    /**
     * Gets the title of the book.
     * <p>
     *
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the book.
     * <p>
     * Limited to 32 characters. Removes title when given null.
     *
     * @param title the title to set.
     */
    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    /**
     * Gets the author of the book.
     * <p>
     *
     * @return the author of the book
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the book. Removes author when given null.
     *
     * @param author the author to set
     */
    public void setAuthor(@Nullable final String author) {
        this.author = author;
    }

    /**
     * Gets the generation of the book.
     * <p>
     *
     * @return the generation of the book
     */
    public org.bukkit.inventory.meta.BookMeta.Generation getGeneration() {
        return generation;
    }

    /**
     * Sets the generation of the book. Removes generation when given null.
     *
     * @param generation the generation to set
     */
    public void setGeneration(org.bukkit.inventory.meta.BookMeta.Generation generation) {
        this.generation = generation;
    }

    /**
     * Sets the specified page in the book. Pages of the book must be
     * contiguous.
     * <p>
     * The text can be up to 256 characters in length, additional characters
     * are truncated.
     * <p>
     * Pages are 1-indexed.
     *
     * @param page the page number to set, in range [1, getPageCount()]
     * @param text the text to set for that page
     */
    public void setPage(int page, @Nullable String text) {
        String newText = validatePage(text);
        pages.set(page - 1, newText);
    }

    /**
     * Clears the existing book pages, and sets the book to use the provided
     * pages. Maximum 50 pages with 256 characters per page.
     *
     * @param pages A list of strings, each being a page
     */
    public void setPages(@Nonnull String... pages) {
        this.setPages(Arrays.asList(pages));
    }

    /**
     * Clears the existing book pages, and sets the book to use the provided
     * pages. Maximum 100 pages with 256 characters per page.
     *
     * @param pages A list of pages to set the book to use
     */
    public void setPages(List<String> pages) {
        if (pages.isEmpty()) {
            this.pages = null;
            return;
        }

        if (this.pages != null) {
            this.pages.clear();
        }
        for (String page : pages) {
            addPage(page);
        }
    }

    /**
     * Adds new pages to the end of the book. Up to a maximum of 50 pages with
     * 256 characters per page.
     *
     * @param pages A list of strings, each being a page
     */
    public void addPage(@Nonnull String... pages) {
        for (String page : pages) {
            page = validatePage(page);
            internalAddPage(page);
        }
    }

    /**
     * Gets the specified page in the book. The given page must exist.
     * <p>
     * Pages are 1-indexed.
     *
     * @param page the page number to get, in range [1, getPageCount()]
     * @return the page from the book
     */
    public String getPage(final int page) {
        //Preconditions.checkArgument(isValidPage(page), "Invalid page number (%s)", page);
        // assert: pages != null
        return pages.get(page - 1);
    }

    /**
     * Gets all the pages in the book.
     *
     * @return list of all the pages in the book
     */
    @Nonnull
    public List<String> getPages() {
        if (pages == null) return new ArrayList<>();
        return Collections.unmodifiableList(pages);
    }

    /**
     * Gets the number of pages in the book.
     *
     * @return the number of pages in the book
     */
    public int getPageCount() {
        return (pages == null) ? 0 : pages.size();
    }


    private boolean isValidPage(int page) {
        return page > 0 && page <= getPageCount();
    }

    private String validatePage(String page) {
        if (page == null) {
            page = "";
        } else if (page.length() > MAX_PAGE_LENGTH) {
            page = page.substring(0, MAX_PAGE_LENGTH);
        }
        return page;
    }

    private void internalAddPage(String page) {
        // asserted: page != null
        if (this.pages == null) {
            this.pages = new ArrayList<>();
        } else if (this.pages.size() == MAX_PAGES) {
            return;
        }
        this.pages.add(page);
    }



}
