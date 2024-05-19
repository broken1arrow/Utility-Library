package org.broken.arrow.color.library.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.color.library.ChatColors;
import org.broken.arrow.color.library.Component;
import org.broken.arrow.color.library.TextTranslator;

import javax.annotation.Nonnull;

import static org.broken.arrow.color.library.utility.StringUtility.checkIfColor;

public class CreateComponent {

    private final TextTranslator textTranslator;
    private String text = "";
    public CreateComponent(TextTranslator textTranslator,String message) {
        this.textTranslator = textTranslator;
        this.text = message;
    }

    public JsonObject componentFormat(String defaultColor) {
        JsonArray jsonArray = new JsonArray();
        Component.Builder component = new Component.Builder();
        this.text = textTranslator.checkStringForGradient(this.text);

        defaultColor = (defaultColor == null || defaultColor.isEmpty()) ? "white" : defaultColor;

        StringBuilder builder = new StringBuilder(this.text.length());
        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < this.text.length(); i++) {
            char letter = this.text.charAt(i);
            boolean checkChar = false;
            boolean checkHex = false;

            if (isPotentialColorCode(this.text, i, letter)) {
                char msg = this.text.charAt(i + 1);
                checkChar = checkIfColor(msg);

                if (msg == '#') {
                    String hexString = getHexColorFromText(this.text, i);
                    checkChar = isValidHexCode(hexString);
                    if (checkChar) {
                        hex = new StringBuilder(hexString);
                        checkHex = true;
                    }
                }
            }

            if (checkChar) {
                i += processColorCode( builder, component, jsonArray, defaultColor, i, checkHex, hex);
                continue;
            }

            builder.append(letter);
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

    private int processColorCode( StringBuilder builder, Component.Builder component, JsonArray jsonArray,
                                 String defaultColor, int i, boolean checkHex, StringBuilder hex) {
        if (++i >= this.text.length()) return i;
        char letter = this.text.charAt(i);

        if (Character.isUpperCase(letter)) {
            letter = Character.toLowerCase(letter);
        }

        String format = checkHex ? hex.toString() : getChatColorByChar(letter);
        if (format == null) return i;

        if (builder.length() > 0) {
            addComponentToJsonArray(builder, component, jsonArray);
        }
        textTranslator.setColor(defaultColor, component, format);
        return checkHex ? format.length() + 1 : 1;
    }

    private String getChatColorByChar(char letter) {
        try {
            return ChatColors.getByChar(letter).getName();
        } catch (Exception ignore) {
            return null;
        }
    }

    private void addComponentToJsonArray(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        builder.setLength(0);
        jsonArray.add(component.build().toJson());
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
