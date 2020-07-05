package de.adesso.objectfieldcoverage.api.evaluation;

import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * A data class for which contains information about the asserted type of an {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}.
 */
@Data
@Builder
@RequiredArgsConstructor
public class AssertionEvaluationInformation {

    /**
     * A reference to the type which the assertion asserts. Uses a type reference since
     * the asserted type might not be part of the spoon model (e.g. the asserted type
     * is a {@link String} or primitive {@code boolean} value).
     */
    private final CtTypeReference<?> assertedTypeReference;

    /**
     * The {@link AccessibleFieldGraph} containing all fields which are accessible from the test
     * method which contains the {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}.
     */
    private final AccessibleFieldGraph accessibleFieldsGraph;

    /**
     * The {@link AccessibleFieldGraph} containing all fields which are accessible from the test
     * method which contains the {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}
     * <b>and</b> which are compared in the equals method of the declaring type. A subgraph
     * of the {@link #getAccessibleFieldsGraph() accessible fields graph}.
     */
    private final AccessibleFieldGraph accessibleFieldsUsedInEqualsGraph;

    /**
     * A set containing {@link Path paths} in the {@link #getAccessibleFieldsGraph() accessible fields
     * graph} which indicate that a field is accessible, but not asserted in the equals method of
     * the types which lead to that node. All paths in this set end at the first node which is not
     * compared in the equals method, since paths may be infinitely long otherwise in case the graph contains
     * a circle.
     */
    private final Set<Path> pathsOfFieldsNotUsedInEquals;

    /**
     *
     * @return
     *          {@code true}, when the {@link #getPathsOfFieldsNotUsedInEquals() path set} for fields
     *          not used in equals is empty. {@code false} is returned otherwise.
     */
    public boolean allAccessibleFieldsUsedInEquals() {
        return pathsOfFieldsNotUsedInEquals.isEmpty();
    }

}
