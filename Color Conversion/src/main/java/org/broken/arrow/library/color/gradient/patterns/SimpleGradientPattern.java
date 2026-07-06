package org.broken.arrow.library.color.gradient.patterns;

import org.broken.arrow.library.color.TextTranslator.GradientType;
import org.broken.arrow.library.color.gradient.GradientMatch;
import org.broken.arrow.library.color.gradient.GradientPattern;
import org.broken.arrow.library.color.utility.ConversionsGradients;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGradientPattern implements GradientPattern {
	private static final Pattern SIMPLE_GRADIENT = Pattern.compile("<(#[0-9a-fA-F]{6}:#[0-9a-fA-F]{6})>");

	@Override
	public GradientMatch tryParse(@NonNull String message, int index) {
		final String sub = message.substring(index);
		final Matcher m = SIMPLE_GRADIENT.matcher(sub);
		if (!m.lookingAt()) {
			return null;
		}

		final String colorsRaw = m.group(1);
		final ConversionsGradients gradients = ConversionsGradients.parse(colorsRaw);
		final Color[] colors = gradients.getColors();

		final GradientMatch d = new GradientMatch();
		d.setType(GradientType.SIMPLE_GRADIENT_PATTERN);
		d.setColors(colors);
		d.setPortions(null);
		d.setTagLength(m.group(0).length());
		return d;
	}

}