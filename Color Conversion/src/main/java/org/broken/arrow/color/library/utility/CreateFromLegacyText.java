package org.broken.arrow.color.library.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.broken.arrow.color.library.ChatColors;
import org.broken.arrow.color.library.Component;
import org.broken.arrow.color.library.TextTranslator;

public class CreateFromLegacyText {

    private CreateFromLegacyText() {
    }

    public static JsonObject fromLegacyText(String message, ChatColors defaultColor) {
        if (message == null) {
            return createEmptyJsonObject();
        }
        int i = 0;
        StringBuilder builder = new StringBuilder();
        Component.Builder component = new Component.Builder();
        JsonArray jsonArray = new JsonArray();

        while (i < message.length()){
            char c = message.charAt(i);
            if (c == ChatColors.COLOR_CHAR || c == ChatColors.COLOR_AMPERSAND) {
                i++;
                if (i >= message.length()) {
                    break;
                }
                c = message.charAt(i);
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                ChatColors format = extractColorFormat(message, i, c);
                if (format != null) {
                    i += handleFormat(builder, component, jsonArray, defaultColor, format,  c);
                    continue;
                }
            }
            builder.append(c);
            i++;
        }

        finalizeComponent(builder, component, jsonArray);

        return buildJsonObject(jsonArray, component);
    }

    private static JsonObject createEmptyJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", "");
        return jsonObject;
    }

    private static ChatColors extractColorFormat(String message, int i, char c) {
        if (c == 'x' && i + 12 < message.length()) {
            StringBuilder hex = new StringBuilder("#");
            for (int j = 0; j < 6; j++) {
                hex.append(message.charAt(i + 2 + (j * 2)));
            }
            return parseHexColor(hex.toString());
        }
        return ChatColors.getByChar(c);
    }

    private static ChatColors parseHexColor(String hex) {
        try {
            return ChatColors.of(hex);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static int handleFormat(StringBuilder builder, Component.Builder component, JsonArray jsonArray,
                                    ChatColors defaultColor, ChatColors format, char c) {
        if (builder.length() > 0) {
            addComponentToJsonArray(builder, component, jsonArray);
        }
        TextTranslator.getInstance().setColor(
                defaultColor != null ? defaultColor.getName() : "",
                component,
                format.getName()
        );
        return c == 'x' ? 13 : 0;
    }

    private static void addComponentToJsonArray(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        builder.setLength(0);
        jsonArray.add(component.build().toJson());
    }

    private static void finalizeComponent(StringBuilder builder, Component.Builder component, JsonArray jsonArray) {
        component.message(builder.toString());
        jsonArray.add(component.build().toJson());
    }

    private static JsonObject buildJsonObject(JsonArray jsonArray, Component.Builder component) {
        if (jsonArray.size() > 1) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("extra", jsonArray);
            jsonObject.addProperty("text", "");
            return jsonObject;
        }
        return component.build().toJson();
    }

}
