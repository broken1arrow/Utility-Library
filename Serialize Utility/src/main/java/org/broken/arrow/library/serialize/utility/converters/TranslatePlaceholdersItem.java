package org.broken.arrow.library.serialize.utility.converters;

import org.broken.arrow.color.library.TextTranslator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class TranslatePlaceholdersItem {

    private static boolean hasTextTranslator;

    static {
        try {
            TextTranslator.getInstance();
            hasTextTranslator = true;
        } catch (NoClassDefFoundError e) {
            hasTextTranslator = false;
        }
    }

    private TranslatePlaceholdersItem() {
    }

    /**
     * Translates the {@link ItemStack}'s display name and lore, replacing any placeholders in the text.
     *
     * <p>Supports basic placeholder replacement using the given key-value map.
     * Color codes will be translated using {@link TranslatePlaceholdersItem#translateColorCodes}.</p>
     *
     * @param item         the {@link ItemStack} whose display name and lore should be updated.
     * @param placeholders a map of placeholders to replace (e.g., {@code %player% -> "Steve"}), or {@code null} to skip.
     * @return the updated {@link ItemStack}, or {@code null} if the input item was {@code null}.
     */
    public static ItemStack replacePlaceHolders(@Nullable final ItemStack item, @Nullable final Map<String, Object> placeholders) {
        return replacePlaceHolders(item, placeholders, TranslatePlaceholdersItem::translateColorCodes);
    }

    /**
     * Translates the {@link ItemStack}'s display name and lore, replacing placeholders and applying final text transformations.
     *
     * <p>This overload gives more control over how the text is transformed after placeholders are replaced,
     * allowing customization like color code translation, markdown stripping, or gradient insertion.</p>
     *
     * @param item         the {@link ItemStack} to update.
     * @param placeholders a map of placeholders to replace (e.g., {@code %player% -> "Steve"}), or {@code null} to skip.
     * @param callBackText a function to apply to each finalized text value before it's set on the item.
     *                     Typically used to handle color codes or other string post-processing.
     * @return the updated {@link ItemStack}, or {@code null} if the input item was {@code null}.
     */
    public static ItemStack replacePlaceHolders(@Nullable final ItemStack item, @Nullable final Map<String, Object> placeholders, @Nonnull final UnaryOperator<String> callBackText) {
        if (item == null)
            return null;
        if (placeholders != null)
            placeholders.forEach((key, value) -> replacePlaceHolder(item, key, value, callBackText));

        return item;
    }

    /**
     * Replaces placeholders in the {@link ItemStack}'s display name and lore.
     *
     * <p>If the placeholder exists in either the display name or any lore line, it will be replaced
     * with the provided value. If the value is a {@link List}, it will be processed accordingly and
     * lore will be updated with formatted lines. The {@code callBackText} function is applied after
     * replacement to perform transformations such as color code translation.</p>
     *
     * @param item         the {@link ItemStack} to modify.
     * @param placeHolder  the placeholder string to search for (e.g. {@code "%player%"} or {@code "[object]"}).
     * @param value        the replacement value; can be a {@link List} or any {@link Object}.
     * @param callBackText a post-processing function applied to each updated line.
     */
    private static void replacePlaceHolder(final ItemStack item, final String placeHolder, final Object value, @Nonnull final UnaryOperator<String> callBackText) {
        if (item == null)
            return;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        applyChangesToDisplayName(placeHolder, value, callBackText, meta);
        applyChangesToLore(placeHolder, value, callBackText, meta);

        item.setItemMeta(meta);
    }

    private static void applyChangesToDisplayName(final String placeHolder, final Object value, @Nonnull final UnaryOperator<String> callBackText, final ItemMeta meta) {
        if (!meta.hasDisplayName()) return;

        final String displayName = meta.getDisplayName();
        if (displayName.contains(placeHolder)) {
            final StringBuilder placeholderValue = new StringBuilder();
            if (value instanceof List) {
                ((List<?>) value).forEach(o -> placeholderValue.append(o.toString()));
            } else {
                placeholderValue.append(value != null ? value.toString() : "");
            }
            meta.setDisplayName(callBackText.apply(displayName.replace(placeHolder, placeholderValue.toString())));
        }
    }

    private static void applyChangesToLore(final String placeHolder, Object value, @Nonnull final UnaryOperator<String> callBackText, final ItemMeta meta) {
        List<String> lore = meta.getLore();
        if (lore == null) return;

        if (value instanceof List)
            lore = getStringList(placeHolder, split((List<?>) value, 50), lore, callBackText);
        else
            lore = lore.stream()
                    .map(line -> getTextProcessed(placeHolder, value, callBackText, line))
                    .collect(Collectors.toList());
        meta.setLore(lore);
    }

    @Nullable
    private static String getTextProcessed(String placeHolder, Object value, @Nonnull UnaryOperator<String> callBackText, String line) {
        if(line == null){
            return null;
        }
        return callBackText.apply(line.replace(placeHolder, value != null ? value.toString() : ""));
    }

    @Nonnull
    private static List<String> getStringList(final String placeHolder, final List<?> value, final List<String> lore, final UnaryOperator<String> callBackText) {
        final List<String> list = new ArrayList<>(lore.size());

        int index = getIndexOf(placeHolder, lore);

        for (final String itemLore : lore) {
            final int indexOfPlaceHolder = itemLore.indexOf(placeHolder);
            if (index > 0 && indexOfPlaceHolder > 0) {
                if (index > list.size()) {
                    final int expand = (index - list.size()) + 1;
                    for (int i = 0; i < expand; i++)
                        list.add(null);
                }
                for (int i = 0; i < value.size(); i++) {
                    list.add(i + index, callBackText.apply(itemLore.replace(placeHolder, value.get(i) + "")));
                }
            } else
                list.add(itemLore.replace(placeHolder, ""));
        }
        return list;
    }

    private static int getIndexOf(String placeHolder, List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            final String itemLore = lore.get(i);
            if (itemLore.contains(placeHolder))
                return i;
        }
        return -1;
    }

    public static List<String> split(final List<?> input, final int maxLineLength) {
        List<String> output = new ArrayList<>();

        for (Object line : input) {
            StringTokenizer tok = new StringTokenizer(line + "", " ");
            StringBuilder currentLine = new StringBuilder();

            while (tok.hasMoreTokens()) {
                String word = tok.nextToken();
                String color = "";
                if (currentLine.length() + word.length() > maxLineLength) {
                    output.add(currentLine.toString().trim());

                    String start = currentLine.substring(0, Math.min(2, currentLine.length()));
                    if (start.contains("&") || start.contains("§")) {
                        color = start;
                    }
                    currentLine = new StringBuilder();
                }
                currentLine.append(color).append(word).append(" ");
            }
            if (currentLine.length() > 0) {
                output.add(currentLine.toString().trim());
            }
        }
        return output;
    }

    /**
     * Translates color codes in the given string using either {@link TextTranslator} (if available),
     * or falls back to {@link ChatColor#translateAlternateColorCodes(char, String)}.
     *
     * <p>If this utility is shaded into your own plugin, it will attempt to use {@code TextTranslator}.
     * If you're using the Utility Library by installing its plugin JAR (rather than shading it),
     * this behavior is always enabled.</p>
     *
     * @param text the text to translate
     * @return the translated string with appropriate color formatting
     */
    @Nonnull
    private static String translateColorCodes(final String text) {
        if (hasTextTranslator)
            return TextTranslator.toSpigotFormat(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}
