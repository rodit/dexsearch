package xyz.rodit.dexsearch.tree.nodes.bytecode.events;

import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.function.Predicate;

public enum EventTarget {
    FIELD(i -> i instanceof ReferenceInstruction ref && ref.getReferenceType() == ReferenceType.FIELD),
    METHOD(i -> i instanceof ReferenceInstruction ref && ref.getReferenceType() == ReferenceType.METHOD),
    TYPE(i -> i instanceof ReferenceInstruction ref && ref.getReferenceType() == ReferenceType.TYPE);

    private final Predicate<Instruction> matches;

    EventTarget(Predicate<Instruction> matches) {
        this.matches = matches;
    }

    public boolean matches(Instruction instruction) {
        return matches.test(instruction);
    }
}
