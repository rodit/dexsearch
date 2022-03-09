package xyz.rodit.dexsearch.codegen;

import com.google.gson.Gson;
import xyz.rodit.dexsearch.client.ClassMapping;
import xyz.rodit.dexsearch.client.Mappings;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.utils.TypeUtils;

import javax.tools.*;
import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Packager {

    public static void createMappingsFile(Map<String, ClassBinding> bindings, int buildNum, File destination) throws IOException {
        Mappings.BindingEntry entry = new Mappings.BindingEntry();
        entry.build = buildNum;
        entry.mappings = bindings.keySet().stream().map(n -> generateMapping(bindings.get(n))).toList();
        try (FileWriter writer = new FileWriter(destination)) {
            new Gson().toJson(entry, writer);
        }
    }

    private static ClassMapping generateMapping(ClassBinding binding) {
        ClassMapping mapping = new ClassMapping(binding.getNode().getName(), TypeUtils.toJavaType(binding.get().getType()));
        binding.getBoundFields().stream()
                .filter(f -> !f.hasAttribute(Attribute.DISCARD))
                .forEach(f -> mapping.mapField(f.getName(), binding.getField(f).getName()));
        binding.getBoundMethods().stream()
                .filter(m -> !m.hasAttribute(Attribute.DISCARD))
                .forEach(m -> mapping.mapMethod(m.getName(), binding.getMethod(m).getName()));
        return mapping;
    }

    public static boolean compile(Collection<File> source, File destination, Collection<String> classPaths) {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null);
        List<String> options = List.of("-classpath",
                System.getProperty("java.class.path") + File.pathSeparator + String.join(File.pathSeparator, classPaths),
                "-d",
                destination.getPath(),
                "-source",
                "11",
                "-target",
                "11");
        Iterable<? extends JavaFileObject> compilations = files.getJavaFileObjectsFromFiles(source);
        JavaCompiler.CompilationTask task = compiler.getTask(Writer.nullWriter(), files, diagnostics, options, null, compilations);
        boolean result = task.call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic);
        }

        return result;
    }

    public static void createJar(File sourceDirectory, File jarFile) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        try (JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            add(sourceDirectory, sourceDirectory, out);
        }
    }

    private static void add(File root, File source, JarOutputStream jar) throws IOException {
        if (source.isDirectory()) {
            for (File subFile : Optional.ofNullable(source.listFiles()).orElse(new File[0])) {
                add(root, subFile, jar);
            }
        } else if (source.isFile() && source.getName().endsWith(".class")) {
            String sourceName = source.getPath().substring(root.getPath().length()).replace('\\', '/');
            if (sourceName.startsWith("/")) {
                sourceName = sourceName.substring(1);
            }

            JarEntry entry = new JarEntry(sourceName);
            jar.putNextEntry(entry);
            try (InputStream in = new FileInputStream(source)) {
                in.transferTo(jar);
                jar.closeEntry();
            }
        }
    }
}