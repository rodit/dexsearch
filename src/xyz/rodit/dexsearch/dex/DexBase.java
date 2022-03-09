package xyz.rodit.dexsearch.dex;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DexBase implements ClassLookup {

    private final Map<String, ClassDef> classes = new HashMap<>();

    public void load(File dexFile) throws IOException {
        DexFile dex = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault());
        for (ClassDef classDef : dex.getClasses()) {
            classes.put(classDef.getType(), classDef);
        }
    }

    @Override
    public ClassDef get(String name) {
        return classes.get(name);
    }

    @Override
    public Collection<ClassDef> getAll() {
        return classes.values();
    }

    public int size() {
        return classes.size();
    }
}
