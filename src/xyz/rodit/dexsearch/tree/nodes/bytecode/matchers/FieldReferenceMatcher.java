package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

public class FieldReferenceMatcher extends MemberReferenceMatcher<FieldReference> {

    public FieldReferenceMatcher(Type type, BytecodeMemberName name) {
        super(type, name, FieldReference.class);
    }

    @Override
    protected FieldReference getReference(ReferenceInstruction instruction) {
        return instruction.getReference() instanceof FieldReference fieldRef ? fieldRef : null;
    }

    @Override
    protected String getDefiningClass(FieldReference reference) {
        return reference.getDefiningClass();
    }

    @Override
    protected String getName(FieldReference reference) {
        return reference.getName();
    }
}
