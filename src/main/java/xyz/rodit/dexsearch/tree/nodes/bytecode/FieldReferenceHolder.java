package xyz.rodit.dexsearch.tree.nodes.bytecode;

import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.value.EncodedValue;

import java.util.Collections;
import java.util.Set;

public class FieldReferenceHolder implements Field {

    private final FieldReference field;

    public FieldReferenceHolder(FieldReference field) {
        this.field = field;
    }

    @Override
    public String getDefiningClass() {
        return field.getDefiningClass();
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getType() {
        return field.getType();
    }

    @Override
    public int compareTo(FieldReference fieldReference) {
        return field.compareTo(fieldReference);
    }

    @Override
    public int getAccessFlags() { return 0; }

    @Override
    public EncodedValue getInitialValue() {
        return null;
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
    public void validateReference() {

    }
}
