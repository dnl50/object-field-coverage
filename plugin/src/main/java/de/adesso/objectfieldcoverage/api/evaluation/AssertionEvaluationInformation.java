package de.adesso.objectfieldcoverage.api.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
     * is a {@link String}).
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
     * the types which lead to that node. Each path contained in this set leads to a leaf
     * {@link de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode node} of the
     * {@link #getAccessibleFieldsGraph() accessible fields graph}. This set is not allowed to contain
     * any empty paths.
     */
    private final Set<Path> pathsOfFieldsNotUsedInEquals;

    /**
     * Initializes the {@link #getAccessibleFieldsGraph() accessible field graph} and
     * the {@link #getAccessibleFieldsUsedInEqualsGraph() accessible field used in equals graph} with an
     * {@link AccessibleFieldGraph#EMPTY_GRAPH empty} graph and the {@link #getPathsOfFieldsNotUsedInEquals() path}
     * set with an <b>unmodifiable</b> and empty set.
     *
     * @param assertedTypeReference
     *          The {@link CtTypeReference} the newly constructed instance belongs to, not
     *          {@code null}.
     */
    public AssertionEvaluationInformation(CtTypeReference<?> assertedTypeReference) {
        this.assertedTypeReference = assertedTypeReference;

        this.accessibleFieldsGraph = AccessibleFieldGraph.EMPTY_GRAPH;
        this.accessibleFieldsUsedInEqualsGraph = AccessibleFieldGraph.EMPTY_GRAPH;
        this.pathsOfFieldsNotUsedInEquals = Set.of();
    }

    /**
     *
     * @return
     *          {@code true}, when the {@link #getPathsOfFieldsNotUsedInEquals() path set} for fields
     *          not used in equals is empty. {@code false} is returned otherwise.
     */
    public boolean allAccessibleFieldsUsedInEquals() {
        return pathsOfFieldsNotUsedInEquals.isEmpty();
    }

    /**
     * Takes the {@link AccessibleField} from the last {@link de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode node}
     * in each {@link #getPathsOfFieldsNotUsedInEquals() path}.
     *
     * @return
     *          A set containing the last node of each path which leads to a field which is
     *          not compared in an equals method of the declaring type. Empty in case the
     *          {@link #allAccessibleFieldsUsedInEquals()} method returns {@code true}.
     */
    public Set<AccessibleField<?>> getAccessibleFieldsNotAsserted() {
        return pathsOfFieldsNotUsedInEquals.stream()
                .map(Path::getLast)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(AccessibleFieldGraphNode::getAccessibleField)
                .collect(Collectors.toSet());
    }

}
