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
    public List<GradientChar> multiRgbGradientRaw(final GradientType type, final String text, final Color[] colors, final Double[] portions) {
        final List<GradientChar> result = new ArrayList<>();

        if (text == null || text.isEmpty()) return result;
        if (colors.length < 2) {
            for (char c : text.toCharArray()) {
                result.add(new GradientChar(c, toHex(colors[0])));
            }
            return result;
        }

        int length = text.length();

        // === Normalize portions ===
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

            // Clamp last segment to avoid overflow
            if (seg == colors.length - 2) {
                segLength = length - index;
            }

            Color from = colors[seg];
            Color to = colors[seg + 1];

            for (int i = 0; i < segLength && index < length; i++, index++) {
                double t = segLength <= 1 ? 0 : (double) i / (segLength - 1);

                Color color;
                if (type == GradientType.HSV_GRADIENT_PATTERN) {
                    color = interpolateHSV(from, to, t);
                } else if (type == GradientType.HSL_GRADIENT_PATTERN) {
                    color = interpolateHSL(from, to, t);
                } else {
                    color = interpolateRGB(from, to, t);
                }

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
    private Color interpolateHSV(Color from, Color to, double t) {
        float[] hsvFrom = Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), null);
        float[] hsvTo = Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), null);

        // Calculate the shortest path around the hue wheel
        float hFrom = hsvFrom[0];
        float hTo = hsvTo[0];
        float dh = hTo - hFrom;

        if (dh > 0.5f) {
            dh -= 1.0f;
        } else if (dh < -0.5f) {
            dh += 1.0f;
        }

        float h = hFrom + dh * (float) t;
        // Keep hue within standard 0.0 - 1.0 bounds safely
        h = (h % 1.0f + 1.0f) % 1.0f;

        float s = (float) (hsvFrom[1] + (hsvTo[1] - hsvFrom[1]) * t);
        float v = (float) (hsvFrom[2] + (hsvTo[2] - hsvFrom[2]) * t);

        return Color.getHSBColor(h, s, v);
    }

    private Color interpolateHSL(Color from, Color to, double t) {
        float[] hslFrom = rgbToHsl(from);
        float[] hslTo = rgbToHsl(to);

        float hFrom = hslFrom[0];
        float hTo = hslTo[0];
        float dh = hTo - hFrom;

        // Shortest path for Hue (same as HSV fix)
        if (dh > 0.5f) dh -= 1.0f;
        else if (dh < -0.5f) dh += 1.0f;

        float h = hFrom + dh * (float) t;
        h = (h % 1.0f + 1.0f) % 1.0f;

        float s = (float) (hslFrom[1] + (hslTo[1] - hslFrom[1]) * t);
        float l = (float) (hslFrom[2] + (hslTo[2] - hslFrom[2]) * t);

        return hslToRgb(h, s, l);
    }

    private float[] rgbToHsl(Color color) {
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;

        float h = 0f;
        float s = 0f;
        float l = (max + min) / 2f;

        if (d != 0f) {
            s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
            if (max == r) {
                h = (g - b) / d + (g < b ? 6f : 0f);
            } else if (max == g) {
                h = (b - r) / d + 2f;
            } else {
                h = (r - g) / d + 4f;
            }
            h /= 6f;
        }
        return new float[]{h, s, l};
    }

    private Color hslToRgb(float h, float s, float l) {
        float r, g, b;

        if (s == 0f) {
            r = g = b = l; // achromatic (gray)
        } else {
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }
        return new Color(Math.round(r * 255), Math.round(g * 255), Math.round(b * 255));
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
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
