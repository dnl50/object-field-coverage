package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

/**
 * A data class for which contains information about the asserted type.
 */
@Data
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    /**
     * A reference to the type which the assertion asserts.
     */
    private final CtTypeReference<?> assertedTypeReference;

    /**
     * The {@link AssertedField fields} which are accessible on the given
     * {@code assertedType}.
     * <p/>
     * Does not contain any elements when the {@link #assertedTypeIsPrimitive()} method returns
     * {@code true}.
     */
    private final List<AssertedField<?>> accessibleFields = List.of();

    /**
     * The {@link AssertedField fields} which are compared inside the {@code equals} method
     * of the given {@code assertedType}.
     * <p/>
     * Does not contain any elements when the {@link #assertedTypeIsPrimitive()} method returns
     * {@code true}.
     */
    private final List<AssertedField<?>> accessibleFieldsUsedInEquals = List.of();

    /**
     *
     * @return
     *          {@code true}, if the {@code assertedType} is a primitive type. {@code false}
     *          is returned otherwise.
     */
    public boolean assertedTypeIsPrimitive() {
        return assertedTypeReference.isPrimitive();
    }

}
