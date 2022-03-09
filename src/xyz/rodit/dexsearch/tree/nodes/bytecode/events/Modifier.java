package xyz.rodit.dexsearch.tree.nodes.bytecode.events;

import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.List;

public interface Modifier {

    Instruction findInstruction(List<Instruction> instructions, int current, EventTarget target, EventSource source);
}
