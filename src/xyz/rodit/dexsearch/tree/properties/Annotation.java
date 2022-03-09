package xyz.rodit.dexsearch.tree.properties;

import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.*;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.properties.types.Type;
import xyz.rodit.dexsearch.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Annotation {

    private final Type type;
    private final List<Object> arguments;

    public Annotation(Type type, Collection<Object> arguments) {
        this.type = type;
        this.arguments = new ArrayList<>(arguments);
    }

    public boolean matches(Resolver resolver, org.jf.dexlib2.iface.Annotation annotation) {
        return type.matches(resolver, null, annotation.getType()) && CollectionUtils.matches(arguments, annotation.getElements(), Annotation::argumentMatches);
    }

    private static boolean argumentMatches(Object arg, AnnotationElement element) {
        EncodedValue val = element.getValue();
        if (val instanceof StringEncodedValue s) {
            return arg.equals(s.getValue());
        } else if (val instanceof IntEncodedValue i) {
            return arg.equals(i.getValue());
        } else if (val instanceof LongEncodedValue l) {
            return arg.equals(l.getValue());
        } else if (val instanceof FloatEncodedValue f) {
            return arg.equals(f.getValue());
        } else if (val instanceof DoubleEncodedValue d) {
            return arg.equals(d.getValue());
        } else if (val instanceof BooleanEncodedValue b) {
            return arg.equals(b.getValue());
        } else if (val instanceof CharEncodedValue c) {
            return arg.equals(c.getValue());
        }

        return false;
    }
}
