package xyz.rodit.dexsearch.tree.properties.names;

import org.jf.dexlib2.iface.Member;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.Reference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ReferenceName implements BytecodeMemberName {

    private final String name;

    public ReferenceName(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding owner, String name, Class<? extends Reference> type) {
        if (owner != null) {
            Member member = type == FieldReference.class ? owner.getField(this.name) : owner.getMethod(this.name);
            return member != null && member.getName().equals(name);
        }

        return false;
    }
}
