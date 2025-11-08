package org.broken.arrow.library.itemcreator.meta.map.font.customdraw;

import org.broken.arrow.library.itemcreator.ItemCreator;
import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.font.CharacterSprite;
import org.broken.arrow.library.itemcreator.meta.map.font.MapFontWrapper;
import org.broken.arrow.library.itemcreator.meta.map.pixel.TextOverlay;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Responsible for rendering text using a custom pixel-based {@link MapFontWrapper} onto a {@link MapCanvas}.
 * <p>
 * This renderer supports:
 * <ul>
 *     <li>Minecraft-style chat color codes using {@code &} or {@code §}</li>
 *     <li>Bold rendering (duplicate render shifted by +1 pixel)</li>
 *     <li>Shadow rendering (render offset +1,+1 with darker color)</li>
 *     <li>Per-character spacing defined by {@link CharacterSprite#getSpacing()}</li>
 *     <li>Fallback spacing via {@link MapFontWrapper#applyDefaultFontSpacing(char)}</li>
 * </ul>
 * <p>
 * The text is rendered pixel-by-pixel based on boolean sprite data.
 * Each visible pixel is drawn directly into the map canvas at the computed coordinate.
 */
public class MapTextRenderer {
    private final MapRendererData mapRendererData;
    private final MapCanvas canvas;
    private final MapFontWrapper font;
    private final String text;


    /**
     * Creates a new custom font renderer.
     *
     * @param canvas          the target map canvas to draw on
     * @param mapRendererData the rendered data instance with your global settings.
     * @param textOverlay     the custom text overlay wrapper providing character sprites and spacing rules
     */
    public MapTextRenderer(@Nonnull final MapCanvas canvas, @Nonnull final MapRendererData mapRendererData, @Nonnull final TextOverlay textOverlay) {
        this.canvas = canvas;
        this.mapRendererData = mapRendererData;
        this.font = textOverlay.getMapFontWrapper();
        this.text = textOverlay.getText();
    }


    /**
     * Draws the configured text to the map canvas starting at the given coordinate.
     * <p>
     * Supports Minecraft formatting codes:
     * <ul>
     *     <li>{@code &0 - &f} color codes</li>
     *     <li>{@code &l} bold</li>
     *     <li>{@code &o} shadow</li>
     *     <li>{@code &r} reset color + styles</li>
     * </ul>
     * <p>
     * Characters without a sprite will advance using the default font spacing.
     *
     * @param x starting X position in pixels
     * @param y starting Y position in pixels
     */
    public void drawCustomFontText(int x, int y) {
        int cursorX = x;
        RenderState renderState = new RenderState();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            int consumed;
            consumed = this.font.applyColorParser(text, i, renderState);
            if (consumed <= 0)
                consumed = this.mapRendererData.applyGlobalColorParser(text, i, renderState);

            if (consumed > 0) {
                i += consumed - 1;
                continue;
            }

            CharacterSprite sprite = font.getChar(ch);
            if (sprite == null) {
                cursorX += font.applyDefaultFontSpacing(ch);
                continue;
            }

            if (renderState.hasStyle(TextStyle.SHADOW)) {
                drawPixelsOffset(sprite, cursorX + 1, y + 1, darker(renderState.getCurrentColor()));
            }

            drawPixelsOffset(sprite, cursorX, y, renderState.getCurrentColor());

            if (renderState.hasStyle(TextStyle.BOLD)) {
                drawPixelsOffset(sprite, cursorX + 1, y, renderState.getCurrentColor());
            }

            cursorX += sprite.getWidth() + sprite.getSpacing();
            if (renderState.hasStyle(TextStyle.BOLD)) {
                cursorX += 1;
            }
        }
    }


    /**
     * Draws all visible pixels of a sprite at an offset.
     * Any pixel in the sprite marked as {@code true} is drawn to the final canvas.
     *
     * @param sprite the character sprite to render
     * @param startX destination x-coordinate
     * @param startY destination y-coordinate
     * @param color  the color used for visible pixels
     */
    private void drawPixelsOffset(CharacterSprite sprite, int startX, int startY, Color color) {
        boolean[] pixels = sprite.getData();
        int w = sprite.getWidth();
        int h = sprite.getHeight();

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (pixels[py * w + px]) {
                    setMapPixel(startX + px, startY + py, color);
                }
            }
        }
    }

    /**
     * Writes a single pixel to the map canvas.
     * <p>
     * Spigot 1.20+ uses {@link MapCanvas#setPixelColor(int, int, java.awt.Color)},
     * older versions use {@link MapPalette#matchColor(Color)}.
     *
     * @param x      location where draw in digonal direction.
     * @param y      location where draw in vertical direction.
     * @param color  the color to set for the pixel
     */
    private void setMapPixel( int x, int y, Color color) {
        if (ItemCreator.getServerVersion() < 20.0F) {
            this.canvas.setPixel(x, y, MapPalette.matchColor(color));
        } else {
            this.canvas.setPixelColor(x, y, color);
        }
    }

    /**
     * Creates a slightly darker version of the given color. Used for shadow rendering.
     *
     * @param color the current color you want to be darker.
     * @return return a darker tone of your color.
     */
    private Color darker(Color color) {
        return new Color(
                Math.max(0, color.getRed() - 40),
                Math.max(0, color.getGreen() - 40),
                Math.max(0, color.getBlue() - 40)
        );
    }


    /**
     * Available formatting styles applied during rendering.
     */
    public enum TextStyle {
        /**
         * Draws a second copy of the glyph shifted by +1px horizontally.
         */
        BOLD,
        /**
         * Draws a darker copy of the glyph at +1,+1 (shadow effect).
         */
        SHADOW,
        /**
         * Resets styles and color to white.
         */
        RESET
    }

}
