package org.broken.arrow.library.itemcreator.meta;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;
import org.bukkit.Color;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class ColorMeta {
    private static final Logging logger = new Logging(ColorMeta.class);
    private String rgb;
    private Color color;
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
     * Gets the color as an RGB integer.
     *
     * @return An integer representation of this color, as 0xRRGGBB
     */
    public int toRgb() {
        return color.asRGB();
    }

    /**
     * Gets the color as an ARGB integer.
     *
     * @return An integer representation of this color, as 0xAARRGGBB
     */
    public int toArgb() {
        return color.asARGB();
    }

    /**
     * Set the 3 colors from Bukkit Color.
     *
     * @param color you want to convert to split up in three colors.
     */
    public void setRgb(@Nonnull final Color color) {
        this.setColor(color);
    }

    /**
     * Sets a new color object from an integer that contains the red,
     * green, and blue bytes in the lowest order 24 bits.
     *
     * @param rgb the integer storing the red, green, and blue values
     * @throws IllegalArgumentException if any data is in the highest order 8
     *                                  bits
     */
    public void setRgb(final int rgb) {
        final Color color = Color.fromRGB(rgb);
        this.setRgb(color);
    }

    /**
     * Creates a new color object from an integer that contains the alpha, red,
     * green, and blue bytes.
     *
     * @param argb the integer storing the alpha, red, green, and blue values
     */
    public void setArgb(final int argb) {
        final Color color = Color.fromARGB(argb);
        this.setRgb(color);
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
        Validate.checkBoolean(colors.length < 3, "rgb is not format correctly. Should be formatted like this 'r,b,g'. Example '20,15,47'. Current input is " + rgb + " length " + colors.length);
        try {
            final int colorRed = Integer.parseInt(colors[0]);
            final int colorGreen = Integer.parseInt(colors[2]);
            final int colorBlue = Integer.parseInt(colors[1]);

            this.setRgb(255,colorRed, colorGreen, colorBlue);
        } catch (final NumberFormatException exception) {
            logger.log(Level.WARNING, exception, () -> "you don´t use numbers inside this string. Your input: " + rgb);
        }
    }

    /**
     * Creates a new Color object from an alpha, red, green, and blue
     *
     * @param alpha integer from 0-255
     * @param red integer from 0-255
     * @param green integer from 0-255
     * @param blue integer from 0-255
     * @throws Validate.ValidateExceptions if any value is strictly {@literal >255 or <0}
     */
    public void setRgb(final int alpha,final int red, final int green, final int blue) {
        Validate.checkBoolean(red < 0 || green < 0 || blue < 0, "You can't use negative numbers for the rbg color.");
        final Color color = Color.fromARGB(alpha,red, green, blue);
        this.setColor(color);
    }

    /**
     * Set the rbg color.
     *
     * @param color The color object to set.
     */
    private void setColor(final Color color) {
        Validate.checkBoolean(color == null, "You can't set color to null.");

        final int colorRed = color.getRed();
        final int colorGreen = color.getGreen();
        final int colorBlue = color.getBlue();

        this.rgb = colorRed + "," + colorGreen + "," + colorBlue;
        this.color = color;
        this.red = colorRed;
        this.green = colorGreen;
        this.blue = colorBlue;

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
