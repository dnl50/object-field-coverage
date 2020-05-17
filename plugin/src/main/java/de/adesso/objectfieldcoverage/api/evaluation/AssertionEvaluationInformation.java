package de.adesso.objectfieldcoverage.api.evaluation;

import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.reference.CtTypeReference;

/**
 * A data class for which contains information about the asserted type.
 */
@Data
@Builder
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    /**
     * A reference to the type which the assertion asserts. Uses a type reference since
     * the asserted type might not be part of the spoon model.
     */
    private final CtTypeReference<?> assertedTypeReference;

    private final AccessibleFieldGraph accessibleFields;

    private final AccessibleFieldGraph accessibleFieldsUsedInEquals;

}
