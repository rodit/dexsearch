package xyz.rodit.dexsearch.codegen.xposed;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import xyz.rodit.dexsearch.codegen.ClassChecker;
import xyz.rodit.dexsearch.codegen.ClassGenerator;
import xyz.rodit.dexsearch.codegen.CodegenUtils;
import xyz.rodit.dexsearch.codegen.ResolvedType;
import xyz.rodit.dexsearch.resolver.Resolver;
import xyz.rodit.dexsearch.tree.AccessUtils;
import xyz.rodit.dexsearch.tree.attributes.Attribute;
import xyz.rodit.dexsearch.tree.bindings.ClassBinding;
import xyz.rodit.dexsearch.tree.nodes.FieldNode;
import xyz.rodit.dexsearch.tree.nodes.MethodNode;

import java.util.List;
import java.util.stream.IntStream;

public class XposedClassGenerator implements ClassGenerator {

    private final ClassChecker checker;
    private final String baseClass;
    private final String classPackage;

    public XposedClassGenerator(ClassChecker checker, String baseClass, String classPackage) {
        this.checker = checker;
        this.baseClass = baseClass;
        this.classPackage = classPackage;
    }

    @Override
    public String generate(Resolver resolver, ClassBinding binding) {
        boolean isEnum = AccessUtils.hasModifiers(binding.get().getAccessFlags(), AccessFlags.ENUM.getValue());

        StringBuilder source = new StringBuilder();
        if (classPackage != null) {
            source.append("package ")
                    .append(classPackage)
                    .append(';');
        }

        source.append("import xyz.rodit.dexsearch.client.*;import xyz.rodit.dexsearch.client.xposed.*;")
                .append("public class ")
                .append(binding.getNode().getName())
                .append(" extends ")
                .append(baseClass)
                .append('{');

        source.append("public static ConstructorRef constructors=new ConstructorRef(\"")
                .append(binding.getNode().getName())
                .append("\");");

        source.append("public ")
                .append(binding.getNode().getName())
                .append("(Object instance){super(Mappings.get(\"")
                .append(binding.getNode().getName())
                .append("\"),instance);}");

        source.append("public ")
                .append(binding.getNode().getName())
                .append("(Object[] args){super(Mappings.get(\"")
                .append(binding.getNode().getName())
                .append("\"),args);}");

        source.append("public static ")
                .append(binding.getNode().getName())
                .append(" wrap(Object instance){return new ")
                .append(binding.getNode().getName())
                .append("(instance);}");

        source.append("public static void hook(String method,de.robv.android.xposed.XC_MethodHook hook){hook(\"")
                .append(binding.getNode().getName())
                .append("\",method,hook);}");

        source.append("public static boolean isInstance(Object test){return MappedObject.isInstance(\"")
                .append(binding.getNode().getName())
                .append("\",test);}");

        source.append("public static ClassMapping getMapping(){return Mappings.get(\"")
                .append(binding.getNode().getName())
                .append("\");}");

        source.append("public static Class<?> getMappedClass(){return Mappings.getClass(\"")
                .append(binding.getNode().getName())
                .append("\");}");

        if (isEnum) {
            source.append("public static ")
                    .append(binding.getNode().getName())
                    .append(" valueOf(String name){return ")
                    .append(binding.getNode().getName())
                    .append(".wrap(callStatic(\"")
                    .append(binding.getNode().getName())
                    .append("\",\"valueOf\", name));}");
        }

        binding.getBoundMethods().stream()
                .filter(m -> !m.hasAttribute(Attribute.DISCARD)
                        && !binding.getMethod(m).getName().equals("<clinit>"))
                .forEach(methodNode -> source.append(generateMethod(resolver, binding, methodNode)));

        binding.getBoundFields().stream()
                .filter(f -> !f.hasAttribute(Attribute.DISCARD))
                .forEach(fieldNode -> source.append(generateGetter(resolver, binding, fieldNode))
                        .append(generateSetter(resolver, binding, fieldNode)));

        return source.append('}')
                .toString();
    }

    private String generateMethod(Resolver resolver, ClassBinding binding, MethodNode methodNode) {
        Method method = binding.getMethod(methodNode);
        ResolvedType returnType = ResolvedType.get(checker, resolver, method.getReturnType());
        boolean isConstructor = AccessUtils.hasModifiers(method.getAccessFlags(), AccessFlags.CONSTRUCTOR.getValue());
        boolean isStatic = AccessUtils.hasModifiers(method.getAccessFlags(), AccessFlags.STATIC.getValue());

        StringBuilder source = new StringBuilder();
        if (!isConstructor) {
            source.append("public static MethodRef ")
                    .append(methodNode.getName())
                    .append("=new MethodRef(\"")
                    .append(binding.getNode().getName())
                    .append("\",\"")
                    .append(methodNode.getName())
                    .append("\");");
        }

        source.append("public ");

        if (isStatic) {
            source.append("static ");
        }

        if (isConstructor) {
            source.append(binding.getNode().getName());
        } else {
            source.append(returnType)
                    .append(' ')
                    .append(methodNode.getName());
        }

        source.append('(');

        List<? extends CharSequence> paramTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.size(); i++) {
            source.append(ResolvedType.get(checker, resolver, paramTypes.get(i).toString()))
                    .append(" arg")
                    .append(i);
            if (i < paramTypes.size() - 1) {
                source.append(',');
            }
        }

        source.append("){");
        List<String> argNames = IntStream.range(0, paramTypes.size()).mapToObj(i -> "arg" + i).toList();

        if (isConstructor) {
            source.append("this(new Object[]{")
                    .append(String.join(",", argNames))
                    .append("})");
        } else {
            StringBuilder bodySource = new StringBuilder().append("call")
                    .append(isStatic ? "Static(\"" + binding.getNode().getName() + "\"," : "(")
                    .append('"')
                    .append(methodNode.getName())
                    .append("\"");

            for (int i = 0; i < paramTypes.size(); i++) {
                bodySource.append(",arg").append(i);
            }

            bodySource.append(")");
            if (returnType.returns()) {
                source.append("return ");
            }

            source.append(returnType.wrap(bodySource.toString()));
        }

        return source.append(";}").toString();
    }

    private String generateGetter(Resolver resolver, ClassBinding binding, FieldNode fieldNode) {
        Field field = binding.getField(fieldNode);
        boolean isStatic = AccessUtils.hasModifiers(field.getAccessFlags(), AccessFlags.STATIC.getValue());
        boolean isEnum = AccessUtils.hasModifiers(binding.get().getAccessFlags(), AccessFlags.ENUM.getValue());
        ResolvedType type = ResolvedType.get(checker, resolver, field.getType());
        return "public " +
                (isStatic ? "static " : "") +
                type +
                ' ' +
                (isEnum ? fieldNode.getName() : CodegenUtils.getCamelCase("get", fieldNode.getName())) +
                "(){return " +
                type.wrap("get" +
                        (isStatic ? "Static(\"" + binding.getNode().getName() + "\"," : "(") +
                        "\"" + fieldNode.getName() + "\")") +
                ";}";
    }

    private String generateSetter(Resolver resolver, ClassBinding binding, FieldNode fieldNode) {
        Field field = binding.getField(fieldNode);
        boolean isStatic = AccessUtils.hasModifiers(field.getAccessFlags(), AccessFlags.STATIC.getValue());
        return "public " +
                (isStatic ? "static " : "") +
                "void " +
                CodegenUtils.getCamelCase("set", fieldNode.getName()) +
                "(" +
                ResolvedType.get(checker, resolver, field.getType()) +
                " value){set" +
                (isStatic ? "Static(\"" + binding.getNode().getName() + "\"," : "(") +
                "\"" +
                fieldNode.getName() +
                "\",value);}";
    }
}
