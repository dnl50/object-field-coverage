package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.AccessibleFieldGraphBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for building {@link AssertionEvaluationInformation} instances for the evaluation of
 * {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}s. Uses result caches internally
 * to prevent redundant analysis and to speed up performance.
 *
 * @implNote This implementation is thread safe if and only if the supplied {@link EqualsMethodAnalyzer}s and
 * {@link AccessibilityAwareFieldFinder}s are thread safe.
 */
@Slf4j
public class AssertionEvaluationBuilder {

    /**
     * A map which contains entries mapping a (asserted {@link CtTypeReference}, accessing {@link CtType}) pair to a
     * set of accessible fields.
     */
    private final Map<Pair<CtTypeReference<?>, CtType<?>>, Set<AccessibleField<?>>> assertedAndAccessingTypeToAccessibleFieldMap;

    /**
     * A map which contains entries mapping a (asserted {@link CtTypeReference}, accessing {@link CtType}) pair to the
     * resulting {@link AssertionEvaluationInformation} instance.
     */
    private final Map<Pair<CtTypeReference<?>, CtType<?>>, AssertionEvaluationInformation> resultCache;

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

    public AssertionEvaluationBuilder(List<AccessibilityAwareFieldFinder> fieldFinders, List<EqualsMethodAnalyzer> equalsMethodAnalyzers) {
        this.assertedAndAccessingTypeToAccessibleFieldMap = new ConcurrentHashMap<>();
        this.resultCache = new ConcurrentHashMap<>();

        this.fieldFinders = fieldFinders;
        this.equalsMethodAnalyzers = equalsMethodAnalyzers;
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

        if(assertedTypeRef.isPrimitive()) {
            var primitiveType = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedTypeRef.getSimpleName());
            return new AssertionEvaluationInformation(primitiveType);
        }

        var assertedType = assertedTypeRef.getTypeDeclaration();
        var accessingType = assertion.getAssertedExpression()
                .getParent(CtType.class);

        var accessibleFieldGraph = AccessibleFieldGraphBuilder.buildGraph(fieldFinders, accessingType, assertedType);
//        buildEqualsGraph(accessibleFieldGraph, assertion);
        return null;
    }


//    private AccessibleFieldGraph buildEqualsGraph(AccessibleFieldGraph baseGraph, AbstractAssertion<?> assertion) {
//        var superClassesIncludingClass = TypeUtil.findExplicitSuperClassesIncludingClass(assertedType);
//        var accessingType = assertion.getAssertedExpression()
//                .getParent(CtType.class);
//
//        var aggregatingFieldFinder = new AggregatingAccessibilityAwareFieldFinder(fieldFinders);
//
//        Map<CtType<?>, Set<AccessibleField<?>>> accessibleFieldsInSuperTypes = superClassesIncludingClass.stream()
//                .collect(Collectors.toMap(Function.identity(), type -> Set.copyOf(aggregatingFieldFinder.findAccessibleFields(type, type))));
//        var accessibleFields = Set.copyOf(aggregatingFieldFinder.findAccessibleFields(accessingType, assertedType));
//
//        var accessibleFieldsUsedInEquals = new IterativeEqualsMethodAnalyzer(equalsMethodAnalyzers)
//                .findAccessibleFieldsUsedInEquals(assertedType, accessibleFields, accessibleFieldsInSuperTypes);
//
//        return null;
//    }

}
