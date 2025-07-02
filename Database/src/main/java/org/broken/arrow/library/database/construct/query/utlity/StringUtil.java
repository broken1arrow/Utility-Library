package org.broken.arrow.library.database.construct.query.utlity;

import java.util.List;
import java.util.StringJoiner;

public class StringUtil {

    private StringUtil() {
    }

    public static String repeat(String marker, int times) {
        StringBuilder sb = new StringBuilder(marker.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(marker);
        }
        return sb.toString();
    }

    public static String stringJoin(List<?> value) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Object constraints : value) {
            joiner.add(constraints.toString());
        }
        return joiner +"";
    }
}
