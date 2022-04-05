package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

import java.util.List;

public abstract class SingleInstructionMatcher implements InstructionMatcher {

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, List<Instruction> instructions) {
        return matches(resolver, binding, method, instructions.get(0));
    }

    public abstract boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction);
}
