package xyz.rodit.dexsearch.codegen;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.utils.TypeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolvedType {

    private static final Set<String> PRIMITIVES = new HashSet<>(List.of("void", "boolean", "byte", "short", "char", "int", "long", "float", "double"));

    private final String name;
    private final boolean resolved;

    public ResolvedType(String name, boolean resolved) {
        this.name = name;
        this.resolved = resolved;
    }

    public String wrap(String expression) {
        if (resolved) {
            return String.format("%s.wrap(%s)", name, expression);
        }

        if (!name.equals("void") && !name.equals("Object")) {
            return String.format("(%s) %s", name, expression);
        }

        return expression;
    }

    public boolean returns() {
        return !name.equals("void");
    }

    @Override
    public String toString() {
        return name;
    }

    public static ResolvedType get(ClassChecker checker, Resolver resolver, String name) {
        ClassBinding binding = resolver.getBindingFromDex(name);
        if (binding != null) {
            return new ResolvedType(binding.getNode().getName(), true);
        }

        String javaName = TypeUtils.toJavaType(name);
        if (PRIMITIVES.contains(javaName) || checker.get(javaName) != null) {
            return new ResolvedType(javaName, false);
        }

        return new ResolvedType("Object", false);
    }
}
