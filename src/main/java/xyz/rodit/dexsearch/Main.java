package xyz.rodit.dexsearch;

import org.apache.commons.cli.*;
import org.jf.dexlib2.iface.ClassDef;
import xyz.rodit.dexsearch.codegen.ClassChecker;
import xyz.rodit.dexsearch.codegen.CodegenUtils;
import xyz.rodit.dexsearch.codegen.EmbeddedMappingsGenerator;
import xyz.rodit.dexsearch.codegen.Packager;
import xyz.rodit.dexsearch.codegen.android.AndroidStubLoader;
import xyz.rodit.dexsearch.codegen.xposed.XposedClassGenerator;
import xyz.rodit.dexsearch.dex.DexBase;
import xyz.rodit.dexsearch.parser.SchemaLoader;
import xyz.rodit.dexsearch.resolver.DefaultResolver;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.SchemaNode;
import xyz.rodit.dexsearch.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    public static void main(String[] args) throws ParseException, IOException {
        Options options = new Options()
                .addOption("h", "help", false, "Displays help information.");

        CommandLine helpCli = null;

        try {
            helpCli = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            // ignore unrecognised options
        }

        options.addRequiredOption("i", "input", true, "The input apk file containing DEX binaries.")
                .addRequiredOption("o", "output", true, "The output file to write the schema mapping metadata.")
                .addRequiredOption("s", "schema", true, "The schema file.")
                .addOption("j", "jar", true, "The destination for the jar containing generated classes for the mappings.")
                .addOption("e", "embed", false, "Embed the mappings into the generated jar file. Call EmbeddedMappings#load(ClassLoader) to load them.")
                .addOption("p", "package", true, "The name of the package the generated classes should be generated under.")
                .addOption("a", "android", true, "The location of the android stub jar (included in the Android SDK) to see if class names are valid or should be changed to java.lang.Object.")
                .addOption("b", "base", true, "The fully qualified name of the base class of the generated mapping classes.");

        if (helpCli != null && helpCli.hasOption("h")) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("dexsearch", options);
            return;
        }

        CommandLine cli = new DefaultParser().parse(options, args);

        File input = new File(cli.getOptionValue("i"));
        File output = new File(cli.getOptionValue("o"));
        File schema = new File(cli.getOptionValue("s"));
        File jar = cli.hasOption("j") ? new File(cli.getOptionValue("j")) : null;
        String packageName = cli.getOptionValue("p");
        File androidJar = cli.hasOption("a") ? new File(cli.getOptionValue("a")) : null;
        String baseClass = cli.getOptionValue("b", "xyz.rodit.dexsearch.client.xposed.MappedObject");
        boolean embedMappings = cli.hasOption("e");

        if (!input.exists()) {
            System.err.println("Input file not found: " + input);
            return;
        }

        if (!schema.exists()) {
            System.err.println("Schema file not found: " + schema);
            return;
        }

        SchemaNode schemaNode = SchemaLoader.load(schema);

        DexBase dex = new DexBase();
        ZipFile apk = new ZipFile(input);
        List<File> dexFiles = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = apk.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".dex")) {
                System.out.println("Extracting dex file " + entry.getName() + ".");
                File tmpFile = File.createTempFile("tmp", "dex");
                tmpFile.deleteOnExit();
                Files.copy(apk.getInputStream(entry), Paths.get(tmpFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
                dex.load(tmpFile);
                dexFiles.add(tmpFile);
            }
        }

        apk.close();
        System.out.println("Loaded " + dex.size() + " classes.");

        Resolver resolver = new DefaultResolver(schemaNode.getOptions(), dex, schemaNode.getClasses());
        Map<String, ClassBinding> bindings = resolver.resolveAll();


        FileUtils.createParentDir(output);
        Packager.createMappingsFile(bindings, 0, output);

        if (jar != null) {
            FileUtils.createParentDir(jar);
            File tmpDir = Files.createTempDirectory("dexsearch").toFile();
            File srcDir = new File(tmpDir, "src");
            File binDir = new File(tmpDir, "bin");
            srcDir.mkdirs();
            binDir.mkdirs();

            boolean useAndroidJar = androidJar != null && androidJar.exists();
            List<String> classPaths = new ArrayList<>();
            classPaths.add(srcDir.getPath());
            if (useAndroidJar) {
                classPaths.add(androidJar.getPath());
            }

            ClassChecker checker = new ClassChecker(List.of(useAndroidJar
                    ? AndroidStubLoader.loadStubs(androidJar)
                    : ClassLoader.getSystemClassLoader()));

            List<File> compilationFiles = new ArrayList<>();
            for (String key : bindings.keySet()) {
                ClassBinding binding = bindings.get(key);
                if (binding.getNode().hasAttribute(Attribute.DISCARD)) {
                    continue;
                }

                XposedClassGenerator gen = new XposedClassGenerator(checker, baseClass, packageName);
                String generated = gen.generate(resolver, binding);

                File outputFile = new File(srcDir, binding.getNode().getName() + ".java");
                try (PrintWriter out = new PrintWriter(outputFile)) {
                    out.print(generated);
                }

                compilationFiles.add(outputFile);
            }

            if (embedMappings) {
                File embeddedMappingsFile = new File(srcDir, "EmbeddedMappings.java");
                String generated = EmbeddedMappingsGenerator.generateClass(packageName, output);
                try (PrintWriter out = new PrintWriter(embeddedMappingsFile)) {
                    out.print(generated);
                }

                compilationFiles.add(embeddedMappingsFile);
            }

            if (Packager.compile(compilationFiles, binDir, classPaths)) {
                Packager.createJar(binDir, jar);
            } else {
                System.err.println("Compilation failed.");
            }
        }

        for (File dexFile : dexFiles) {
            dexFile.delete();
        }
    }
}
