package xyz.rodit.dexsearch.tree.nodes.bytecode.events;

import org.jf.dexlib2.iface.instruction.Instruction;

public class Modifiers {

    public static Modifier get(String name) {
        return switch (name) {
            case "previous" -> (instructions, current, target, source) -> {
                for (int i = current - 1; i > -1; i--) {
                    Instruction instr = instructions.get(i);
                    if (target.matches(instr) && source.matches(instr)) {
                        return instr;
                    }
                }

                return null;
            };
            case "current" -> (instructions, current, target, source) -> {
                Instruction instr = instructions.get(current);
                return target.matches(instr) && source.matches(instr) ? instr : null;
            };
            case "next" -> (instructions, current, target, source) -> {
                for (int i = current + 1; i < instructions.size(); i++) {
                    Instruction instr = instructions.get(i);
                    if (target.matches(instr) && source.matches(instr)) {
                        return instr;
                    }
                }

                return null;
            };
            default -> null;
        };
    }
}