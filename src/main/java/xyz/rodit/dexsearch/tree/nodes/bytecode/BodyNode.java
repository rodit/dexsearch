package xyz.rodit.dexsearch.tree.nodes.bytecode;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.NodeBase;
import xyz.rodit.dexsearch.tree.nodes.bytecode.events.BindEvent;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.InstructionMatcher;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.SingleInstructionMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

public class BodyNode extends NodeBase<ClassBinding, Method, List<Instruction>> {

    private final InstructionMatcher matcher;
    private final List<BindEvent> events;

    public BodyNode(EnumSet<Attribute> attributes, SingleInstructionMatcher matcher, Collection<BindEvent> events) {
        super(attributes);
        this.matcher = matcher;
        this.events = new ArrayList<>(events);
    }

    @Override
    public boolean tryBind(Resolver resolver, ClassBinding binding, Method method, List<Instruction> instructions) {
        int count = matcher.getCount();
        int found = -1;
        for (int i = 0; i < instructions.size() - count; i++) {
            if (matcher.matches(resolver, binding, method, instructions.subList(i, i + count))) {
                found = i;
                break;
            }
        }

        if (found > -1) {
            for (BindEvent event : events) {
                if (!event.onBind(resolver, binding, instructions, found)
                        && hasAttribute(Attribute.STRICT)) {
                    return false;
                }
            }

            if (!hasAttribute(Attribute.CONSERVE)) {
                IntStream.range(found, found + count).forEach(instructions::remove);
            }

            return true;
        }

        return false;
    }
}
