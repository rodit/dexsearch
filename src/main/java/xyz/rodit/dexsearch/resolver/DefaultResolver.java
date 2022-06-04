package xyz.rodit.dexsearch.resolver;

import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.dex.ClassLookup;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.BindingException;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.bindings.options.Options;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;
import xyz.rodit.dexsearch.utils.TypeUtils;

import java.util.*;

public class DefaultResolver implements Resolver {

    private final Options options;
    private final ClassLookup classes;
    private final Map<String, ClassBinding> bindings = new HashMap<>();
    private final Map<String, ClassBinding> dexBindings = new HashMap<>();
    private final Map<String, ClassNode> schemaClasses = new HashMap<>();
    private final List<String> schemasOrdered = new ArrayList<>();

    public DefaultResolver(Options options, ClassLookup classes, Collection<ClassNode> schemaClasses) {
        this.options = options;
        this.classes = classes;
        for (ClassNode node : schemaClasses) {
            this.schemaClasses.put(node.getName(), node);
            schemasOrdered.add(node.getName());
        }
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public ClassLookup getClasses() {
        return classes;
    }

    @Override
    public ClassBinding getBinding(String name) {
        return bindings.get(name);
    }

    @Override
    public ClassBinding getBindingFromDex(String dexName) {
        return dexBindings.get(dexName);
    }

    @Override
    public ClassNode getSchemaClass(String name) {
        return schemaClasses.get(name);
    }

    @Override
    public boolean resolve(ClassNode node, Iterable<ClassDef> candidates) {
        if (node.hasAttribute(Attribute.FUZZY) || node.hasAttribute(Attribute.VERY_FUZZY)) {
            return resolveFuzzy(node, node.hasAttribute(Attribute.VERY_FUZZY), candidates);
        }

        return resolveNonFuzzy(node, candidates);
    }

    private boolean resolveNonFuzzy(ClassNode node, Iterable<ClassDef> candidates) {
        ClassBinding binding = new ClassBinding(node);
        for (ClassDef candidate : candidates) {
            try {
                binding.attach(candidate);
                if (node.tryBind(this, binding, candidate, null)) {
                    putBinding(node.getName(), binding);
                    return true;
                }
            } catch (BindingException e) {
                // class binding unsuccessful.
            }

            binding.reset();
        }

        return false;
    }

    private boolean resolveFuzzy(ClassNode node, boolean very, Iterable<ClassDef> candidates) {
        ClassBinding best = bindings.get(node.getName());
        for (ClassDef candidate : candidates) {
            ClassBinding binding = new ClassBinding(node);
            try {
                binding.attach(candidate);
                if (node.tryBind(this, binding, candidate, null)) {
                    bindings.put(node.getName(), binding);
                    return true;
                }
            } catch (BindingException e) {
                // class binding unsuccessful.
            }

            if (best == null || binding.getScore() > best.getScore()) {
                best = binding;
            }
        }

        putBinding(node.getName(), best);
        return very;
    }

    private void putBinding(String name, ClassBinding binding) {
        bindings.put(name, binding);
    }

    @Override
    public Map<String, ClassBinding> resolveAll() {
        Set<ClassDef> available = new HashSet<>(classes.getAll());
        schemasOrdered.stream().map(schemaClasses::get)
                .filter(c -> !c.hasAttribute(Attribute.LATE))
                .sorted(Comparator.comparing(c -> c.hasAttribute(Attribute.CERTAIN) ? -1 : 1))
                .forEach(node -> {
                    if (node.getExpected() != null) {
                        ClassDef candidate = classes.get(TypeUtils.toDexType(node.getExpected()));
                        if (candidate != null) {
                            if (resolve(node, List.of(candidate))) {
                                System.out.println("Bound " + node.getName() + " to expected class " + node.getExpected() + ".");
                                if (!node.hasAttribute(Attribute.CONSERVE)) {
                                    available.remove(candidate);
                                }
                                return;
                            } else {
                                System.err.println("Failed to bind " + node.getName() + " to expected class.");
                                if (node.hasAttribute(Attribute.EXPECTED)) {
                                    return;
                                }
                            }
                        } else {
                            System.err.println("Expected class for " + node.getName() + " was not found (" + node.getExpected() + ").");
                        }
                    }
                    if (resolve(node, available)) {
                        ClassBinding binding = bindings.get(node.getName());
                        if (!node.hasAttribute(Attribute.CONSERVE)) {
                            available.remove(binding.get());
                        }
                        System.out.println("Bound " + node.getName() + " to " + TypeUtils.toJavaType(binding.get().getType()) + " with score " + binding.getScore() + ".");
                    } else {
                        System.err.println("Failed to bind " + node.getName() + ".");
                    }
                });

        for (String name : bindings.keySet()) {
            ClassBinding binding = bindings.get(name);
            dexBindings.put(bindings.get(name).get().getType(), binding);
        }

        return bindings;
    }
}
