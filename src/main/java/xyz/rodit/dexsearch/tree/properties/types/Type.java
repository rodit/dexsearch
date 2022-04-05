package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public interface Type {

    boolean matches(Resolver resolver, ClassBinding binding, String typeName);
}
