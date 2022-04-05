package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

public class MethodReferenceMatcher extends MemberReferenceMatcher<MethodReference> {

    public MethodReferenceMatcher(Type type, BytecodeMemberName name) {
        super(type, name, MethodReference.class);
    }

    @Override
    protected MethodReference getReference(ReferenceInstruction instruction) {
        return instruction.getReference() instanceof MethodReference methodRef ? methodRef : null;
    }

    @Override
    protected String getDefiningClass(MethodReference reference) {
        return reference.getDefiningClass();
    }

    @Override
    protected String getName(MethodReference reference) {
        return reference.getName();
    }
}
