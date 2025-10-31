package org.broken.arrow.library.itemcreator.meta.map.font.customdraw;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;

/**
 * Tracks active text formatting state while rendering.
 * Holds current color and active style flags such as bold or shadow and reset.
 */
public class RenderState {
    private final EnumSet<MapTextRenderer.TextStyle> textStyles = EnumSet.noneOf(MapTextRenderer.TextStyle.class);
    private Color currentColor = Color.WHITE;

    /**
     * Applies a Minecraft-style formatting code.
     * <ul>
     *     <li>{@code l} = bold</li>
     *     <li>{@code o} = shadow</li>
     *     <li>{@code r} = reset styles + white</li>
     *     <li>Otherwise = color code</li>
     * </ul>
     *
     * @param code the color code to check for.
     */
    public void applyFormattingCode(char code) {
        switch (code) {
            case 'l':
                textStyles.add(MapTextRenderer.TextStyle.BOLD);
                break;
            case 'o':
                textStyles.add(MapTextRenderer.TextStyle.SHADOW);
                break;
            case 'r':
                textStyles.clear();
                currentColor = Color.WHITE;
                break;
            default:
                currentColor = translateChatColor(code);
                break;
        }
    }

    /**
     * Checks if a style is currently active.
     *
     * @param textStyle the style to check
     * @return true if active
     */
    public boolean hasStyle(MapTextRenderer.TextStyle textStyle) {
        return textStyles.contains(textStyle);
    }

    /**
     *
     * @return Returns the color currently used for glyph drawing.
     */
    public Color getCurrentColor() {
        return currentColor;
    }

    /**
     * Set the color
     *
     * @param decode set the color for the charter.
     */
    public void setCurrentColor(@Nullable final Color decode) {
        currentColor = decode;
    }

    /**
     * Translates a Minecraft-style chat color code into a Color instance.
     * Does not handle formatting (bold, shadow), only RGB color.
     *
     * @param code the color code for vanillas minecraft.
     * @return returns the color or null if not a valid Minecraft color code.
     */
    @Nullable
    public Color translateChatColor(char code) {
        switch (code) {
            case '0':
                return new Color(0, 0, 0);
            case '1':
                return new Color(0, 0, 170);
            case '2':
                return new Color(0, 170, 0);
            case '3':
                return new Color(0, 170, 170);
            case '4':
                return new Color(170, 0, 0);
            case '5':
                return new Color(170, 0, 170);
            case '6':
                return new Color(255, 170, 0);
            case '7':
                return new Color(170, 170, 170);
            case '8':
                return new Color(85, 85, 85);
            case '9':
                return new Color(85, 85, 255);
            case 'a':
                return new Color(85, 255, 85);
            case 'b':
                return new Color(85, 255, 255);
            case 'c':
                return new Color(255, 85, 85);
            case 'd':
                return new Color(255, 85, 255);
            case 'e':
                return new Color(255, 255, 85);
            case 'f':
                return Color.WHITE;
            default:
                return null;
        }
    }

}