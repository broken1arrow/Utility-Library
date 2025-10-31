package org.broken.arrow.library.itemcreator.meta.map;

public interface CharShape {

    /**
     * Set the transparency for your letter.
     *
     * @param width  the width currently processing
     * @param height the height currently processing
     * @return boolean for given width and height for the letter set.
     */
    boolean getPixel(int width, int height);
}