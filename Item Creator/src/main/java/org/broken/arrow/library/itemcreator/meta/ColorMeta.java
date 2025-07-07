package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import java.util.logging.Level;

public class ColorMeta {
    private static final Logging logger = new Logging(ColorMeta.class);
    private String rgb;
    private int red = -1;
    private int green = -1;
    private int blue = -1;

    /**
     * Get red color.
     *
     * @return color number.
     */
    public int getRed() {
        return red;
    }

    /**
     * Get green color.
     *
     * @return color number.
     */
    public int getGreen() {
        return green;
    }

    /**
     * Get blue color
     *
     * @return color number.
     */
    public int getBlue() {
        return blue;
    }

    /**
     * Get the rbg colors, used to dye leather armor,potions and fireworks.
     *
     * @return string with the colors, like this #,#,#.
     */
    public String getRgb() {
        return rgb;
    }

    /**
     * Set the 3 colors auto.
     *
     * @param rgb string need to be formatted like this #,#,#.
     * @return this class.
     */
    public ColorMeta setRgb(final String rgb) {
        this.rgb = rgb;

        final String[] colors = this.getRgb().split(",");
        Validate.checkBoolean(colors.length < 4, "rgb is not format correctly. Should be formatted like this 'r,b,g'. Example '20,15,47'.");
        try {
            red = Integer.parseInt(colors[0]);
            green = Integer.parseInt(colors[2]);
            blue = Integer.parseInt(colors[1]);
        } catch (final NumberFormatException exception) {
            logger.log(Level.WARNING, exception, () -> "you don´t use numbers inside this string. Your input: " + rgb);
        }

        return this;
    }

    /**
     * Set the rbg color.
     *
     * @param red   color.
     * @param green color.
     * @param blue  color.
     * @return this class.
     */
    public ColorMeta setRgb(final int red, final int green, final int blue) {
        Validate.checkBoolean(red < 0 || green < 0 || blue < 0, "You can't use negative numbers for the rbg color.");

        this.rgb = red + "," + green + "," + blue;
        this.red = red;
        this.green = green;
        this.blue = blue;

        return this;
    }

    /**
     * Retrieve if all colors is set.
     *
     * @return true if the colors is set.
     */
    public boolean isColorSet() {
        return getRed() >= 0 && getGreen() >= 0 && getBlue() >= 0;
    }

}
