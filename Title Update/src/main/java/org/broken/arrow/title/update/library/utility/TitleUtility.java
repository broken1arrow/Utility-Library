package org.broken.arrow.title.update.library.utility;

import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import org.broken.arrow.color.library.ChatColors;
import org.broken.arrow.color.library.TextTranslator;

import javax.annotation.Nonnull;

/**
 * Utility class for managing titles and JSON titles, does also offer color conversion support.
 */
public class TitleUtility {

    private String title;
    private JsonObject jsonObjectTitle;
    private final boolean defaultConvertColor;

    /**
     * Constructs a TitleUtility instance.
     *
     * @param defaultConvertColor This flag indicating whether color codes should be converted if set to true and false otherwise.
     */
    public TitleUtility(final boolean defaultConvertColor) {
        this.defaultConvertColor = defaultConvertColor;
    }

    /**
     * Sets the JSON object title. You need to format this so Minecraft can read
     * the JSON string, and this will be converted to a string.
     * <p>&nbsp;</p>
     * <p>
     * To ensure proper formatting, follow this structure:
     * For titles with multiple colors set in the same text, use the "extra" key and
     * an empty "text" element outside the array at the end of the JSON.
     * </p>
     * <p>
     * Example with multiple components:
     * </p>
     * <pre>
     * {
     *   "extra":[
     *      {
     *        "color":"gold",
     *        "text":"Test "
     *      },
     *      {
     *        "color":"dark_red",
     *        "bold":true,
     *        "text":"this"
     *       }
     *    ],
     *    "text":""
     * }
     * </pre>
     * Example with a single color set:
     * <pre>
     * {
     *   "color": "gold",
     *   "text": "Test this"
     * }
     * </pre>
     *
     * @param jsonObjectTitle The JSON object representing the title.
     */
    public void setJsonObjectTitle(@Nonnull final JsonObject jsonObjectTitle) {
        this.jsonObjectTitle = jsonObjectTitle;
    }

    /**
     * Sets the title text. The color conversion method used depends on what you set the flag to. Read more
     * further down.
     * <p>
     * If {@link #defaultConvertColor} is set to true, my color conversion methods will be used. Otherwise,
     * {@link ChatColor#translateAlternateColorCodes(char, String)} will be used for color conversion.
     *
     * @param title The title text.
     */
    public void setTitle(@Nonnull final String title) {
        this.title = title;
    }

    /**
     * Checks if a title is set.
     *
     * @return {@code true} if a title is set, {@code false} otherwise.
     */
    public boolean isTitleSet() {
        return this.jsonObjectTitle != null || title != null;
    }

    /**
     * Gets the title text based on the server version and the value of the {@link #defaultConvertColor} flag.
     * If the flag is set to true, this method attempts to translate colors using the Color conversion module.
     * <p>&nbsp;</p>
     * <p>
     * If you are using the {@link com.google.gson.JsonObject}, this method will only convert it to a string.
     * It will not translate colors or correct the JSON format if it doesn't follow Minecraft formatting rules
     * </p>
     *
     * @param serverVersion The version of the server.
     * @return The formatted title text, or {@code null} if no title is set.
     */
    public Object getTitle(float serverVersion) {
        if (serverVersion > 20.2F) {
            return getTitleNewVersions();
        }
        if (this.jsonObjectTitle != null && serverVersion > 13.0F)
            return this.jsonObjectTitle;
        if (this.title != null) {
            return getTitleLegacy(serverVersion);
        }
        return null;
    }

    private String getTitleNewVersions() {
        if (this.title == null)
            return "";
        if (this.jsonObjectTitle != null)
            return this.jsonObjectTitle.toString();
        if (!this.defaultConvertColor)
            return ChatColor.translateAlternateColorCodes('&', title);
        return TextTranslator.toSpigotFormat(title);
    }

    private Object getTitleLegacy(float serverVersion) {
        if (this.defaultConvertColor && serverVersion > 13.0F)
            return TextTranslator.toComponent(title);
        else {
            if (this.defaultConvertColor)
                return "'" + TextTranslator.toSpigotFormat(title) + "'";
            else {
                if (serverVersion >= 16.0F)
                    return TextTranslator.fromLegacyText(title, ChatColors.WHITE);
                else
                    return "'" + ChatColor.translateAlternateColorCodes('&', title) + "'";
            }
        }
    }
}
