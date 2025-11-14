package org.broken.arrow.library.itemcreator.meta.map.color.parser;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.pixel.MapColoredPixel;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for smoothing and balancing colors from a source image before rendering
 * onto a Minecraft-style map.
 * <p>
 * Certain images produce extreme brightness differences or isolated bright pixels
 * which result in visual artifacts (e.g., "snow" or white speckles).
 * This class applies lightweight filtering to reduce those issues while preserving detail.
 * <p>
 * The filtered pixels are forwarded into the provided {@link MapRendererData} instance.
 * <strong>Recommended:</strong> scale the image before passing it in. Map items only
 * support 128×128 pixels, so supplying very large images (e.g., 4000×4000) would be
 * unnecessarily expensive.
 */
public class RenderColors {

    private RenderColors() {
    }

    /**
     * Processes a scaled image, smooths brightness inconsistencies,
     * and sends final pixel colors to the provided {@link MapRendererData}.
     * <p>
     * Note:  this method will make a copy of your image.
     *
     * @param scaled the image already scaled to map resolution (typically 128×128)
     * @return a list of set pixels to be set in {@link MapRendererData#addAll(List)}.
     */
    public static List<MapColoredPixel> renderFromImage(final BufferedImage scaled) {
        return renderFromImage(scaled, true);
    }

    /**
     * Processes a scaled image, smooths brightness inconsistencies,
     * and sends final pixel colors to the provided {@link MapRendererData}.
     *
     * @param scaled the image already scaled to map resolution (typically 128×128)
     * @param copy   Make a copy of the image before scale it.
     * @return a list of set pixels to be set in {@link MapRendererData#addAll(List)}.
     */
    public static List<MapColoredPixel> renderFromImage(final BufferedImage scaled, final boolean copy) {
        int width = scaled.getWidth();
        int height = scaled.getHeight();

        BufferedImage filtered = copy ? new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB) : scaled;

        float[] neighB = new float[8];
        float[] hsb = new float[3];
        float[] hsb2 = new float[3];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                getHSB(scaled.getRGB(x, y), hsb);
                float centerB = hsb[2];
                int idx = 0;
                neighbouringPixels(scaled, neighB, idx, x, y, hsb2);

                float median = getMedianBrightness(neighB);
                float diff = centerB - median;
                if (Math.abs(diff) > 0.18f) {
                    centerB = centerB * 0.4f + median * 0.6f;
                }
                centerB = getBrightness(neighB, idx, centerB);

                int rgb = convertHSBtoRGB(hsb[0], hsb[1], clamp(centerB, 0f, 1f));
                filtered.setRGB(x, y, rgb);
            }
        }
        return addPixels(height, width, filtered);
    }

    /**
     * Converts a color from HSB components into packed RGB format (0xAARRGGBB).
     * This matches the RGB format used by {@link Color#getRGB()} and can be directly
     * passed to the {@link Color#Color(int)} constructor.
     *
     * @param hue        hue component (any float; integer part is ignored)
     * @param saturation value in range 0.0–1.0
     * @param brightness value in range 0.0–1.0
     * @return a packed ARGB integer representing the color
     */
    public static int convertHSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
                default:
                    break;
            }
        }
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }

    /**
     * Extracts the hue, saturation, and brightness (HSB) values from a packed RGB integer.
     * <p>
     * This is a convenience wrapper around {@link #retrieveRGBToHSB(int, int, int, float[])}.
     * The input must be in the same packed format used by {@link java.awt.Color#getRGB()} or
     * by constructing a {@link java.awt.Color} with an integer.
     * <p>
     * If {@code hsbvals} is {@code null}, a new float[3] is created. Otherwise, the existing
     * array is reused and written to.
     *
     * @param rgb     a packed 0xAARRGGBB or 0xRRGGBB integer
     * @param hsbvals optional array to store the result; may be {@code null}
     * @return an array of three floats containing hue, saturation, and brightness (in that order)
     */
    public static float[] getHSB(final int rgb, final float[] hsbvals) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return retrieveRGBToHSB(r, g, b, hsbvals);
    }

    /**
     * Converts RGB components into hue, saturation, and brightness values (HSB).
     * <p>
     * Equivalent to {@link java.awt.Color#RGBtoHSB(int, int, int, float[])}, but implemented
     * locally to avoid allocations and improve performance when called frequently.
     * <p>
     * If {@code hsbvals} is {@code null}, a new array is allocated. If not null, values are
     * written directly into the provided array.
     *
     * @param r       red component (0–255)
     * @param g       green component (0–255)
     * @param b       blue component (0–255)
     * @param hsbvals optional array to store the result; may be {@code null}
     * @return an array containing hue, saturation, and brightness (in that order).
     * @see java.awt.Color#getRGB()
     * @see java.awt.Color#Color(int)
     * @see java.awt.Color#RGBtoHSB(int, int, int, float[])
     */
    public static float[] retrieveRGBToHSB(int r, int g, int b, float[] hsbvals) {
        float hue;
        float saturation;
        float brightness;
        if (hsbvals == null) {
            hsbvals = new float[3];
        }
        int cmax = (r > g) ? r : g;
        if (b > cmax) cmax = b;
        int cmin = (r < g) ? r : g;
        if (b < cmin) cmin = b;

        brightness = (float) (cmax / 255.0);
        if (cmax != 0)
            saturation = ((float) (cmax - cmin)) / ((float) cmax);
        else
            saturation = 0;
        if (saturation == 0)
            hue = 0;
        else {
            float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
            float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
            float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0f + redc - bluec;
            else
                hue = 4.0f + greenc - redc;
            hue = hue / 6.0f;
            if (hue < 0)
                hue = hue + 1.0f;
        }
        hsbvals[0] = hue;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    private static void neighbouringPixels(final BufferedImage scaled, final float[] neighB, int idx, final int x, final int y, final float[] hsb2) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                neighB[idx++] = getHSB(scaled.getRGB(x + dx, y + dy), hsb2)[2];
            }
        }
    }


    /**
     * Returns the median brightness value from eight neighboring pixels.
     * <p>
     * Only the five smallest values are partially sorted, which is sufficient
     * to find the true median for a fixed-size 8-element list. This is faster
     * than fully sorting the array.
     *
     * @param v an array of 8 brightness values (0–1 range)
     * @return the median brightness value
     */
    private static float getMedianBrightness(float[] v) {
        for (int i = 0; i < 5; i++) {
            int min = i;
            for (int j = i + 1; j < 8; j++) {
                if (v[j] < v[min]) min = j;
            }
            float t = v[i];
            v[i] = v[min];
            v[min] = t;
        }
        return v[4];
    }

    /**
     * Clamps a value into a min/max range. If the number is less than {@code min},
     * {@code min} is returned. If greater than {@code max}, {@code max} is returned.
     * Otherwise, the value itself is returned.
     *
     * @param v   the value to clamp
     * @param min the lower bound
     * @param max the upper bound
     * @return a value within the inclusive range {@code [min, max]}
     */
    private static float clamp(float v, float min, float max) {
        if (v < min) {
            return min;
        }
        if (v > max) return max;

        return v;
    }

    private static float getBrightness(final float[] neighB, int idx, float brightness) {
        float bilateral = 0f;
        float weightSum = 0f;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                float nb = neighB[idx++];
                float distW = (dx == 0 || dy == 0) ? 1f : 0.7f;
                float diffB = Math.abs(brightness - nb);
                float colorW = (diffB < 0.07f) ? 1f : 0.2f;
                float w = distW * colorW;

                bilateral += nb * w;
                weightSum += w;
            }
        }

        if (weightSum > 0) {
            float smoothB = bilateral / weightSum;
            brightness = brightness * 0.85f + smoothB * 0.15f;
        }
        return brightness;
    }


    private static List<MapColoredPixel> addPixels(final int height, final int width, @Nonnull final BufferedImage filtered) {
        List<MapColoredPixel> mapColoredPixels = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                mapColoredPixels.add(new MapColoredPixel(x, y, new Color(filtered.getRGB(x, y))));
            }
        }
        return mapColoredPixels;
    }

}
