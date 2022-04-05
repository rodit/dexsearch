package xyz.rodit.dexsearch.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClassChecker {

    private final List<ClassLoader> loaders;

    public ClassChecker(Collection<ClassLoader> loaders) {
        this.loaders = new ArrayList<>(loaders);
    }

    public Class<?> get(String name) {
        for (ClassLoader loader : loaders) {
            try {
                return Class.forName(name, false, loader);
            } catch (ClassNotFoundException e) {
                // ignored
            }
        }

        return null;
    }
}
