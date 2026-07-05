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
		String sub = message.substring(index);

		Matcher m = PATTERN.matcher(sub);
		if (!m.find() || m.start() != 0) {
			return null;
		}

		boolean hsv = m.group(1) != null;
		String colorsRaw = m.group(2);
		String portionsRaw = m.group(3);

		Color[] colors = ConversionsGradients.parseColors(colorsRaw);
		Double[] portions = ConversionsGradients.parsePortions(portionsRaw, colors.length);

		GradientMatch d = new GradientMatch();
		d.setType(hsv ? GradientType.HSV_GRADIENT_PATTERN  : GradientType.SIMPLE_GRADIENT_PATTERN);
		d.setColors(colors);
		d.setPortions(portions);
		d.setTagLength(m.group(0).length());

		return d;
	}

}