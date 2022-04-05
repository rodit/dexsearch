package xyz.rodit.dexsearch.tree;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;

public class ClassUtils {

    public static Field findField(ClassDef cls, String name) {
        for (Field field : cls.getFields()) {
            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    public static Method findMethod(ClassDef cls, String name) {
        for (Method method : cls.getMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }

        return null;
    }
}
