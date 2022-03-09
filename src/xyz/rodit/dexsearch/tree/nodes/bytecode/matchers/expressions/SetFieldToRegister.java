package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.SingleInstructionMatcher;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

public class SetFieldToRegister extends SingleInstructionMatcher {

    private final SetFieldBase fieldSet;
    private final int registerNum;
    private final boolean paramRegister;

    public SetFieldToRegister(Type type, BytecodeMemberName name, int registerNum, boolean paramRegister) {
        this.fieldSet = new SetFieldBase(type, name);
        this.registerNum = registerNum;
        this.paramRegister = paramRegister;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction) {
        int register = registerNum;
        if (paramRegister) {
            MethodImplementation impl = method.getImplementation();
            if (impl != null) {
                register += impl.getRegisterCount() - method.getParameterTypes().size();
            }
        }

        return fieldSet.matches(resolver, binding, method, instruction, register);
    }
}
