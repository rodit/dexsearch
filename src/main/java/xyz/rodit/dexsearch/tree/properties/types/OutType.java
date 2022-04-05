package xyz.rodit.dexsearch.tree.properties.types;

import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;
import xyz.rodit.dexsearch.tree.properties.Name;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class OutType extends BasicType {

    private final String name;

    public OutType(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Resolver resolver, String type) {
        ClassDef def = resolver.getClasses().get(type);
        if (def != null) {
            ClassNode classNode = Optional.ofNullable(resolver.getSchemaClass(name))
                    .orElse(new ClassNode(EnumSet.of(Attribute.LATE),
                            0,
                            new Name(name, false),
                            null,
                            Collections.emptySet(),
                            null,
                            Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
            return resolver.resolve(classNode, List.of(def));
        }

        return false;
    }
}
