package xyz.rodit.dexsearch.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import xyz.rodit.dexsearch.antlr.SchemaGrammarLexer;
import xyz.rodit.dexsearch.antlr.SchemaGrammarParser;
import xyz.rodit.dexsearch.tree.nodes.SchemaNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchemaLoader {

    public static SchemaNode load(File file) throws IOException {
        SchemaGrammarLexer lexer = new SchemaGrammarLexer(CharStreams.fromStream(new FileInputStream(file)));
        SchemaGrammarParser parser = new SchemaGrammarParser(new CommonTokenStream(lexer));

        SchemaVisitor visitor = new SchemaVisitor();
        SchemaGrammarParser.SchemaContext context = parser.schema();
        SchemaNode node = visitor.visitSchema(context);
        for (Exception error : visitor.getErrors()) {
            System.err.println(error.getMessage());
        }
        return node;
    }
}
