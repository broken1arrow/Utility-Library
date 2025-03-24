package org.broken.arrow.database.library.construct.query.utlity;

public class StringUtil {


    public static String repeat(String marker, int times) {
        StringBuilder sb = new StringBuilder(marker.length() * times);
        for (int i = 0; i < times; i++) {
            sb.append(marker);
        }
        return sb.toString();
    }
}
