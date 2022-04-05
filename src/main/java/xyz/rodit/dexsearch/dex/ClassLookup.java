package xyz.rodit.dexsearch.dex;

import org.jf.dexlib2.iface.ClassDef;

import java.util.Collection;

public interface ClassLookup {

    ClassDef get(String name);
    Collection<ClassDef> getAll();
}
