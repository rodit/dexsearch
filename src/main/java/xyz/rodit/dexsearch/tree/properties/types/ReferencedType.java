package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ReferencedType extends BasicType {

    private final String name;

    public ReferencedType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean matches(Resolver resolver, String type) {
        ClassBinding binding = resolver.getBinding(name);
        return binding != null && binding.get().getType().equals(type);
    }
}
