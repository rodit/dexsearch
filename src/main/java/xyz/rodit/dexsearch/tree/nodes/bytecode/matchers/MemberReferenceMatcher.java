package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.Reference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.ReferencedType;
import xyz.rodit.dexsearch.tree.properties.types.ThisType;
import xyz.rodit.dexsearch.tree.properties.types.Type;

public abstract class MemberReferenceMatcher<T extends Reference> extends SingleInstructionMatcher {

    private final Type type;
    private final BytecodeMemberName name;
    private final Class<T> clazz;

    public MemberReferenceMatcher(Type type, BytecodeMemberName name, Class<T> clazz) {
        this.type = type;
        this.name = name;
        this.clazz = clazz;
    }

    protected abstract T getReference(ReferenceInstruction instruction);

    protected abstract String getDefiningClass(T reference);

    protected abstract String getName(T reference);

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction) {
        if (!(instruction instanceof ReferenceInstruction refInstr)) {
            return false;
        }

        T reference = getReference(refInstr);
        if (reference == null) {
            return false;
        }

        String definingClass = getDefiningClass(reference);
        if (!type.matches(resolver, binding, definingClass)) {
            return false;
        }

        ClassBinding owner = null;
        if (type instanceof ThisType) {
            owner = binding;
        } else if (type instanceof ReferencedType refType) {
            owner = resolver.getBinding(refType.getName());
        }

        String name = getName(reference);
        return this.name.matches(resolver, owner, name, clazz);
    }
}
