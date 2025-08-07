package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Color;

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
     * Set the 3 colors from Bukkit Color.
     *
     * @param color you want to convert to split up in three colors.
     */
    public void setRgb(final Color color) {
        final int colorRed = color.getRed();
        final int colorGreen = color.getGreen();
        final int colorBlue = color.getBlue();
        this.setRgb(colorRed, colorGreen, colorBlue);
    }

    /**
     * Set the 3 colors from a string. The color order is R,B,G
     * and the string should look like this 20,15,47.
     *
     * @param rgb string need to be formatted like this #,#,#.
     */
    public void setRgb(final String rgb) {
        this.rgb = rgb;
        final String[] colors = this.getRgb() != null ? this.getRgb().split(",") : new String[]{""};
        Validate.checkBoolean(colors.length < 3, "rgb is not format correctly. Should be formatted like this 'r,b,g'. Example '20,15,47'. Current input is " + rgb + " length "+ colors.length);
        try {
            final int colorRed = Integer.parseInt(colors[0]);
            final int colorGreen = Integer.parseInt(colors[2]);
            final int colorBlue = Integer.parseInt(colors[1]);

            this.setRgb(colorRed, colorGreen, colorBlue);
        } catch (final NumberFormatException exception) {
            logger.log(Level.WARNING, exception, () -> "you don´t use numbers inside this string. Your input: " + rgb);
        }
    }


    /**
     * Set the rbg color.
     *
     * @param red   color.
     * @param green color.
     * @param blue  color.
     */
    public void setRgb(final int red, final int green, final int blue) {
        Validate.checkBoolean(red < 0 || green < 0 || blue < 0, "You can't use negative numbers for the rbg color.");

        this.rgb = red + "," + green + "," + blue;
        this.red = red;
        this.green = green;
        this.blue = blue;

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
