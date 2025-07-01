package org.broken.arrow.library.color.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.library.color.ChatColors;
import org.broken.arrow.library.color.Component;
import org.broken.arrow.library.color.TextTranslator;

import javax.annotation.Nonnull;

import static org.broken.arrow.library.color.utility.StringUtility.checkIfColor;

public class CreateComponent {

    private final TextTranslator textTranslator;
    private String text = "";
    public CreateComponent(TextTranslator textTranslator,String message) {
        this.textTranslator = textTranslator;
        this.text = message;
    }

    public JsonObject componentFormat(String defaultColor) {
        int i = 0;
        JsonArray jsonArray = new JsonArray();
        Component.Builder component = new Component.Builder();
        this.text = textTranslator.checkStringForGradient(this.text);

        defaultColor = (defaultColor == null || defaultColor.isEmpty()) ? "white" : defaultColor;

        StringBuilder builder = new StringBuilder(this.text.length());
        StringBuilder hex = new StringBuilder();

        while (i < this.text.length()) {
            char letter = this.text.charAt(i);
            boolean checkChar = false;

            if (isPotentialColorCode(this.text, i, letter)) {
                char msg = this.text.charAt(i + 1);
                checkChar = checkIfColor(msg);

                if (msg == '#') {
                    String hexString = getHexColorFromText(this.text, i);
                    checkChar = isValidHexCode(hexString);
                    if (checkChar) {
                        hex = new StringBuilder(hexString);
                    }
                }
            }

            if (checkChar) {
                i += processColorCode( builder, component, jsonArray, defaultColor, i, hex);
                hex.setLength(0);
                continue;
            }
            builder.append(letter);
            i++;
        }

        finalizeComponent(builder, component, jsonArray);

        return buildJsonObject(jsonArray, component);
    }

    private boolean isPotentialColorCode(String message, int i, char letter) {
        return i + 1 < message.length() && (letter == ChatColors.COLOR_CHAR || letter == ChatColors.COLOR_AMPERSAND || letter == '<');
    }

    @Nonnull
    private String getHexColorFromText(String message, int i) {
        String hexString = message.substring(i + 1, i + 8);
        int lastIndex = hexString.indexOf('>');
        if (lastIndex != -1) {
            hexString = hexString.substring(0, lastIndex);
        }
        return hexString;
    }

    private boolean isValidHexCode(String hex) {
        return StringUtility.isValidHexCode(hex);
    }

    private int processColorCode( final StringBuilder builder,final  Component.Builder component,final  JsonArray jsonArray,
                                  final  String defaultColor,final int i,final StringBuilder hex) {
        final int index = i + 1;
        if (index >= this.text.length()) return index;
        char letter = this.text.charAt(index);

        if (Character.isUpperCase(letter)) {
            letter = Character.toLowerCase(letter);
        }
        boolean isHex = hex.length() > 0;
        String format = isHex ? hex.toString() : getChatColorByChar(letter);
        if (format == null) return index;

        if (builder.length() > 0) {
            jsonArray.add(buildComponentToJson(builder, component));
        }
        textTranslator.setColor(defaultColor, component, format);
        return isHex  ? format.length() + 2 : 1;
    }

    private String getChatColorByChar(char letter) {
        try {
            return ChatColors.getByChar(letter).getName();
        } catch (Exception ignore) {
            return null;
        }
    }

    private JsonObject buildComponentToJson(StringBuilder builder, Component.Builder component) {
        component.message(builder.toString());
        builder.setLength(0);
        return component.build().toJson();
    }

    private void finalizeComponent(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        jsonArray.add(component.build().toJson());
    }

    private JsonObject buildJsonObject(JsonArray jsonArray, Component.Builder component) {
        if (jsonArray.size() > 1) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("extra", jsonArray);
            jsonObject.addProperty("text", "");
            return jsonObject;
        }
        return component.build().toJson();
    }
}
