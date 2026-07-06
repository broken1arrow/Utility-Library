package org.broken.arrow.library.color.gradient.patterns;

import org.broken.arrow.library.color.TextTranslator.GradientType;
import org.broken.arrow.library.color.gradient.GradientMatch;
import org.broken.arrow.library.color.gradient.GradientPattern;
import org.broken.arrow.library.color.utility.ConversionsGradients;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MultiGradientPattern implements GradientPattern {
    private static final Pattern PATTERN = Pattern.compile("(gradients_hsv_|gradients_|hsv_)<([^>]+)>(?:_portion<([^>]+)>)?");

    @Override
    public GradientMatch tryParse(@NonNull String message, int index) {
        final String sub = message.substring(index);
        final Matcher m = PATTERN.matcher(sub);

        if (!m.find() || m.start() != 0) {
            return null;
        }
        final String gradientType = m.group(1);
        final boolean hsv = gradientType != null && (gradientType.startsWith("gradients_hsv") || gradientType.startsWith("hsv"));

        final String colorsRaw = m.group(2);
        final String portionsRaw = m.group(3);
        final ConversionsGradients gradients = ConversionsGradients.parse(colorsRaw);
        final Color[] colors = gradients.getColors();
        final Double[] portions = gradients.parsePortions(portionsRaw);

        final GradientMatch d = new GradientMatch();
        d.setType(hsv ? GradientType.HSV_GRADIENT_PATTERN : GradientType.SIMPLE_GRADIENT_PATTERN);
        d.setColors(colors);
        d.setPortions(portions);
        d.setTagLength(m.group(0).length());

        return d;
    }

}