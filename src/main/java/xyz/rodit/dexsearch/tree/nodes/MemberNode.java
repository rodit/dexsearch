package xyz.rodit.dexsearch.tree.nodes;

import org.jf.dexlib2.iface.Member;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.AccessUtils;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.bindings.options.AccessModifiers;
import xyz.rodit.dexsearch.tree.bindings.options.Option;
import xyz.rodit.dexsearch.tree.properties.Annotation;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.types.Type;
import xyz.rodit.dexsearch.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public abstract class MemberNode<TMember extends Member> extends NodeBase<ClassBinding, TMember, Object> {

    private final int accessModifiers;
    private final Type type;
    private final Name name;
    private final List<Annotation> annotations;

    public MemberNode(EnumSet<Attribute> attributes, int accessModifiers, Type type, Name name, Collection<Annotation> annotations) {
        super(attributes);
        this.accessModifiers = accessModifiers;
        this.type = type;
        this.name = name;
        this.annotations = new ArrayList<>(annotations);
    }

    public int getAccessModifiers() {
        return accessModifiers;
    }

    public String getName() {
        return name.getName();
    }

    protected abstract String getMemberType(TMember member);

    @Override
    public boolean tryBind(Resolver resolver, ClassBinding binding, TMember member, Object state) {
        if (resolver.getOptions().get(Option.ACCESS_MODIFIERS) == AccessModifiers.STRICT
                && !AccessUtils.hasModifiers(member.getAccessFlags(), accessModifiers)) {
            return false;
        }

        if (!type.matches(resolver, binding, getMemberType(member))) {
            return false;
        }

        if (!name.matches(member.getName())) {
            return false;
        }

        if (!CollectionUtils.matchesUnstrict(annotations, member.getAnnotations(), (required, annotation) -> required.matches(resolver, annotation))) {
            return false;
        }

        return true;
    }
}
