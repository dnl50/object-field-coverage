package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
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
     * @param assertion
     *          The {@link AbstractAssertion} for which the {@link AssertionEvaluationInformation} should be built,
     *          not {@code null}.
     *
     * @return
     *          The {@link AssertionEvaluationInformation} for the given {@code assertion}.
     */
    public AssertionEvaluationInformation build(AbstractAssertion<?> assertion) {
        Objects.requireNonNull(assertion, "The given abstract assertion cannot be null!");

        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();
        var accessingType = assertion.getOriginTestMethod()
                .getParent(CtType.class);

        return buildAndCacheResult(assertedTypeRef, accessingType);
    }

    /**
     *
     * @param accessingType
     *          The {@link CtType} which accesses the fields of the given {@code typeRef}, not {@code null}.
     *
     * @param typeRef
     *          The reference of the type which is accessed by the given {@code accessingType}, not {@code null}.
     *
     * @return
     *          The {@link AssertionEvaluationInformation} for the given {@code accessingType} and {@code typeRef}.
     */
    public AssertionEvaluationInformation build(CtType<?> accessingType, CtTypeReference<?> typeRef) {
        return buildAndCacheResult(typeRef, accessingType);
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
        var cacheKey = Pair.of(assertedTypeRef, accessingType);

        if(resultCache.containsKey(cacheKey)) {
            log.debug("[Cache Hit] Cache contained entry for (asserted type '{}' | accessing type '{}') pair!",
                    assertedTypeRef.getQualifiedName(), accessingType.getQualifiedName());
            return resultCache.get(cacheKey);
        }

        var graphBuilder = graphBuilderSupplier.apply(fieldFinders, accessingType);

        var accessibleFieldGraph = graphBuilder.buildGraph(assertedTypeRef);
        var accessibleFieldsUsedInEqualsGraph = graphBuilder.buildGraph(assertedTypeRef,
                new ComparedInEqualsMethodBiPredicate(equalsMethodAnalyzers, fieldFinders));
        var pathsOfFieldsNotComparedInEquals = findPathsOfFieldsNotComparedInEquals(accessibleFieldGraph,
                accessibleFieldsUsedInEqualsGraph);

        if(pathsOfFieldsNotComparedInEquals.isEmpty()) {
            log.info("All {} accessible fields of '{}' are compared in the equals method!",
                    accessibleFieldGraph.getAllNodes().size(), assertedTypeRef.getQualifiedName());
        } else {
            log.info("{} accessible fields of '{}' and their accessible child fields (if present) are not compared in the equals method!",
                    assertedTypeRef.getQualifiedName(), pathsOfFieldsNotComparedInEquals.size());
        }

        var resultIngEvaluationInformation = new AssertionEvaluationInformation(assertedTypeRef, accessibleFieldGraph,
                accessibleFieldsUsedInEqualsGraph, pathsOfFieldsNotComparedInEquals);

        return cacheResult(resultIngEvaluationInformation, assertedTypeRef, accessingType);
}

    /**
     * Since {@code usedInEqualsGraph} ({@code B}) <b>must</b> be a subgraph of {@code accessibleFieldGraph} ({@code A}),
     * the paths returned by {@link AccessibleFieldGraph#getTransitiveReachabilityPaths()} of {@code B} must also be
     * a subset of the paths returned by {@code A}.
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
     *          the given {@code usedInEqualsGraph}.
     *
     * @see AccessibleFieldGraph#getTransitiveReachabilityPaths()
     */
    private Set<Path> findPathsOfFieldsNotComparedInEquals(AccessibleFieldGraph accessibleFieldGraph, AccessibleFieldGraph usedInEqualsGraph) {
        if(accessibleFieldGraph.equals(usedInEqualsGraph)) {
            return Set.of();
        }

        var pathsCoveredByEquals = usedInEqualsGraph.getTransitiveReachabilityPaths();
        var allPaths = new HashSet<>(accessibleFieldGraph.getTransitiveReachabilityPaths());

        allPaths.removeAll(pathsCoveredByEquals);
        return allPaths;
    }

    /**
     * Puts the given {@link AssertionEvaluationInformation} result in the {@link #resultCache} Map overriding
     * any previous value.
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
