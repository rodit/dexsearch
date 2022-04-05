package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ThisType implements Type {

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, String typeName) {
        return binding.get().getType().equals(typeName);
    }
}
