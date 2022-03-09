package xyz.rodit.dexsearch.tree.properties.types;

import xyz.rodit.dexsearch.resolver.Resolver;

public class VarargsType extends BasicType {

    @Override
    public boolean matches(Resolver resolver, String typeName) {
        return true;
    }
}
