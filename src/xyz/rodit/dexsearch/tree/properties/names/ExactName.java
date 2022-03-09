package xyz.rodit.dexsearch.tree.properties.names;

import org.jf.dexlib2.iface.reference.Reference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ExactName implements BytecodeMemberName {

    private final String name;

    public ExactName(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding owner, String name, Class<? extends Reference> type) {
        return this.name.equals(name);
    }
}
