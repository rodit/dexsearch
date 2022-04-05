package xyz.rodit.dexsearch.tree.bindings.options;

public interface ValueParser<T> {

    T parse(String value);
}
