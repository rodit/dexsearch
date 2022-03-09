package xyz.rodit.dexsearch.tree.properties.types;

import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ExtendsType implements Type {

    private final Type baseType;

    public ExtendsType(Type baseType) {
        this.baseType = baseType;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, String typeName) {
        ClassDef def = resolver.getClasses().get(typeName);
        if (def != null) {
            String superclass = def.getSuperclass();
            while (superclass != null) {
                if (baseType.matches(resolver, binding, superclass)) {
                    return true;
                }

                ClassDef superDef = resolver.getClasses().get(superclass);
                superclass = superDef == null ? null : superDef.getSuperclass();
            }
        }

        return false;
    }
}
