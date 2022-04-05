package xyz.rodit.dexsearch.utils;

public class EnumUtils {

    public static <T extends Enum<T>> T valueOfSafe(Class<T> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
