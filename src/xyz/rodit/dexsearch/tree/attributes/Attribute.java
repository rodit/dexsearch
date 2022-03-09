package xyz.rodit.dexsearch.tree.attributes;

import java.util.EnumSet;

public enum Attribute {
    NONE(EnumSet.noneOf(Target.class)),
    LATE(EnumSet.of(Target.CLASS, Target.FIELD, Target.METHOD)),
    EXACT(EnumSet.of(Target.CLASS)),
    CERTAIN(EnumSet.of(Target.CLASS)),
    FUZZY(EnumSet.of(Target.CLASS)),
    VERY_FUZZY(EnumSet.of(Target.CLASS)),
    EXPECTED(EnumSet.of(Target.CLASS)),
    DISCARD(EnumSet.of(Target.CLASS, Target.FIELD, Target.METHOD)),
    OPTIONAL(EnumSet.of(Target.FIELD, Target.METHOD)),
    NOT(EnumSet.of(Target.FIELD, Target.METHOD)),
    CONSERVE(EnumSet.of(Target.INSTRUCTION)),
    STRICT(EnumSet.of(Target.INSTRUCTION)),
    // TODO: add support for markers in the future
    MARKER(EnumSet.noneOf(Target.class));

    private final EnumSet<Target> targets;

    Attribute(EnumSet<Target> targets) {
        this.targets = targets;
    }

    public boolean isValidTarget(Target target) {
        return targets.contains(target);
    }
}
