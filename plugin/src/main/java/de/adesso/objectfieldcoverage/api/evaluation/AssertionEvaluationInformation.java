package de.adesso.objectfieldcoverage.api.evaluation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.declaration.CtType;

import java.util.List;

/**
 * A data class for which contains information about the asserted type.
 */
@Data
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    /**
     * The type which the assertion asserts.
     */
    private final CtType<?> assertedType;

    /**
     * The {@link AssertedField fields} which are accessible on the given {@code assertedType}.
     */
    private final List<AssertedField<?>> accessibleFields;

    /**
     * The {@link AssertedField fields} which are compared inside the {@code equals} method
     * of the given {@code assertedType}.
     */
    private final List<AssertedField<?>> accessibleFieldsUsedInEquals;

}
