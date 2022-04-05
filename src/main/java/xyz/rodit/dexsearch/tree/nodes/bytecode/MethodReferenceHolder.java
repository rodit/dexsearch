package xyz.rodit.dexsearch.tree.nodes.bytecode;

import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.reference.MethodReference;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class MethodReferenceHolder implements Method {

    private final MethodReference method;

    public MethodReferenceHolder(MethodReference method) {
        this.method = method;
    }

    @Override
    public String getDefiningClass() {
        return method.getDefiningClass();
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public List<? extends CharSequence> getParameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public List<? extends MethodParameter> getParameters() {
        List<? extends CharSequence> types = method.getParameterTypes();
        return IntStream.range(0, types.size()).mapToObj(i -> new MethodReferenceParameter(i, types.get(i).toString())).toList();
    }

    @Override
    public String getReturnType() {
        return method.getReturnType();
    }

    @Override
    public int compareTo(MethodReference methodReference) {
        return 0;
    }

    @Override
    public int getAccessFlags() {
        return 0;
    }

    @Override
    public Set<? extends Annotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Override
    public Set<HiddenApiRestriction> getHiddenApiRestrictions() {
        return Collections.emptySet();
    }

    @Override
    public MethodImplementation getImplementation() {
        return null;
    }

    @Override
    public void validateReference() {

    }

    private static class MethodReferenceParameter implements MethodParameter {

        private final int index;
        private final String type;

        private MethodReferenceParameter(int index, String type) {
            this.index = index;
            this.type = type;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public int compareTo(CharSequence charSequence) {
            return getName().compareTo(charSequence.toString());
        }

        @Override
        public Set<? extends Annotation> getAnnotations() {
            return Collections.emptySet();
        }

        @Override
        public String getName() {
            return "param" + index;
        }

        @Override
        public String getSignature() {
            return "";
        }

        @Override
        public int length() {
            return getName().length();
        }

        @Override
        public char charAt(int index) {
            return getName().charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return getName().subSequence(start, end);
        }

        @Override
        public void validateReference() {

        }
    }
}
