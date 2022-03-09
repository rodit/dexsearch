package xyz.rodit.dexsearch.codegen;

import java.io.File;

public class CodegenUtils {

    public static String getCamelCase(String prefix, String name) {
        return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static File resolvePackageDirectory(File root, String packageName) {
        if (packageName == null) {
            return root;
        }

        String[] parts = packageName.split("\\.");
        File current = root;
        for (String part : parts) {
            current = new File(current, part);
        }

        return current;
    }
}
