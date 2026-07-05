package org.broken.arrow.library.color.gradient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Defines a strategy for identifying and parsing custom gradient tags within raw text strings.
 * <p>
 * Implementations of this interface use specific syntactic patterns (such as regular expressions)
 * to scan a text message at a given index. If a valid gradient tag is matched, the implementation
 * extracts the color details, interpolation settings, and tag dimensions into a {@link GradientMatch}.
 *
 * @see GradientMatch
 * @see org.broken.arrow.library.color.gradient.patterns.MultiGradientPattern
 * @see org.broken.arrow.library.color.gradient.patterns.SimpleGradientPattern
 */
public interface GradientPattern {

    /**
     * Attempts to parse a gradient tag from the provided message starting at the specified index.
     * <p>
     * The implementation must check if the substring starting exactly at {@code index}
     * conforms to its expected gradient syntax. While it should only scan forward into the string,
     * the match <b>must</b> begin exactly at the designated index.
     * <p>
     * Successful implementations must populate the match length using {@link GradientMatch#setTagLength(int)}
     * so the parsing engine can correctly strip the tag from the final output.
     *
     * @param message the full source text message being processed
     * @param index   the character index within the message where parsing should begin
     * @return a {@link GradientMatch} containing the extracted gradient properties if a tag
     *         is successfully matched at the index; {@code null} otherwise
     */
    @Nullable
    GradientMatch tryParse(@Nonnull final String message, final int index);

}