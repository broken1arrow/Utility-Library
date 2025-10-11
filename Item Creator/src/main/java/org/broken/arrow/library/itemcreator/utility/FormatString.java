package org.broken.arrow.library.itemcreator.utility;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public class FormatString {

    private final StringBuilder formatedString;

    /**
     * Create a formated string in a json style format.
     *
     * @param builder the string builder instance you want to use.
     */
    public FormatString(@Nonnull final StringBuilder builder) {
        this.formatedString = builder;
    }

    /**
     * Append your value for a string, map or a collection.
     *
     * @param key    the key you want to attach to the value.
     * @param value  the value to format.
     * @param indent amount of spaces to add.
     */
    public void appendFieldRecursive(String key, Object value, int indent) {
        if (value == null) return;
        if (checkIfCollectionEmpty(value)) return;

        String prefix = indent(indent);

        if (key != null) {
            formatedString.append(prefix).append("\"").append(key).append("\": ");
        } else {
            formatedString.append(prefix);
        }

        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            formatedString.append("{\n");
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                appendFieldRecursive(String.valueOf(entry.getKey()), entry.getValue(), indent + 1);
            }
            trimTrailingComma();
            formatedString.append(prefix).append("},\n");

        } else if (value instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) value;
            formatedString.append("[\n");
            for (Object obj : collection) {
                appendFieldRecursive(null, obj, indent + 1);
            }
            trimTrailingComma();
            formatedString.append(prefix).append("],\n");

        } else if (value instanceof String) {
            formatedString.append("\"").append(value).append("\",\n");

        } else {
            addNewline(value, indent, prefix);
        }
    }

    /**
     * Finalize the string created.
     *
     * @return the string builder instance with your set data.
     */
    public StringBuilder finalizeString() {
        trimTrailingComma();
        formatedString.append("}");
        return formatedString;
    }

    private void addNewline(final Object value, final int indent, final String prefix) {
        String text = value.toString();
        if (text.contains("\n")) {
            formatedString.append("{\n");
            String[] lines = text.split("\n");
            for (String line : lines) {
                formatedString.append(indent(indent + 1)).append(line.trim()).append("\n");
            }
            formatedString.append(prefix).append("},\n");
        } else {
            formatedString.append(value).append(",\n");
        }
    }

    private void trimTrailingComma() {
        int len = formatedString.length();
        if (len > 2 && formatedString.substring(len - 2).equals(",\n")) {
            formatedString.setLength(len - 2);
            formatedString.append('\n');
        }
    }

    private String indent(int level) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < level * 2; i++) {
            spaces.append(' ');
        }
        return spaces.toString();
    }

    private static boolean checkIfCollectionEmpty(final Object value) {
        return (value instanceof Collection && ((Collection<?>) value).isEmpty())
                || (value instanceof Map && ((Map<?, ?>) value).isEmpty());
    }


}
