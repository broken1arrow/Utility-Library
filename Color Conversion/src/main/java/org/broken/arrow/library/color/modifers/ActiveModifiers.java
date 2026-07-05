package org.broken.arrow.library.color.modifers;

import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.color.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tracks and manages active text formatting modifiers (styles and colors),
 * used for parsing and translating Minecraft-style chat components.
 * <p>
 * This class maintains state for formatting options such as bold, italic,
 * underline, strikethrough, and obfuscation (magic), as well as the active color.
 */
public class ActiveModifiers {
    private final String defaultColor;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean strikethrough;
    private boolean magic;
    private String currentColor;

    /**
     * Constructs a new {@code ActiveModifiers} instance with a specified default color.
     *
     * @param defaultColor the fallback color code to use when formatting is reset
     */
    public ActiveModifiers(@Nonnull final String defaultColor) {
        this.defaultColor = defaultColor;
        this.currentColor = defaultColor;
    }

    /**
     * Updates the active modifiers based on the provided format or color name.
     * <p>
     * If the input matches a styling modifier (e.g., bold, italic), that specific
     * modifier is enabled. If it matches a reset code, all modifiers and colors return
     * to their defaults. If it is a new color code, existing style modifiers are cleared
     * and the active color is updated.
     *
     * @param format the format name or color code to apply, which may be null
     */
    public void update(@Nullable final String format) {
        final String string = format != null ? format.toLowerCase() : "";
        if (ChatColors.BOLD.getName().equals(string)) bold = true;
        else if (ChatColors.ITALIC.getName().equals(string)) italic = true;
        else if (ChatColors.UNDERLINE.getName().equals(string)) underline = true;
        else if (ChatColors.STRIKETHROUGH.getName().equals(string)) strikethrough = true;
        else if (ChatColors.MAGIC.getName().equals(string)) magic = true;
        else if (ChatColors.RESET.getName().equals(string)) {
            reset();
        } else {
            resetFormats();
            if (!string.isEmpty()) {
                this.currentColor = string;
            }
        }
    }

    /**
     * Applies all currently active styles and colors to the provided component builder.
     *
     * @param component the component builder to apply these styles to
     */
    public void applyTo(@Nonnull final Component.Builder component) {
        if (bold) component.bold(true);
        if (italic) component.italic(true);
        if (underline) component.underline(true);
        if (strikethrough) component.strikethrough(true);
        if (magic) component.obfuscated(true);
        if (currentColor != null && !currentColor.isEmpty()) {
            component.colorCode(currentColor);
        }
    }

    /**
     * Compiles the currently active style modifiers into a legacy Minecraft
     * formatting code string (using the {@code §} section sign).
     * <p>
     * Note: This only returns the style modifiers (e.g., {@code §l§o}) and
     * does not include the active color code.
     *
     * @return a string containing the active legacy formatting codes
     */
    public String getLegacyFormatCodes() {
        StringBuilder builder = new StringBuilder();
        if (bold) builder.append("§l");
        if (italic) builder.append("§o");
        if (underline) builder.append("§n");
        if (strikethrough) builder.append("§m");
        if (magic) builder.append("§k");
        return builder.toString();
    }

    /**
     * Clears all active text style formats (bold, italic, underline,
     * strikethrough, and magic) while keeping the current color intact.
     */
    private void resetFormats() {
        bold = italic = underline = strikethrough = magic = false;
    }

    /**
     * Resets all formatting styles to false and reverts the active color
     * back to the defined {@code defaultColor}.
     */
    public void reset() {
        resetFormats();
        currentColor = defaultColor;
    }
}