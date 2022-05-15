package xyz.rodit.dexsearch.tree.nodes;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Member;
import org.jf.dexlib2.iface.Method;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.AccessUtils;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.BindingException;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.bindings.Reason;
import xyz.rodit.dexsearch.tree.bindings.options.AccessModifiers;
import xyz.rodit.dexsearch.tree.bindings.options.Option;
import xyz.rodit.dexsearch.tree.nodes.bytecode.BodyNode;
import xyz.rodit.dexsearch.tree.nodes.bytecode.events.*;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.StringMatcher;
import xyz.rodit.dexsearch.tree.properties.Annotation;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.types.ExtendsType;
import xyz.rodit.dexsearch.tree.properties.types.ImplementsType;
import xyz.rodit.dexsearch.tree.properties.types.JavaType;
import xyz.rodit.dexsearch.tree.properties.types.Type;
import xyz.rodit.dexsearch.utils.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ClassNode extends NodeBase<ClassBinding, ClassDef, Object> {

    private final int accessModifiers;
    private final Name name;
    private final String expected;
    private final List<Annotation> annotations;
    private final Type extendsType;
    private final ImplementsType interfaceTypes;
    private final List<FieldNode> fields = new ArrayList<>();
    private final List<FieldNode> notFields = new ArrayList<>();
    private final List<MethodNode> methods = new ArrayList<>();
    private final List<MethodNode> notMethods = new ArrayList<>();

    private final Map<String, FieldNode> lateFieldMap;
    private final Map<String, MethodNode> lateMethodMap;

    public ClassNode(EnumSet<Attribute> attributes, int accessModifiers, Name name, String expected, Collection<Annotation> annotations, Type extendsType, Collection<Type> interfaceTypes, Collection<FieldNode> fields, Collection<MethodNode> methods) {
        super(attributes);
        this.accessModifiers = accessModifiers;
        this.name = name;
        this.expected = expected;
        this.annotations = new ArrayList<>(annotations);
        this.extendsType = extendsType != null ? new ExtendsType(extendsType) : null;
        this.interfaceTypes = new ImplementsType(interfaceTypes);
        CollectionUtils.separate(fields, this.notFields, this.fields, f -> f.hasAttribute(Attribute.NOT));
        CollectionUtils.separate(methods, this.notMethods, this.methods, m -> m.hasAttribute(Attribute.NOT));
        lateFieldMap = this.fields.stream().filter(f -> f.hasAttribute(Attribute.LATE)).collect(Collectors.toMap(MemberNode::getName, f -> f));
        lateMethodMap = this.methods.stream().filter(m -> m.hasAttribute(Attribute.LATE)).collect(Collectors.toMap(MemberNode::getName, m -> m));

        processAttributes();
    }

    public String getName() {
        return name.getName();
    }

    public String getExpected() {
        return expected;
    }

    public FieldNode getLateField(String name) {
        return lateFieldMap.get(name);
    }

    public MethodNode getLateMethod(String name) {
        return lateMethodMap.get(name);
    }

    @Override
    public boolean tryBind(Resolver resolver, ClassBinding binding, ClassDef def, Object state) throws BindingException {
        if (resolver.getOptions().get(Option.ACCESS_MODIFIERS) == AccessModifiers.STRICT
                && !AccessUtils.hasModifiers(def.getAccessFlags(), accessModifiers)) {
            binding.fail(Reason.ACCESS_MODIFIERS, accessModifiers);
        } else {
            binding.succeed(Reason.ACCESS_MODIFIERS, accessModifiers);
        }

        binding.test(Reason.NAME, name, n -> n.matches(def.getType()));

        binding.test(Reason.ANNOTATION, annotations, CollectionUtils.matchesUnstrict(annotations, def.getAnnotations(), (required, annotation) -> required.matches(resolver, annotation)));

        // TODO: Consider moving potentially expensive inheritance checks to the end (long inheritance chains are expensive to evaluate).
        if (extendsType != null) {
            binding.test(Reason.INHERITANCE, extendsType, extendsType.matches(resolver, binding, def.getType()));
        }

        binding.test(Reason.INHERITANCE, interfaceTypes, interfaceTypes.matches(resolver, binding, def.getType()));

        List<Field> availableFields = CollectionUtils.toList(def.getFields());
        List<Method> availableMethods = CollectionUtils.toList(def.getMethods());

        if (hasAttribute(Attribute.EXACT) && (availableFields.size() != fields.size() || availableMethods.size() != methods.size())) {
            binding.fail(Reason.EXACT, null);
        }

        bindMembers(resolver, binding, availableFields, notFields, Reason.FIELD, true);
        bindMembers(resolver, binding, availableFields, fields, Reason.FIELD, false);
        bindMembers(resolver, binding, availableMethods, notMethods, Reason.METHOD, true);
        bindMembers(resolver, binding, availableMethods, methods, Reason.METHOD, false);

        return binding.getFailReasons().isEmpty();
    }

    private <T extends Member> void bindMembers(Resolver resolver, ClassBinding binding, List<T> available, Iterable<? extends MemberNode<T>> members, Reason reason, boolean not) throws BindingException {
        for (MemberNode<T> member : members) {
            // Late members can be set by binding events.
            if (member.hasAttribute(Attribute.LATE)) {
                continue;
            }

            int index = CollectionUtils.findIndex(available, m -> member.tryBind(resolver, binding, m, null));
            if (not != (index == -1)) {
                binding.fail(reason, member);
            } else if (!not) {
                binding.bindMember(member, available.get(index));
                binding.succeed(reason, member);
                available.remove(index);
            }
        }
    }

    private void processAttributes() {
        if (hasAttribute(Attribute.OBFUSCATED)) {
            if (AccessUtils.hasModifiers(accessModifiers, AccessFlags.ENUM.getValue())) {
                methods.add(new MethodNode(
                        EnumSet.of(Attribute.DISCARD),
                        AccessFlags.STATIC.getValue(),
                        new JavaType("void"),
                        new Name("<clinit>", true),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        fields.stream()
                                .filter(f ->
                                        AccessUtils.hasModifiers(f.getAccessModifiers(), AccessFlags.STATIC.getValue())
                                                && f.hasAttribute(Attribute.LATE))
                                .map(f -> new BodyNode(
                                        EnumSet.of(Attribute.STRICT),
                                        new StringMatcher(f.getName(), false, false),
                                        Collections.singleton(new BindEvent(Operation.BIND, EventTarget.FIELD, f.getName(), Modifiers.get("next"), EventSource.REFERENCE))
                                )).toList()));
            }
        }
    }
}
