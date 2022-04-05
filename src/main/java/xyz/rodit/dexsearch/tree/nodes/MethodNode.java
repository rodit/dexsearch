package xyz.rodit.dexsearch.tree.nodes;

import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.bytecode.BodyNode;
import xyz.rodit.dexsearch.tree.properties.Annotation;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.types.Type;
import xyz.rodit.dexsearch.tree.properties.types.VarargsType;
import xyz.rodit.dexsearch.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class MethodNode extends MemberNode<Method> {

    private final List<Type> parameterTypes;
    private final List<BodyNode> bodyNodes;

    public MethodNode(EnumSet<Attribute> attributes, int accessModifiers, Type type, Name name, Collection<Annotation> annotations, Collection<Type> parameterTypes, Collection<BodyNode> bodyNodes) {
        super(attributes, accessModifiers, type, name, annotations);
        this.parameterTypes = new ArrayList<>(parameterTypes);
        this.bodyNodes = new ArrayList<>(bodyNodes);
    }

    @Override
    protected String getMemberType(Method method) {
        return method.getReturnType();
    }

    @Override
    public boolean tryBind(Resolver resolver, ClassBinding binding, Method method, Object state) {
        if (!super.tryBind(resolver, binding, method, state)
                || !CollectionUtils.matchesUntil(parameterTypes, method.getParameterTypes(), (required, type) -> required.matches(resolver, binding, type.toString()), required -> required instanceof VarargsType, true)) {
            return false;
        }

        MethodImplementation impl = method.getImplementation();
        if (impl == null) {
            return bodyNodes.isEmpty();
        }

        List<Instruction> body = CollectionUtils.toList(method.getImplementation().getInstructions());
        for (BodyNode node : bodyNodes) {
            if (!node.tryBind(resolver, binding, method, body)) {
                return false;
            }
        }

        return true;
    }
}
