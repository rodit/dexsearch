package xyz.rodit.dexsearch.tree.nodes.bytecode.events;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.function.Predicate;

public enum EventSource {
    REFERENCE(i -> i instanceof ReferenceInstruction);

    private final Predicate<Instruction> matches;

    EventSource(Predicate<Instruction> matches) {
        this.matches = matches;
    }

    public boolean matches(Instruction instruction) {
        return matches.test(instruction);
    }
}
