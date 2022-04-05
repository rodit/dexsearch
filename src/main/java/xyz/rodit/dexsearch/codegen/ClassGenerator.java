package xyz.rodit.dexsearch.codegen;

import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;

public interface ClassGenerator {

    String generate(Resolver resolver, ClassBinding binding);
}
