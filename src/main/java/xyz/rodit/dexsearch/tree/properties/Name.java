package xyz.rodit.dexsearch.tree.properties;

public class Name {

    private final String name;
    private final boolean exact;

    public Name(String name, boolean exact) {
        this.name = name;
        this.exact = exact;
    }

    public String getName() {
        return name;
    }

    public boolean matches(String name) {
        return !exact || name.equals(this.name);
    }
}
