package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21ih;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.InstructionMatcher;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

import java.util.List;

public class SetFieldToInt16 implements InstructionMatcher {

    private final SetFieldBase fieldSet;
    private final int value;

    public SetFieldToInt16(Type type, BytecodeMemberName name, int value) {
        this.fieldSet = new SetFieldBase(type, name);
        this.value = value;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, List<Instruction> instructions) {
        Instruction valInstr = instructions.get(0);
        Instruction setInstr = instructions.get(1);

        if (!(valInstr instanceof Instruction21ih i21ih
                && valInstr.getOpcode() == Opcode.CONST_16)) {
            return false;
        }

        return value == i21ih.getHatLiteral()
                && fieldSet.matches(resolver, binding, method, setInstr, i21ih.getRegisterA());
    }
}
