package xyz.rodit.dexsearch.resolver;

import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.dex.ClassLookup;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.bindings.options.Options;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;

import java.util.Map;

public interface Resolver {

    Options getOptions();
    ClassLookup getClasses();
    ClassBinding getBinding(String name);
    ClassBinding getBindingFromDex(String dexName);
    ClassNode getSchemaClass(String name);
    boolean resolve(ClassNode node, Iterable<ClassDef> candidates);
    Map<String, ClassBinding> resolveAll();
}
