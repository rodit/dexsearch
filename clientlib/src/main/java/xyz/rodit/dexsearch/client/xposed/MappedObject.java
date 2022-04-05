package xyz.rodit.dexsearch.client.xposed;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import xyz.rodit.dexsearch.client.ClassMapping;
import xyz.rodit.dexsearch.client.Mappings;

import java.util.Collection;
import java.util.Collections;

public class MappedObject {

    public final ClassMapping mapping;
    public final Object instance;

    public MappedObject(ClassMapping mapping) {
        this(mapping, new Object[0]);
    }

    public MappedObject(ClassMapping mapping, Object instance) {
        this.mapping = mapping;
        this.instance = instance;
    }

    public MappedObject(ClassMapping mapping, Object[] args) {
        this.mapping = mapping;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof MappedObject) {
                    args[i] = ((MappedObject) args[i]).instance;
                }
            }
        }

        this.instance = XposedHelpers.newInstance(XposedHelpers.findClass(mapping.getDexClassName(), Mappings.getClassLoader()), args);
    }

    public boolean isNull() {
        return instance == null;
    }

    public boolean isNotNull() {
        return instance != null;
    }

    public Object get(String fieldName) {
        return XposedHelpers.getObjectField(instance, mapping.getDexField(fieldName));
    }

    public static Object getStatic(String niceClassName, String fieldName) {
        return XposedHelpers.getStaticObjectField(Mappings.getClass(niceClassName), Mappings.get(niceClassName).getDexField(fieldName));
    }

    public void set(String fieldName, Object value) {
        if (value instanceof MappedObject) {
            value = ((MappedObject) value).instance;
        }

        XposedHelpers.setObjectField(instance, mapping.getDexField(fieldName), value);
    }

    public static void setStatic(String niceClassName, String fieldName, Object value) {
        if (value instanceof MappedObject) {
            value = ((MappedObject) value).instance;
        }

        XposedHelpers.setStaticObjectField(Mappings.getClass(niceClassName), Mappings.get(niceClassName).getDexField(fieldName), value);
    }

    public Object call(String methodName, Object... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof MappedObject) {
                    args[i] = ((MappedObject) args[i]).instance;
                }
            }
        }

        return XposedHelpers.callMethod(instance, mapping.getDexMethod(methodName), args);
    }

    public static Object callStatic(String niceClassName, String methodName, Object... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof MappedObject) {
                    args[i] = ((MappedObject) args[i]).instance;
                }
            }
        }

        return XposedHelpers.callStaticMethod(Mappings.getClass(niceClassName), Mappings.get(niceClassName).getDexMethod(methodName), args);
    }

    public static Collection<XC_MethodHook.Unhook> hook(String className, String methodName, XC_MethodHook hook) {
        Class<?> cls = Mappings.getClass(className);
        if (cls != null) {
            ClassMapping mapping = Mappings.get(className);
            if (mapping != null) {
                return XposedBridge.hookAllMethods(cls, mapping.getDexMethod(methodName), hook);
            } else {
                XposedBridge.log("Cannot hook method. No mapping found for method " + methodName + " in class " + className + ".");
            }
        } else {
            XposedBridge.log("Cannot hook class with alias " + className + ". No mapping found.");
        }

        return Collections.emptySet();
    }

    public static Collection<XC_MethodHook.Unhook> hookConstructors(String className, XC_MethodHook hook) {
        Class<?> cls = Mappings.getClass(className);
        if (cls != null) {
            return XposedBridge.hookAllConstructors(cls, hook);
        } else {
            XposedBridge.log("Cannot hook class with alias " + className + ". No mapping found.");
        }

        return Collections.emptySet();
    }

    public static boolean isInstance(String className, Object test) {
        Class<?> cls = Mappings.getClass(className);
        if (cls != null && test != null) {
            return cls.isAssignableFrom(test.getClass());
        }

        return false;
    }

    @Override
    public String toString() {
        return mapping.getNiceClassName() + "{" +
                "instance=" + instance +
                '}';
    }
}
