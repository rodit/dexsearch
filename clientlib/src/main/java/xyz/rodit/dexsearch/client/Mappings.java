package xyz.rodit.dexsearch.client;

import com.google.gson.Gson;
import de.robv.android.xposed.XposedHelpers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mappings {

    private static Map<String, ClassMapping> mappings;
    private static ClassLoader classLoader;

    public static ClassMapping get(String name) {
        return mappings.get(name);
    }

    public static Map<String, ClassMapping> loadMappings(ClassLoader loader, InputStream stream) {
        classLoader = loader;
        Gson gson = new Gson();
        BindingEntry entry = gson.fromJson(new InputStreamReader(stream), BindingEntry.class);
        mappings = new HashMap<>();
        for (ClassMapping mapping : entry.mappings) {
            mappings.put(mapping.getNiceClassName(), mapping);
        }

        return mappings;
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public static Class<?> getClass(String className) {
        return mappings.containsKey(className) ? XposedHelpers.findClass(mappings.get(className).getDexClassName(), classLoader) : null;
    }

    public static class BindingEntry {
        public long build;
        public List<ClassMapping> mappings = new ArrayList<>();
    }
}
