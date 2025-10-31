package org.broken.arrow.library.itemcreator.meta.map.color.parser;

import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;

import javax.annotation.Nonnull;

/**
 * Handle the colors from a text and provide the length of your color code
 * to ignore when create the text.
 */
@FunctionalInterface
public interface ColorParser {
    /**
     * @param text        Full user input
     * @param i           Current index
     * @param renderState Modify if parsed successfully
     * @return number of characters consumed (0 = not parsed)
     */
    int tryParse(@Nonnull final String text, final int i,@Nonnull final RenderState renderState);
}


