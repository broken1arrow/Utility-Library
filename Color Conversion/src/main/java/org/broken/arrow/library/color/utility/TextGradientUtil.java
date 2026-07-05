package org.broken.arrow.library.color.utility;

import org.broken.arrow.library.color.TextTranslator.GradientType;
import org.broken.arrow.library.color.gradient.GradientChar;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Utility class for working with text gradients.
 * <p>
 * Provides algorithms to generate multipoint gradients over text strings, supporting
 * both RGB linear interpolation and HSV cylindrical color space interpolation.
 */
public class TextGradientUtil {

    /**
     * Constructs a new {@code TextGradientUtil} instance.
     */
    public TextGradientUtil() {

    }

    /**
     * Interpolates a list of colors across a given text string, calculating either RGB or HSV steps.
     * <p>
     * This method can accept optional portion distributions to alter how much width each segment spans.
     * For the best visual effects, providing at least 3 colors is highly recommended.
     * <p>
     * <b>Example format supported by this logic:</b><br>
     * {@code gradients_<#D16BA5:#86A8E7:#5FFBF1>_portion<0.2:0.5:0.3>}
     *
     * @param type     the gradient strategy to use (e.g., RGB linear vs HSV hue paths)
     * @param text     the target text string to apply the gradient across
     * @param colors   the array of color keys defining the gradient steps
     * @param portions an array of double weights managing step widths, if null or empty, uniform spacing is used
     * @return a list of {@link GradientChar} mappings assigning a character to its calculated hex string color
     */
    public java.util.List<GradientChar> multiRgbGradientRaw(@Nonnull final GradientType type, @Nonnull final String text, @Nonnull final Color[] colors, @Nullable final Double[] portions) {
        final List<GradientChar> result = new ArrayList<>();

        if (text == null || text.isEmpty()) return result;
        if (colors.length < 2) {
            for (char c : text.toCharArray()) {
                result.add(new GradientChar(c, toHex(colors[0])));
            }
            return result;
        }
        int length = text.length();

        double[] p;
        if (portions == null || portions.length == 0) {
            p = new double[colors.length - 1];
            Arrays.fill(p, 1.0 / p.length);
        } else {
            p = Arrays.stream(portions).mapToDouble(Double::doubleValue).toArray();
        }
        int index = 0;
        for (int seg = 0; seg < colors.length - 1; seg++) {
            int segLength = (int) Math.round(p[seg] * length);

            if (seg == colors.length - 2) {
                segLength = length - index;
            }

            final Color from = colors[seg];
            final Color to = colors[seg + 1];

            for (int i = 0; i < segLength && index < length; i++, index++) {
                double t = segLength <= 1 ? 0 : (double) i / (segLength - 1);

                Color color = (type == GradientType.HSV_GRADIENT_PATTERN)
                        ? interpolateHSV(from, to, t)
                        : interpolateRGB(from, to, t);
                result.add(new GradientChar(text.charAt(index), toHex(color)));
            }
        }
        return result;
    }

    /**
     * Linearly interpolates between two colors within the RGB color space.
     *
     * @param from the starting color bounding the segment
     * @param to   the ending color bounding the segment
     * @param t    the interpolation fraction, ranging from 0.0 to 1.0 inclusive
     * @return the interpolated {@link Color}
     */
    private Color interpolateRGB(@Nonnull final Color from, @Nonnull final Color to, final double t) {
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * t);
        return new Color(r, g, b);
    }

    /**
     * Linearly interpolates between two colors within the HSV (Hue, Saturation, Brightness) color space.
     * <p>
     * This creates a cleaner, more vibrant transition across colors compared to raw RGB blending,
     * especially when shifting through distinct parts of the color spectrum.
     *
     * @param from the starting color bounding the segment
     * @param to   the ending color bounding the segment
     * @param t    the interpolation fraction, ranging from 0.0 to 1.0 inclusive
     * @return the interpolated {@link Color}
     */
    private Color interpolateHSV(@Nonnull final Color from, @Nonnull final Color to, final double t) {

        float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), null);
        float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), null);

        float h = (float) (hsvFrom[0] + (hsvTo[0] - hsvFrom[0]) * t);
        float s = (float) (hsvFrom[1] + (hsvTo[1] - hsvFrom[1]) * t);
        float v = (float) (hsvFrom[2] + (hsvTo[2] - hsvFrom[2]) * t);

        return Color.getHSBColor(h, s, v);
    }
    /**
     * Converts a standard {@link Color} object into a web-style hexadecimal string.
     *
     * @param color the color to convert
     * @return a hex string formatted as {@code #RRGGBB}
     */
    private String toHex(@Nonnull final Color color) {
        return String.format("#%02x%02x%02x",
                color.getRed(),
                color.getGreen(),
                color.getBlue());
    }

}
