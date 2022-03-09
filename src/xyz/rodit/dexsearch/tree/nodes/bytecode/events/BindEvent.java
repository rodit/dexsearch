package xyz.rodit.dexsearch.tree.nodes.bytecode.events;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.ClassUtils;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;
import xyz.rodit.dexsearch.tree.nodes.FieldNode;
import xyz.rodit.dexsearch.tree.nodes.MethodNode;
import xyz.rodit.dexsearch.tree.nodes.bytecode.FieldReferenceHolder;
import xyz.rodit.dexsearch.tree.nodes.bytecode.MethodReferenceHolder;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.types.JavaType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class BindEvent {

    private final Operation operation;
    private final EventTarget target;
    private final String targetName;
    private final Modifier modifier;
    private final EventSource source;

    public BindEvent(Operation operation, EventTarget target, String targetName, Modifier modifier, EventSource source) {
        this.operation = operation;
        this.target = target;
        this.targetName = targetName;
        this.modifier = modifier;
        this.source = source;
    }

    public boolean onBind(Resolver resolver, ClassBinding binding, List<Instruction> instructions, int foundIndex) {
        Instruction targetInstr = modifier.findInstruction(instructions, foundIndex, target, source);
        if (targetInstr == null) {
            return false;
        }

        // TODO: Re-implement binding events to more easily support different actions.
        // TODO: Clean this code and reduce duplication.
        // TODO: Implement different sources.
        // TODO: Support annotations, access flags etc. in field and method reference holders
        //       without searching class for field and having ambiguous method references.
        switch (operation) {
            case BIND:
                switch (target) {
                    case FIELD -> {
                        FieldReference fieldRef = (FieldReference) ((ReferenceInstruction) targetInstr).getReference();
                        ClassDef fieldClass = resolver.getClasses().get(fieldRef.getDefiningClass());
                        Field field = ClassUtils.findField(fieldClass, fieldRef.getName());
                        FieldNode fieldNode = Optional.ofNullable(binding.getNode().getLateField(targetName))
                                .orElse(new FieldNode(EnumSet.of(Attribute.LATE),
                                        0,
                                        new JavaType(field.getType()),
                                        new Name(targetName, false),
                                        Collections.emptySet()));
                        binding.bindMember(fieldNode, field);
                        return fieldNode.tryBind(resolver, binding, field, null);
                    }
                    case METHOD -> {
                        MethodReference methodRef = (MethodReference) ((ReferenceInstruction) targetInstr).getReference();
                        ClassDef methodClass = resolver.getClasses().get(methodRef.getDefiningClass());
                        Method method = ClassUtils.findMethod(methodClass, methodRef.getName());
                        MethodNode methodNode = Optional.ofNullable(binding.getNode().getLateMethod(targetName))
                                .orElse(new MethodNode(EnumSet.noneOf(Attribute.class),
                                        0,
                                        new JavaType(method.getReturnType()),
                                        new Name(targetName, false),
                                        Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
                        binding.bindMember(methodNode, method);
                        return methodNode.tryBind(resolver, binding, method, null);
                    }
                    case TYPE -> {
                        TypeReference typeRef = (TypeReference) ((ReferenceInstruction) targetInstr).getReference();
                        ClassDef def = resolver.getClasses().get(typeRef.getType());
                        if (def != null) {
                            ClassNode classNode = Optional.ofNullable(resolver.getSchemaClass(targetName))
                                    .orElse(new ClassNode(EnumSet.of(Attribute.LATE),
                                            0,
                                            new Name(targetName, false),
                                            null,
                                            Collections.emptySet(),
                                            null,
                                            Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
                            return resolver.resolve(classNode, List.of(def));
                        }
                        return false;
                    }
                }
                break;
        }

        return false;
    }
}
