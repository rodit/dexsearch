package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.FieldReferenceMatcher;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

public class SetFieldBase {

    private final FieldReferenceMatcher fieldMatcher;

    public SetFieldBase(Type type, BytecodeMemberName name) {
        this.fieldMatcher = new FieldReferenceMatcher(type, name);
    }

    public boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction, int register) {
        return instruction instanceof Instruction22c i22c
                && i22c.getRegisterA() == register
                && fieldMatcher.matches(resolver, binding, method, instruction);
    }
}
