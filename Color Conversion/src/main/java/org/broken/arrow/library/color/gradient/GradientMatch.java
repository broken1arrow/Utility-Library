package org.broken.arrow.library.color.gradient;

import org.broken.arrow.library.color.TextTranslator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * Holds the metadata resulting from a successfully parsed gradient tag.
 * <p>
 * This class acts as a data transfer object, capturing the gradient type,
 * color array, distribution portions, and the raw character length of the
 * matched token so the parsing engine knows how to advance through the string.
 */
public class GradientMatch {
    private TextTranslator.GradientType  type ;
    private Color[] colors;
    private Double[] portions;
    private int tagLength;

    /**
     * Sets the color interpolation strategy (e.g., RGB vs. HSV).
     *
     * @param type the gradient type to apply
     */
    public void setType(@Nonnull final TextTranslator.GradientType  type) {
        this.type = type;
    }

    /**
     * Sets the array of colors that make up the gradient sequence.
     *
     * @param colors an array of {@link Color} objects in the order they should appear
     */
    public void setColors(@Nonnull final Color[] colors) {
        this.colors = colors;
    }

    /**
     * Sets the proportional width/step distribution for each color in the gradient.
     *
     * @param portions an array of fractional doubles defining color spacing bounds
     */
    public void setPortions(@Nullable final Double[] portions) {
        this.portions = portions;
    }

    /**
     * Sets the character length of the raw gradient tag string.
     * <p>
     * This is used by parsers to determine how many characters to skip or strip
     * from the source text message during processing.
     *
     * @param tagLength the total character length of the matched tag
     */
    public void setTagLength(final int tagLength) {
        this.tagLength = tagLength;
    }

    /**
     * Gets the interpolation type of the gradient.
     *
     * @return the active {@link TextTranslator.GradientType}
     */
    @Nonnull
    public TextTranslator.GradientType getType() {
        if(type == null)
            return TextTranslator.GradientType.SIMPLE_GRADIENT_PATTERN;
        return type;
    }

    /**
     * Gets the array of colors defined for this gradient sequence.
     *
     * @return an array of {@link Color} objects
     */
    @Nonnull
    public Color[] getColors() {
        if(this.colors == null)
            return new Color[0];
        return colors;
    }

    /**
     * Gets the distribution portions defining how the colors shift across the gradient.
     *
     * @return an array of color portions, or null if uniform distribution is assumed
     */
    @Nullable
    public Double[] getPortions() {
        return portions;
    }

    /**
     * Gets the character length of the raw matched gradient tag.
     * <p>
     * This indicates how far forward the parsing head should advance in the original string.
     *
     * @return the total character length of the token tag
     */
    public int getTagLength() {
        return tagLength;
    }
}