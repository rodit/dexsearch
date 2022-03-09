package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ArrayType implements Type {

    private final Type baseType;

    public ArrayType(Type baseType) {
        this.baseType = baseType;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, String typeName) {
        return typeName.startsWith("[") && baseType.matches(resolver, binding, typeName.substring(1));
    }
}
