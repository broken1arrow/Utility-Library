package org.broken.arrow.library.itemcreator.meta.map.color.parser;

import org.broken.arrow.library.itemcreator.meta.map.font.customdraw.RenderState;

import javax.annotation.Nonnull;
import java.awt.*;

public final class AmpersandHexColorParser implements ColorParser {

    /**
     * Attempts to parse a formatting sequence starting at position `i`.
     * Supports:
     * <ui>
     *  <li>{@code &a   §c}   → style/color codes </li>
     *  <li> {@code &#fff}    → 3-digit hex  </li>
     *  <li> {@code &#ffffff}  → 6-digit hex  </li>
     * </ui>
     * @return how many characters were consumed (0 = not a formatting code)
     */
    @Override
    public int tryParse(@Nonnull String text, int i, @Nonnull RenderState state) {
        if (i + 1 >= text.length())
            return 0;

        char prefix = text.charAt(i);
        if (prefix != '&' && prefix != '§')
            return 0;

        char next = text.charAt(i + 1);
        if (next != '#') {
            char code = text.charAt(i + 1);
            state.applyFormattingCode(code);
            return 2;
        }

        int hexStart = i + 2;
        int available = text.length() - hexStart;
        HexData hexColor = getHexData(text, available, hexStart);

        if (hexColor.getHex() != null) {
            try {
                state.setCurrentColor(Color.decode("#" + hexColor.getHex()));
            } catch (NumberFormatException ignored) {
                state.setCurrentColor(Color.WHITE);
            }
            return 2 + hexColor.getLength();
        }
        return 0;
    }

    private HexData getHexData(final String text, final int available, final int hexStart) {
        String hex = null;
        int length = 0;

        if (available >= 6) {
            String cand = text.substring(hexStart, hexStart + 6);
            if (isHex(cand)) {
                hex = cand;
                length = 6;
            }
        }
        if (hex == null && available >= 3) {
            String cand = text.substring(hexStart, hexStart + 3);
            if (isHex(cand)) {
                hex = expand3To6(cand);
                length = 3;
            }
        }
        return new HexData(hex, length);
    }


    private boolean isHex(@Nonnull final String s) {
        for (char c : s.toCharArray()) {
            if (Character.digit(c, 16) == -1)
                return false;
        }
        return true;
    }

    private String expand3To6(@Nonnull final String shortHex) {
        return "" + shortHex.charAt(0) + shortHex.charAt(0) +
                shortHex.charAt(1) + shortHex.charAt(1) +
                shortHex.charAt(2) + shortHex.charAt(2);
    }


    private static class HexData {
        private final String hex;
        private final int length;

        public HexData(final String hex, final int length) {
            this.hex = hex;
            this.length = length;
        }

        public String getHex() {
            return hex;
        }

        public int getLength() {
            return length;
        }
    }
}
