package xyz.rodit.dexsearch.tree.bindings;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Member;
import org.jf.dexlib2.iface.Method;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;
import xyz.rodit.dexsearch.tree.nodes.FieldNode;
import xyz.rodit.dexsearch.tree.nodes.MemberNode;
import xyz.rodit.dexsearch.tree.nodes.MethodNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ClassBinding extends BindingBase<ClassDef> {

    private final ClassNode node;
    private final Map<FieldNode, Field> fields = new HashMap<>();
    private final Map<MethodNode, Method> methods = new HashMap<>();
    private final Map<String, Field> namedFields = new HashMap<>();
    private final Map<String, Method> namedMethods = new HashMap<>();

    public ClassBinding(ClassNode node) {
        this.node = node;
    }

    @Override
    public void fail(Reason reason, Object subject) throws BindingException {
        super.fail(reason, subject);
        if (!node.hasAttribute(Attribute.VERY_FUZZY)) {
            throw new BindingException("Binding failed: " + reason + ", subject=" + subject);
        }
    }

    public ClassNode getNode() {
        return node;
    }

    public <T extends Member> void bindMember(MemberNode<T> node, T member) {
        if (node instanceof FieldNode fieldNode && member instanceof Field field) {
            fields.put(fieldNode, field);
            namedFields.put(fieldNode.getName(), field);
        } else if (node instanceof MethodNode methodNode && member instanceof Method method) {
            methods.put(methodNode, method);
            namedMethods.put(methodNode.getName(), method);
        }
    }

    public Field getField(FieldNode node) {
        return fields.get(node);
    }

    public Method getMethod(MethodNode node) {
        return methods.get(node);
    }

    public Field getField(String name) {
        return namedFields.get(name);
    }

    public Method getMethod(String name) {
        return namedMethods.get(name);
    }

    public Collection<FieldNode> getBoundFields() {
        return fields.keySet();
    }

    public Collection<MethodNode> getBoundMethods() {
        return methods.keySet();
    }

    public void reset() {
        super.reset();
        fields.clear();
        methods.clear();
        namedFields.clear();
        namedMethods.clear();
    }
}
