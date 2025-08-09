package org.broken.arrow.library.database.construct.query.utlity;

import java.util.List;
import java.util.StringJoiner;

/**
 * Utility class providing basic string manipulation methods.
 * <p>
 * This class offers compatibility methods similar to newer Java API features,
 * such as string repetition and joining collections into a single string.
 * It is designed for environments where these newer methods may not be available.
 * </p>
 */
public class StringUtil {

    private StringUtil() {
    }

    /**
     * Repeats the given string marker a specified number of times.
     * <p>
     * Similar to Java 11's {@code String.repeat()}, but compatible with older Java versions.
     * </p>
     *
     * @param marker the string to be repeated
     * @param times  the number of times to repeat the string
     * @return a new string consisting of the marker repeated {@code times} times
     */
    public static String repeat(String marker, int times) {
        StringBuilder sb = new StringBuilder(marker.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(marker);
        }
        return sb.toString();
    }

    /**
     * Joins a list of objects into a single string, separated by commas.
     * <p>
     * Each object's {@code toString()} method is called to generate the string representation.
     * </p>
     *
     * @param value the list of objects to join
     * @return a single string with all objects joined by ", "
     */
    public static String stringJoin(List<?> value) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Object constraints : value) {
            joiner.add(constraints.toString());
        }
        return joiner +"";
    }
}
