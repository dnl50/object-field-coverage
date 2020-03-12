package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtField;

import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
public class PrimitiveTypeField<T> implements AssertedField<T> {

    private final AssertedField<?> parent;

    private final CtField<T> field;

    @Override
    public CtField<T> getCtField() {
        return field;
    }

    @Override
    public List<AssertedField<?>> getAccessibleChildFields() {
        return List.of();
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
