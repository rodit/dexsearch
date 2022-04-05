package xyz.rodit.dexsearch.tree.nodes.bytecode.matchers;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.StringReference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringMatcher extends SingleInstructionMatcher {

    private final Predicate<String> stringMatcher;

    public StringMatcher(String string, boolean contains, boolean regex) {
        if (contains) {
            stringMatcher = s -> s.contains(string);
        } else if (regex) {
            stringMatcher = Pattern.compile(string).asPredicate();
        } else {
            stringMatcher = s -> s.equals(string);
        }
    }

    @Override
    public boolean matches(Resolver resolver, ClassBinding binding, Method method, Instruction instruction) {
        return instruction instanceof ReferenceInstruction ref
                && ref.getReference() instanceof StringReference stringRef
                && stringMatcher.test(stringRef.getString());
    }
}
