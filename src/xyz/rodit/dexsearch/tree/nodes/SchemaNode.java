package xyz.rodit.dexsearch.tree.nodes;

import xyz.rodit.dexsearch.tree.bindings.options.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SchemaNode {

    private final Options options;
    private final List<ClassNode> classes;

    public SchemaNode(Options options, Collection<ClassNode> classes) {
        this.options = options;
        this.classes = new ArrayList<>(classes);
    }

    public Options getOptions() {
        return options;
    }

    public List<ClassNode> getClasses() {
        return classes;
    }
}
