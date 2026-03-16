package org.lingchat.lingchatcommon.utils;

import java.util.UUID;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }
        return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
    }

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }
}