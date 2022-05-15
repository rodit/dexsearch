package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public class ConstantMatcher extends SingleInstructionMatcher {

    private final long literal;

    public ConstantMatcher(long literal) {
        this.literal = literal;
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction) {
        return instruction instanceof WideLiteralInstruction literalInstr
                && literalInstr.getWideLiteral() == literal;
    }
}