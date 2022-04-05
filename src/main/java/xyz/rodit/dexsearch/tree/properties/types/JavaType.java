package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.utils.TypeUtils;

public class JavaType extends BasicType {

    private final String name;

    public JavaType(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Resolver resolver, String type) {
        return name.equals(TypeUtils.toJavaType(type));
    }
}
