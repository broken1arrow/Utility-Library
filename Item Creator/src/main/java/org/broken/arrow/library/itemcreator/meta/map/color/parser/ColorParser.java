package org.broken.arrow.library.itemcreator.meta.map.color.parser;

import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;

public interface ColorParser {
    /**
     * @param text        Full user input
     * @param i           Current index
     * @param renderState Modify if parsed successfully
     * @return number of characters consumed (0 = not parsed)
     */
    int tryParse(String text, int i, RenderState renderState);
}


