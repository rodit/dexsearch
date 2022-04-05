package xyz.rodit.dexsearch.tree.bindings;

public enum Reason {
    ACCESS_MODIFIERS(1),
    NAME(1),
    ANNOTATION(3),
    FIELD(2),
    METHOD(2),
    EXACT(1),
    INHERITANCE(3);

    private final int score;

    Reason(int score) {
        this.score = score;
    }

    public int score() {
        return this.score;
    }
}
