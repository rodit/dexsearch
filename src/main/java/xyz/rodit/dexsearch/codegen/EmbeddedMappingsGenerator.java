package xyz.rodit.dexsearch.codegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class EmbeddedMappingsGenerator {

    public static String generateClass(String packageName, File mappingsFile) throws IOException {
        String content = escape(Files.readString(mappingsFile.toPath()));
        return "package " + packageName + ";" +
                "import java.io.InputStream;" +
                "import java.io.ByteArrayInputStream;" +
                "import java.io.IOException;" +
                "import xyz.rodit.dexsearch.client.Mappings;" +
                "public class EmbeddedMappings{" +
                    "public static void load(ClassLoader loader)throws IOException{" +
                        "try(InputStream i=new ByteArrayInputStream(\"" + content + "\".getBytes())){" +
                            "Mappings.loadMappings(loader,i);" +
                        "}" +
                    "}" +
                "}";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("'", "\\'")
                .replace("\"", "\\\"");
    }
}
