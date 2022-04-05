package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.StringReference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.InstructionMatcher;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.types.Type;

import java.util.List;

public class SetFieldToString implements InstructionMatcher {

    private final SetFieldBase fieldSet;
    private final String string;

    public SetFieldToString(Type type, BytecodeMemberName name, String string) {
        this.fieldSet = new SetFieldBase(type, name);
        this.string = string;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, List<Instruction> instructions) {
        Instruction valInstr = instructions.get(0);
        Instruction setInstr = instructions.get(1);

        if (!(valInstr instanceof Instruction21c i21c)
                || !(i21c.getReference() instanceof StringReference str)) {
            return false;
        }

        return string.equals(str.getString())
                && fieldSet.matches(resolver, binding, method, setInstr, i21c.getRegisterA());
    }
}
