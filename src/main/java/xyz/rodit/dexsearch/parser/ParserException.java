package xyz.rodit.dexsearch.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

public class ParserException extends Exception {

    public ParserException(String message, ParserRuleContext ctx) {
        this(message, ctx.start);
    }

    public ParserException(String message, Token token) {
        this(message, token.getLine(), token.getCharPositionInLine());
    }

    public ParserException(String message, int line, int column) {
        super("(" + line + ", " + column + "): " + message);
    }
}
