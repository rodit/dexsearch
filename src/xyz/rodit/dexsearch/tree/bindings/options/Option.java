package xyz.rodit.dexsearch.tree.bindings.options;


public enum Option {
    ACCESS_MODIFIERS(new EnumParser<>(AccessModifiers.class));

    private final ValueParser<?> parser;

    Option(ValueParser<?> parser) {
        this.parser = parser;
    }

    public ValueParser<?> parser() {
        return parser;
    }
}