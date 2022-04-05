package xyz.rodit.dexsearch.tree.nodes;

import org.jf.dexlib2.iface.Field;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.properties.Annotation;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.types.Type;

import java.util.Collection;
import java.util.EnumSet;

public class FieldNode extends MemberNode<Field> {

    public FieldNode(EnumSet<Attribute> attributes, int accessModifiers, Type type, Name name, Collection<Annotation> annotations) {
        super(attributes, accessModifiers, type, name, annotations);
    }

    @Override
    protected String getMemberType(Field member) {
        return member.getType();
    }
}
