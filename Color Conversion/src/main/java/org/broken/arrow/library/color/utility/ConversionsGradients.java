package org.broken.arrow.library.color.utility;

import org.broken.arrow.library.logging.Logging;
import org.broken.arrow.library.logging.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConversionsGradients {
    private static final Logging LOG = new Logging(ConversionsGradients.class);
    private final Color[] colors;

    /**
     * Create an instance to convert to the Color array
     *
     * @param hexHolors The string that is formated #E786C5:#D3FB5F with two or more color.
     */
    private ConversionsGradients(@Nonnull final String hexHolors) {
        colors = parseColors(hexHolors);
    }

    /**
     * Split up colors from a string that is formated #E786C5:#D3FB5F
     *
     * @param hexHolors The string that is formated #E786C5:#D3FB5F with two or more color.
     * @return an array of colors.
     */
    public static ConversionsGradients parse(@Nonnull final String hexHolors) {
        return new ConversionsGradients(hexHolors);
    }

    /**
     * Retrieve the set colors.
     *
     * @return the array of colors set.
     */
    @Nonnull
    public Color[] getColors() {
        if (this.colors == null)
            return new Color[0];
        return colors;
    }

    /**
     * The portion wight of the colors that should look like this 0.3:0.5:0.4
     *
     * @param raw The string with just the portion.
     * @return an array of how it shall wight every gradient transition.
     */
    public Double[] parsePortions(@Nullable final String raw) {
        final int expectedSegments = this.colors.length;

        if (raw == null || raw.isEmpty()) {
            Double[] even = new Double[expectedSegments];
            Arrays.fill(even, 1.0 / expectedSegments);
            return even;
        }
        String[] split = raw.split(":");

        Validate.checkBoolean(split.length != expectedSegments, "Portions must match gradient segments: expected " +
                expectedSegments + " but got " + split.length);

        Double[] portions = new Double[split.length];
        double sum = 0;

        for (int i = 0; i < split.length; i++) {
            double v = Double.parseDouble(split[i].trim());
            Validate.checkBoolean(v < 0, "Portion part for gradient cannot be negative: " + v);
            portions[i] = v;
            sum += v;
        }

        if (Math.abs(sum - 1.0) > 0.0001) {
            for (int i = 0; i < portions.length; i++) {
                portions[i] /= sum;
            }
        }

        return portions;
    }

    /**
     * Split up colors from a string that is formated #E786C5:#D3FB5F
     *
     * @param raw The string with just the color codes.
     * @return an array of colors.
     */
    @Nonnull
    private Color[] parseColors(@Nonnull final String raw) {
        Validate.checkBoolean(raw.isEmpty(), "Gradient colors cannot be empty");

        String[] split = raw.split(":");
        List<Color> colors = new ArrayList<>();

        for (String s : split) {
            s = s.trim();
            Validate.checkBoolean(!StringUtility.isValidHexCode(s), "Invalid color in gradient: " + s);
            colors.add(Color.decode(normalizeHex(s)));
        }
        Validate.checkBoolean(colors.size() < 2, "Gradient requires at least 2 colors");
        return colors.toArray(new Color[0]);
    }

    private static String normalizeHex(final String hex) {
        if (hex.length() == 4) { // #RGB
            return "#" + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2)
                    + hex.charAt(3) + hex.charAt(3);
        }
        return hex;
    }
}