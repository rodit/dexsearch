package xyz.rodit.dexsearch.tree.properties.types;

import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ImplementsType implements Type {

    private final List<Type> interfaces;

    public ImplementsType(Collection<Type> interfaces) {
        this.interfaces = new ArrayList<>(interfaces);
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, String type) {
        if (interfaces.isEmpty()) {
            return true;
        }

        ClassDef def = resolver.getClasses().get(type);
        if (def != null) {
            List<String> typeInterfaces = new ArrayList<>(def.getInterfaces());
            for (Type requiredInterface : interfaces) {
                int foundIndex = -1;
                for (int i = 0; i < typeInterfaces.size(); i++) {
                    if (requiredInterface.matches(resolver, binding, typeInterfaces.get(i))) {
                        foundIndex = i;
                        break;
                    }
                }

                if (foundIndex == -1) {
                    return false;
                }

                typeInterfaces.remove(foundIndex);
            }

            return true;
        }

        return false;
    }
}
