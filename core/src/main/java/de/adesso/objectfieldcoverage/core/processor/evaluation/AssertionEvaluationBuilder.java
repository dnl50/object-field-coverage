package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.AccessibleFieldGraphBuilder;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.ComparedInEqualsMethodBiPredicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * Utility class for building {@link AssertionEvaluationInformation} instances for the evaluation of
 * {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}s. Uses result caches internally
 * to prevent redundant analysis and to increase performance.
 *
 * @implNote This implementation is thread safe if and only if the supplied {@link EqualsMethodAnalyzer}s and
 * {@link AccessibilityAwareFieldFinder}s are thread safe.
 *
 * @see ComparedInEqualsMethodBiPredicate
 */
@Slf4j
@AllArgsConstructor
public class AssertionEvaluationBuilder {

    /**
     * A map which contains entries mapping a (asserted {@link CtTypeReference}, accessing {@link CtType}) pair to the
     * resulting {@link AssertionEvaluationInformation} instance.
     */
    private final Map<Pair<CtTypeReference<?>, CtType<?>>, AssertionEvaluationInformation> resultCache
            = new ConcurrentHashMap<>();

    /**
     * A list containing the {@link AccessibilityAwareFieldFinder}s to build the {@link AccessibleFieldGraph}s
     * of all accessible fields with.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * A list containing the {@link EqualsMethodAnalyzer}s which are used to build the {@link AccessibleFieldGraph}s
     * of accessible fields which are compared in the equals method of the asserted type.
     */
    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    /**
     * Supplier-like abstraction for simpler unit testing.
     */
    private final BiFunction<List<AccessibilityAwareFieldFinder>, CtType<?>, AccessibleFieldGraphBuilder> graphBuilderSupplier;

    /**
     *
     * @param fieldFinders
     *          A list containing the {@link AccessibilityAwareFieldFinder}s to build the {@link AccessibleFieldGraph}s
     *          of all accessible fields with, not {@code null}.
     *
     * @param equalsMethodAnalyzers
     *          A list containing the {@link EqualsMethodAnalyzer}s which are used to build the {@link AccessibleFieldGraph}s
     *          of accessible fields which are compared in the equals method of the asserted type, not {@code null}.
     */
    public AssertionEvaluationBuilder(List<AccessibilityAwareFieldFinder> fieldFinders, List<EqualsMethodAnalyzer> equalsMethodAnalyzers) {
        this.fieldFinders = fieldFinders;
        this.equalsMethodAnalyzers = equalsMethodAnalyzers;
        this.graphBuilderSupplier = AccessibleFieldGraphBuilder::new;
    }

    /**
     *
     *
     * @param assertion
     *          The {@link AbstractAssertion} for which the {@link AssertionEvaluationInformation} should be built,
     *          not {@code null}.
     *
     * @return
     *          The {@link AssertionEvaluationInformation} for the given {@code assertion}.
     *
     * @see #build(CtTypeReference, CtType)
     */
    public AssertionEvaluationInformation build(AbstractAssertion<?> assertion) {
        Objects.requireNonNull(assertion, "The given abstract assertion cannot be null!");

        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();
        var accessingType = assertion.getAssertedExpression()
                .getParent(CtType.class);

        return build(assertedTypeRef, accessingType);
    }

    /**
     *
     * @param assertedTypeRef
     *          The {@link CtTypeReference} of the type which is asserted, not {@code null}.
     *
     * @param accessingType
     *          The {@link CtType} which accesses fields in the given {@code assertedTypeRef}'s type, not
     *          {@code null}.
     *
     * @return
     *          The {@link AssertionEvaluationInformation} instance for the given types.
     */
    public AssertionEvaluationInformation build(CtTypeReference<?> assertedTypeRef, CtType<?> accessingType) {
        var cacheKey = Pair.of(assertedTypeRef, accessingType);

        if(resultCache.containsKey(cacheKey)) {
            log.debug("[Cache Hit] Cache contained entry for (asserted type '{}' | accessing type '{}') pair!",
                    assertedTypeRef.getQualifiedName(), accessingType.getQualifiedName());
            return resultCache.get(cacheKey);
        }

        return buildAndCacheResult(assertedTypeRef, accessingType);
    }

    /**
     * This method returns an <i>empty</i> {@link AssertionEvaluationInformation} in case the
     * {@link CtTypeReference#isPrimitive()} method of the given {@code assertedTypeRef} returns {@code true},
     * since primitive types cannot declare members.
     *
     * @param assertedTypeRef
     *          The {@link CtTypeReference} of the asserted type, not {@code null}.
     *
     * @param accessingType
     *          The {@link CtType} which accesses the fields in the given {@code assertedTypeRef}, not {@code null}.
     *
     * @return
     *          The cached {@link AssertionEvaluationInformation} instance.
     */
    private AssertionEvaluationInformation buildAndCacheResult(CtTypeReference<?> assertedTypeRef, CtType<?> accessingType) {
        AssertionEvaluationInformation result;

        if(assertedTypeRef.isPrimitive()) {
            var primitiveType = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedTypeRef.getSimpleName());
            var emptyGraph = AccessibleFieldGraph.empty(primitiveType, accessingType.getReference());
            result = new AssertionEvaluationInformation(assertedTypeRef, emptyGraph, emptyGraph, Set.of());
        } else {
            var assertedType = assertedTypeRef.getTypeDeclaration();

            var graphBuilder = graphBuilderSupplier.apply(fieldFinders, accessingType);

            var accessibleFieldGraph = graphBuilder.buildGraph(assertedType);
            var accessibleFieldsUsedInEqualsGraph = graphBuilder.buildGraph(assertedType,
                    new ComparedInEqualsMethodBiPredicate(equalsMethodAnalyzers, fieldFinders));
            var pathsOfFieldsNotComparedInEquals = findPathsOfFieldsNotComparedInEquals(accessibleFieldGraph,
                    accessibleFieldsUsedInEqualsGraph);

            if(pathsOfFieldsNotComparedInEquals.isEmpty()) {
                log.info("All {} accessible fields are compared in the equals method!",
                        accessibleFieldGraph.getAllNodes().size());
            } else {
                log.info("{} accessible fields and their accessible child fields (if present) are not compared in the equals method!",
                        pathsOfFieldsNotComparedInEquals.size());
            }

            result = new AssertionEvaluationInformation(assertedTypeRef, accessibleFieldGraph,
                    accessibleFieldsUsedInEqualsGraph, pathsOfFieldsNotComparedInEquals);
        }

        return cacheResult(result, assertedTypeRef, accessingType);
    }

    /**
     * Since {@code usedInEqualsGraph} ({@code B}) <b>must</b> be a subgraph of {@code accessibleFieldGraph} ({@code A}),
     * a node {@code a} in {@code A} is equal to a node {@code b} in {@code B}, if the contained {@link AccessibleField}s
     * are equal and the parent nodes are equal.
     *
     * @param accessibleFieldGraph
     *          The {@link AccessibleFieldGraph} containing all accessible fields, not {@code null}.
     *
     * @param usedInEqualsGraph
     *          The {@link AccessibleFieldGraph} containing all accessible fields which are also compared
     *          in the equals method of the {@link AccessibleFieldGraph#getDescribedTypeRef()}, not {@code null}.
     *          <b>Must</b> be a subgraph of the given {@code accessibleFieldGraph}.
     *
     * @return
     *          A set containing all paths which are present in the given {@code accessibleFieldGraph}, but not in
     *          the given {@code usedInEqualsGraph}. A path is terminated as soon as it contains a loop.
     */
    private Set<Path> findPathsOfFieldsNotComparedInEquals(AccessibleFieldGraph accessibleFieldGraph, AccessibleFieldGraph usedInEqualsGraph) {
        if(accessibleFieldGraph.equals(usedInEqualsGraph)) {
            return Set.of();
        }

        // Path in accessibleFieldGraph -> node of last path element in usedInEqualsGraph
        var pathsContainedInEqualsGraph = new LinkedList<Pair<Path, AccessibleFieldGraphNode>>();
        var pathsOfFieldsNotComparedInEquals = new HashSet<Path>();

        accessibleFieldGraph.getRootNodes().forEach(rootNode -> {
            var equivalentNodeInEqualsGraph = findNodeWithAccessibleField(usedInEqualsGraph.getRootNodes(),
                    rootNode.getAccessibleField());

            if(equivalentNodeInEqualsGraph.isPresent()) {
                pathsContainedInEqualsGraph.add(Pair.of(new Path(rootNode), equivalentNodeInEqualsGraph.get()));
            } else {
                pathsOfFieldsNotComparedInEquals.add(new Path(rootNode));
            }
        });

        while(!pathsContainedInEqualsGraph.isEmpty()) {
            var currentPathNodePair = pathsContainedInEqualsGraph.removeFirst();
            var currentPath = currentPathNodePair.getLeft();
            var lastNodeInPath = currentPath.getLast().get();
            var equalsGraphLastNodeRepresentation = currentPathNodePair.getRight();

            for(var childNode : lastNodeInPath.getChildren()) {
                var extendedPath = new Path(currentPath).append(childNode);
                var childNodeEqualsGraphRepresentation = findNodeWithAccessibleField(equalsGraphLastNodeRepresentation.getChildren(),
                        childNode.getAccessibleField());

                if(childNodeEqualsGraphRepresentation.isEmpty()) {
                    pathsOfFieldsNotComparedInEquals.add(extendedPath);
                } else if(!extendedPath.containsLoop()) {
                    // prevent infinite loops
                    pathsContainedInEqualsGraph.addFirst(Pair.of(extendedPath, childNodeEqualsGraphRepresentation.get()));
                }
            }
        }

        return pathsOfFieldsNotComparedInEquals;
    }

    /**
     *
     * @param nodes
     *          The nodes to find a node with the given {@code accessibleField} in, not {@code null}.
     *
     * @param accessibleField
     *          The accessible field to find the corresponding node for, not {@code null}.
     *
     * @return
     *          An optional containing a node whose {@link AccessibleFieldGraphNode#getAccessibleField() accessible field}
     *          is equal to the given {@code accessibleField}. An empty optional is returned otherwise.
     */
    private Optional<AccessibleFieldGraphNode> findNodeWithAccessibleField(Set<AccessibleFieldGraphNode> nodes, AccessibleField<?> accessibleField) {
        return nodes.stream()
                .filter(node -> node.getAccessibleField().equals(accessibleField))
                .findFirst();
    }

    /**
     *
     * @param result
     *          The {@link AssertionEvaluationInformation} which was built by the {@link #buildAndCacheResult(CtTypeReference, CtType)}
     *          with the given {@code assertedTypeRef} and {@code accessingType}, not {@code null}.
     *
     * @param assertedTypeRef
     *          The {@link CtTypeReference} of the asserted type, not {@code null}.
     *
     * @param accessingType
     *          The {@link CtType} which accesses the fields in the given {@code assertedTypeRef}, not {@code null}.
     *
     * @return
     *          The given {@code result}.
     */
    private AssertionEvaluationInformation cacheResult(AssertionEvaluationInformation result, CtTypeReference<?> assertedTypeRef,
                                                       CtType<?> accessingType) {
        resultCache.put(Pair.of(assertedTypeRef, accessingType), result);
        return result;
    }

}
