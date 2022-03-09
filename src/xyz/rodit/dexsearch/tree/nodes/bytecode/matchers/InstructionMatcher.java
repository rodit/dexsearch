package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

import java.util.List;

public interface InstructionMatcher {

    int getCount();

    boolean matches(Resolver resolver, ClassBinding binding, Method method, List<Instruction> instructions);
}
