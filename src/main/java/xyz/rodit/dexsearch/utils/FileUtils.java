package xyz.rodit.dexsearch.utils;

import java.io.File;

public class FileUtils {

    public static boolean createParentDir(File file) {
        File parent = file.getParentFile();
        if (parent != null) {
            return parent.mkdirs();
        }

        return false;
    }
}
