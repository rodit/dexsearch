package xyz.rodit.dexsearch.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import xyz.rodit.dexsearch.antlr.SchemaGrammarBaseVisitor;
import xyz.rodit.dexsearch.antlr.SchemaGrammarParser;
import xyz.rodit.dexsearch.tree.AccessUtils;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.attributes.Target;
import xyz.rodit.dexsearch.tree.bindings.options.Option;
import xyz.rodit.dexsearch.tree.bindings.options.Options;
import xyz.rodit.dexsearch.tree.nodes.ClassNode;
import xyz.rodit.dexsearch.tree.nodes.FieldNode;
import xyz.rodit.dexsearch.tree.nodes.MethodNode;
import xyz.rodit.dexsearch.tree.nodes.SchemaNode;
import xyz.rodit.dexsearch.tree.nodes.bytecode.*;
import xyz.rodit.dexsearch.tree.nodes.bytecode.events.*;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.*;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions.SetFieldToInt16;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions.SetFieldToRegister;
import xyz.rodit.dexsearch.tree.nodes.bytecode.matchers.expressions.SetFieldToString;
import xyz.rodit.dexsearch.tree.properties.Annotation;
import xyz.rodit.dexsearch.tree.properties.names.AnyName;
import xyz.rodit.dexsearch.tree.properties.names.BytecodeMemberName;
import xyz.rodit.dexsearch.tree.properties.Name;
import xyz.rodit.dexsearch.tree.properties.names.ExactName;
import xyz.rodit.dexsearch.tree.properties.names.ReferenceName;
import xyz.rodit.dexsearch.tree.properties.types.*;
import xyz.rodit.dexsearch.utils.EnumUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SchemaVisitor extends SchemaGrammarBaseVisitor<Object> {

    private final List<Exception> errors = new ArrayList<>();
    private final Options options = new Options();
    private final Map<String, String> importedTypes = new HashMap<>();

    public List<Exception> getErrors() {
        return errors;
    }

    @Override
    public SchemaNode visitSchema(SchemaGrammarParser.SchemaContext ctx) {
        ctx.directive().forEach(super::visit);
        ctx.importStatement().forEach(this::visitImportStatement);
        List<ClassNode> classes = ctx.classDefinition().stream().map(this::visitClassDefinition).toList();
        return new SchemaNode(options, classes);
    }

    @Override
    public Object visitImportStatement(SchemaGrammarParser.ImportStatementContext ctx) {
        String fullTypeName = ctx.JAVA_TYPE_IDENTIFIER().getText();
        String[] parts = fullTypeName.split("\\.");
        String shortTypeName = parts[parts.length - 1];
        if (importedTypes.containsKey(shortTypeName)) {
            errors.add(new ParserException("'" + shortTypeName + "' imported as both '" + importedTypes.get(shortTypeName) + "' and '" + fullTypeName + "'.", ctx));
        } else {
            importedTypes.put(shortTypeName, fullTypeName);
        }
        return null;
    }

    public EnumSet<Attribute> visitSchemaAttributes(SchemaGrammarParser.SchemaAttributesContext ctx, Target target) {
        EnumSet<Attribute> attribs = EnumSet.noneOf(Attribute.class);
        if (ctx == null) {
            return attribs;
        }

        for (TerminalNode identNode : ctx.identifierList().IDENTIFIER()) {
            Attribute attrib = EnumUtils.valueOfSafe(Attribute.class, identNode.getText().toUpperCase());
            if (attrib != null) {
                if (attrib.isValidTarget(target)) {
                    attribs.add(attrib);
                } else {
                    errors.add(new ParserException("Attribute invalid for " + target + ": " + attrib + ".", identNode.getSymbol()));
                }
            } else {
                errors.add(new ParserException("Unknown attribute " + identNode.getText() + ".", identNode.getSymbol()));
            }
        }

        return attribs;
    }

    @Override
    public Annotation visitAnnotation(SchemaGrammarParser.AnnotationContext ctx) {
        Object visitedType = super.visit(ctx.type());
        if (visitedType instanceof Type annotationType) {
            return new Annotation(annotationType, ctx.annotationArguments().constant().stream().map(super::visit).collect(Collectors.toList()));
        }

        return null;
    }

    @Override
    public ClassNode visitClassDefinition(SchemaGrammarParser.ClassDefinitionContext ctx) {
        EnumSet<Attribute> attributes = visitSchemaAttributes(ctx.definitionPrefix().schemaAttributes(), Target.CLASS);
        int accessModifiers = AccessUtils.getModifiers(ctx.definitionPrefix().ACCESS_FLAG().stream().map(ParseTree::getText).collect(Collectors.toList()))
                | AccessUtils.getModifierValue(ctx.CLASS_TYPE().getText());
        Name name = visitName(ctx.name());
        String expected = ctx.expectsStatement() != null
                ? visitJavaTypeName(ctx.expectsStatement().javaTypeName())
                : null;
        Type extendsType = ctx.extendsStatement() != null
                ? visitType(ctx.extendsStatement().type())
                : null;
        List<Type> interfaceTypes = ctx.implementsStatement() != null
                ? ctx.implementsStatement().type().stream().map(this::visitType).toList()
                : List.of();
        List<Annotation> annotations = ctx.definitionPrefix().annotation().stream().map(this::visitAnnotation).toList();
        List<FieldNode> fields = ctx.fieldDefinition().stream().map(this::visitFieldDefinition).toList();
        List<MethodNode> methods = ctx.methodDefinition().stream().map(this::visitMethodDefinition).toList();
        return new ClassNode(attributes, accessModifiers, name, expected, annotations, extendsType, interfaceTypes, fields, methods);
    }

    @Override
    public FieldNode visitFieldDefinition(SchemaGrammarParser.FieldDefinitionContext ctx) {
        EnumSet<Attribute> attributes = visitSchemaAttributes(ctx.definitionPrefix().schemaAttributes(), Target.FIELD);
        int accessModifiers = AccessUtils.getModifiers(ctx.definitionPrefix().ACCESS_FLAG().stream().map(ParseTree::getText).collect(Collectors.toList()));
        Type type = visitType(ctx.type());
        Name name = visitName(ctx.name());
        List<Annotation> annotations = ctx.definitionPrefix().annotation().stream().map(this::visitAnnotation).toList();
        return new FieldNode(attributes, accessModifiers, type, name, annotations);
    }

    @Override
    public MethodNode visitMethodDefinition(SchemaGrammarParser.MethodDefinitionContext ctx) {
        EnumSet<Attribute> attributes = visitSchemaAttributes(ctx.definitionPrefix().schemaAttributes(), Target.METHOD);
        int accessModifiers = AccessUtils.getModifiers(ctx.definitionPrefix().ACCESS_FLAG().stream().map(ParseTree::getText).collect(Collectors.toList()));
        Type type = visitType(ctx.type());
        Name name = visitName(ctx.name());
        List<Annotation> annotations = ctx.definitionPrefix().annotation().stream().map(this::visitAnnotation).toList();
        List<Type> parameterTypes = visitTypeList(ctx.typeList());
        List<BodyNode> body = ctx.methodBody() != null
                ? ctx.methodBody().methodBodyMatcher().stream().map(this::visitMethodBodyMatcher).toList()
                : List.of();
        return new MethodNode(attributes, accessModifiers, type, name, annotations, parameterTypes, body);
    }

    @Override
    public BodyNode visitMethodBodyMatcher(SchemaGrammarParser.MethodBodyMatcherContext ctx) {
        Object instrMatcher = visitChildren(ctx);
        if (instrMatcher instanceof SingleInstructionMatcher matcher) {
            EnumSet<Attribute> attributes = visitSchemaAttributes(ctx.definitionPrefix().schemaAttributes(), Target.INSTRUCTION);
            List<BindEvent> events = ctx.bindEvent().stream().map(this::visitBindEvent).toList();
            return new BodyNode(attributes, matcher, events);
        }

        errors.add(new ParserException("Failed to parse instruction matcher for method body.", ctx));
        return null;
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        if (node instanceof SchemaGrammarParser.MethodBodyMatcherContext) {
            return !(currentResult instanceof SingleInstructionMatcher);
        }

        return super.shouldVisitNextChild(node, currentResult);
    }

    @Override
    public StringMatcher visitBodyStringAny(SchemaGrammarParser.BodyStringAnyContext ctx) {
        return new StringMatcher(parseString(ctx.STRING_LIT().getText()), false, false);
    }

    @Override
    public StringMatcher visitBodyStringContains(SchemaGrammarParser.BodyStringContainsContext ctx) {
        return new StringMatcher(parseString(ctx.STRING_LIT().getText()), true, false);
    }

    @Override
    public StringMatcher visitBodyStringRegex(SchemaGrammarParser.BodyStringRegexContext ctx) {
        return new StringMatcher(parseString(ctx.STRING_LIT().getText()), false, true);
    }

    @Override
    public TypeReferenceMatcher visitMethodBodyTypeReference(SchemaGrammarParser.MethodBodyTypeReferenceContext ctx) {
        Type type = visitType(ctx.type());
        return new TypeReferenceMatcher(type);
    }

    @Override
    public MethodReferenceMatcher visitMethodBodyMethodReference(SchemaGrammarParser.MethodBodyMethodReferenceContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        return new MethodReferenceMatcher(type, name);
    }

    @Override
    public FieldReferenceMatcher visitMethodBodyFieldReference(SchemaGrammarParser.MethodBodyFieldReferenceContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        return new FieldReferenceMatcher(type, name);
    }

    @Override
    public Object visitMethodBodyNewInstance(SchemaGrammarParser.MethodBodyNewInstanceContext ctx) {
        Type type = visitType(ctx.type());
        return new NewInstanceMatcher(type);
    }

    @Override
    public SetFieldToRegister visitSetRegisterExpression(SchemaGrammarParser.SetRegisterExpressionContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        int register = Integer.parseInt(ctx.INT_LIT().getText());
        return new SetFieldToRegister(type, name, register, false);
    }

    @Override
    public Object visitSetParamExpression(SchemaGrammarParser.SetParamExpressionContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        int register = Integer.parseInt(ctx.INT_LIT().getText());
        return new SetFieldToRegister(type, name, register, true);
    }

    @Override
    public SetFieldToString visitSetStringExpression(SchemaGrammarParser.SetStringExpressionContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        String string = parseString(ctx.STRING_LIT().getText());
        return new SetFieldToString(type, name, string);
    }

    @Override
    public SetFieldToInt16 visitSetIntExpression(SchemaGrammarParser.SetIntExpressionContext ctx) {
        SchemaGrammarParser.BytecodeMemberReferenceContext memberRef = ctx.bytecodeMemberReference();
        Type type = visitType(memberRef.type());
        BytecodeMemberName name = (BytecodeMemberName) visit(memberRef.bytecodeMemberName());
        int value = Integer.parseInt(ctx.INT_LIT().getText());
        return new SetFieldToInt16(type, name, value);
    }

    @Override
    public BytecodeMemberName visitNormalMemberName(SchemaGrammarParser.NormalMemberNameContext ctx) {
        return new ExactName(ctx.IDENTIFIER().getText());
    }

    @Override
    public Object visitReferenceMemberName(SchemaGrammarParser.ReferenceMemberNameContext ctx) {
        return new ReferenceName(ctx.IDENTIFIER().getText());
    }

    @Override
    public BytecodeMemberName visitAnyMemberName(SchemaGrammarParser.AnyMemberNameContext ctx) {
        return new AnyName();
    }

    @Override
    public BindEvent visitBindEvent(SchemaGrammarParser.BindEventContext ctx) {
        Operation operation = EnumUtils.valueOfSafe(Operation.class, ctx.BIND_EVENT_OPERATION().getText().toUpperCase());
        EventTarget target = EnumUtils.valueOfSafe(EventTarget.class, ctx.BIND_EVENT_TARGET().getText().toUpperCase());
        String name = ctx.IDENTIFIER().getText();
        Modifier modifier = Modifiers.get(ctx.BIND_EVENT_MODIFIER().getText());
        EventSource source = EnumUtils.valueOfSafe(EventSource.class, ctx.BIND_EVENT_SOURCE().getText().toUpperCase());
        if (operation == null) {
            errors.add(new ParserException("Invalid bind event operation.", ctx));
        }
        if (target == null) {
            errors.add(new ParserException("Invalid bind event target.", ctx));
        }
        if (modifier == null) {
            errors.add(new ParserException("Invalid bind event modifier.", ctx));
        }
        if (source == null) {
            errors.add(new ParserException("Invalid bind event source.", ctx));
        }
        return new BindEvent(operation, target, name, modifier, source);
    }

    @Override
    public List<Type> visitTypeList(SchemaGrammarParser.TypeListContext ctx) {
        return ctx.type().stream().map(this::visitType).collect(Collectors.toList());
    }

    @Override
    public Object visitOptionDirective(SchemaGrammarParser.OptionDirectiveContext ctx) {
        String name = ctx.IDENTIFIER(0).getText();
        String value = ctx.IDENTIFIER(1).getText();
        Option opt = EnumUtils.valueOfSafe(Option.class, name.toUpperCase());
        if (opt != null) {
            Object val = opt.parser().parse(value);
            if (val != null) {
                options.set(opt, val);
            } else {
                errors.add(new ParserException("Failed to parse value for option " + opt + "=" + value + ".", ctx));
            }
        } else {
            errors.add(new ParserException("Unknown option " + name + ".", ctx));
        }

        return null;
    }

    @Override
    public String visitJavaTypeName(SchemaGrammarParser.JavaTypeNameContext ctx) {
        return Optional.ofNullable(ctx.JAVA_TYPE_IDENTIFIER()).orElse(ctx.IDENTIFIER()).getText();
    }

    @Override
    public ExtendsType visitExtendsType(SchemaGrammarParser.ExtendsTypeContext ctx) {
        Type baseType = visitType(ctx.type());
        if (baseType != null) {
            return new ExtendsType(baseType);
        }

        errors.add(new ParserException("Failed to parse base type for extends type.", ctx));
        return null;
    }

    @Override
    public ImplementsType visitImplementsType(SchemaGrammarParser.ImplementsTypeContext ctx) {
        List<Type> interfaces = new ArrayList<>();
        for (SchemaGrammarParser.TypeContext typeCtx : ctx.typeList().type()) {
            Type baseType = visitType(typeCtx);
            if (baseType != null) {
                interfaces.add(baseType);
            } else {
                errors.add(new ParserException("Failed to parse base type for implements type.", typeCtx));
            }
        }

        return new ImplementsType(interfaces);
    }

    @Override
    public ThisType visitThisType(SchemaGrammarParser.ThisTypeContext ctx) {
        return new ThisType();
    }

    @Override
    public ReferencedType visitReferencedType(SchemaGrammarParser.ReferencedTypeContext ctx) {
        return new ReferencedType(ctx.IDENTIFIER().getText());
    }

    @Override
    public OutType visitOutType(SchemaGrammarParser.OutTypeContext ctx) {
        return new OutType(ctx.IDENTIFIER().getText());
    }

    @Override
    public JavaType visitJavaType(SchemaGrammarParser.JavaTypeContext ctx) {
        String typeName = visitJavaTypeName(ctx.javaTypeName());
        if (importedTypes.containsKey(typeName)) {
            typeName = importedTypes.get(typeName);
        }
        return new JavaType(typeName);
    }

    @Override
    public JavaType visitPrimitiveType(SchemaGrammarParser.PrimitiveTypeContext ctx) {
        return new JavaType(ctx.PRIMITIVE().getText());
    }

    @Override
    public AnyType visitAnyType(SchemaGrammarParser.AnyTypeContext ctx) {
        return new AnyType();
    }

    @Override
    public ArrayType visitArrayType(SchemaGrammarParser.ArrayTypeContext ctx) {
        Type baseType = visitType(ctx.type());
        if (baseType != null) {
            return new ArrayType(baseType);
        }

        errors.add(new ParserException("Failed to parse base type for extends type.", ctx));
        return null;
    }

    @Override
    public VarargsType visitVarargsType(SchemaGrammarParser.VarargsTypeContext ctx) {
        return new VarargsType();
    }

    @Override
    public Object visitStringLiteral(SchemaGrammarParser.StringLiteralContext ctx) {
        return parseString(ctx.getText());
    }

    @Override
    public Object visitBoolLiteral(SchemaGrammarParser.BoolLiteralContext ctx) {
        String lit = ctx.getText();
        if (lit.equals("true")) {
            return Boolean.TRUE;
        } else if (lit.equals("false")) {
            return Boolean.FALSE;
        }

        // should be impossible.
        return null;
    }

    @Override
    public Object visitIntLiteral(SchemaGrammarParser.IntLiteralContext ctx) {
        try {
            return Integer.valueOf(ctx.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Object visitCharLiteral(SchemaGrammarParser.CharLiteralContext ctx) {
        return escapeString(ctx.getText()).charAt(1);
    }

    @Override
    public Object visitFloatLiteral(SchemaGrammarParser.FloatLiteralContext ctx) {
        try {
            return Float.valueOf(ctx.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Name visitName(SchemaGrammarParser.NameContext ctx) {
        String text = ctx.IDENTIFIER().getText();
        boolean exact = ctx.DOLLAR() != null;
        return new Name(text, exact);
    }

    private Type visitType(SchemaGrammarParser.TypeContext ctx) {
        Object visitedType = super.visit(ctx);
        if (visitedType instanceof Type type) {
            return type;
        }

        errors.add(new ParserException("Failed to parse type " + ctx.getText() + ".", ctx));
        return null;
    }

    private String parseString(String str) {
        return escapeString(str.substring(1, str.length() - 1));
    }

    private String escapeString(String str) {
        return str.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\'", "'")
                .replace("\\\"", "\"");
    }
}
