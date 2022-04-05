package xyz.rodit.dexsearch.tree.nodes;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.Binding;
import xyz.rodit.dexsearch.tree.bindings.BindingException;
import xyz.rodit.dexsearch.tree.bindings.options.Options;

import java.util.EnumSet;

public abstract class NodeBase<TBinding extends Binding<?>, TBind, TState> {

    private final EnumSet<Attribute> attributes;

    protected NodeBase(EnumSet<Attribute> attributes) {
        this.attributes = attributes;
    }

    public boolean hasAttribute(Attribute attribute) {
        return attributes.contains(attribute);
    }

    public abstract boolean tryBind(Resolver resolver, TBinding binding, TBind bind, TState state) throws BindingException;
}
