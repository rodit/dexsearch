package xyz.rodit.dexsearch.utils;

import java.util.HashMap;
import java.util.Map;

public class TypeUtils {

    private static final Map<String, String> primDexToJava = new HashMap<>();

    static {
        primDexToJava.put("B", "byte");
        primDexToJava.put("C", "char");
        primDexToJava.put("D", "double");
        primDexToJava.put("F", "float");
        primDexToJava.put("I", "int");
        primDexToJava.put("J", "long");
        primDexToJava.put("S", "short");
        primDexToJava.put("Z", "boolean");
        primDexToJava.put("V", "void");
    }

    public static String toJavaType(String dexName) {
        if (dexName.endsWith(";")) {
            return dexName.substring(1, dexName.length() - 1).replace("/", ".");
        } else if (primDexToJava.containsKey(dexName)) {
            return primDexToJava.get(dexName);
        }

        return dexName;
    }

    public static String toDexType(String javaName) {
        return "L" + javaName.replace('.', '/') + ";";
    }
}
