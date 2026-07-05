package org.broken.arrow.library.color.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ConversionsGradients {

    private ConversionsGradients() {
    }

    /**
     * Split up colors from a string that is formated #E786C5:#D3FB5F
     *
     * @param raw The string with just the color codes.
     * @return an array of colors.
     */
    public static Color[] parseColors(@Nonnull final String raw) {
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Gradient colors cannot be empty");
        }
        String[] split = raw.split(":");
        List<Color> colors = new ArrayList<>();

        for (String s : split) {
            s = s.trim();

            if (!StringUtility.isValidHexCode(s)) {
                throw new IllegalArgumentException("Invalid color in gradient: " + s);
            }

            colors.add(Color.decode(normalizeHex(s)));
        }

        if (colors.size() < 2) {
            throw new IllegalArgumentException("Gradient requires at least 2 colors");
        }

        return colors.toArray(new Color[0]);
    }

    /**
     * The portion wight of the colors that should look like this 0.3:0.5:0.4
     *
     * @param raw              The string with just the portion .
     * @param expectedSegments The amount of portions need match amount of colors provided.
     * @return an array of how it shall wight every gradient transition.
     */
    public static Double[] parsePortions(@Nullable final String raw, final int expectedSegments) {
        if (raw == null || raw.isEmpty()) {
            Double[] even = new Double[expectedSegments];
            Arrays.fill(even, 1.0 / expectedSegments);
            return even;
        }

        String[] split = raw.split(":");
        if (split.length != expectedSegments) {
            throw new IllegalArgumentException(
                    "Portions must match gradient segments: expected " +
                            expectedSegments + " but got " + split.length
            );
        }

        Double[] portions = new Double[split.length];
        double sum = 0;

        for (int i = 0; i < split.length; i++) {
            double v = Double.parseDouble(split[i].trim());
            if (v < 0) throw new IllegalArgumentException("Portion cannot be negative: " + v);

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

    private static String normalizeHex(final String hex) {
        if (hex.length() == 4) { // #RGB
            return "#" + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2)
                    + hex.charAt(3) + hex.charAt(3);
        }
        return hex;
    }
}