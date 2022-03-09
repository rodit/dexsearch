package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public abstract class BasicType implements Type {

    public abstract boolean matches(Resolver resolver, String type);

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, String type) {
        return matches(resolver, type);
    }
}
