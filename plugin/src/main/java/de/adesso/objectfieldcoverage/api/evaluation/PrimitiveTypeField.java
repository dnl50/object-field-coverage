package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtField;

import java.util.List;

/**
 * {@link AssertedField} implementation for primitive types and their wrapper classes. Does not
 * have any accessible child fields and can therefore be seen as a leaf inside the accessible field
 * tree.
 *
 * @param <T>
 *          The type of the field.
 */
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
