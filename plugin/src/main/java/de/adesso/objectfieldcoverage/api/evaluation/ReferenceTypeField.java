package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtField;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
public class ReferenceTypeField<T> implements AssertedField<T> {

    private final CtField<T> field;

    private final AssertedField<?> parent;

    private final List<AssertedField<?>> accessibleChildFields;

    @Override
    public CtField<T> getCtField() {
        return field;
    }

    @Override
    public List<AssertedField<?>> getAccessibleChildFields() {
        return Collections.unmodifiableList(accessibleChildFields);
    }

    @Override
    public AssertedField<?> getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return field.getSimpleName();
    }

}
