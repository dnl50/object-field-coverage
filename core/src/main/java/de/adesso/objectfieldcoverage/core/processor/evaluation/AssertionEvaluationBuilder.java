package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.AccessibleFieldGraphBuilder;
import de.adesso.objectfieldcoverage.core.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for building {@link AssertionEvaluationInformation} instances for the evaluation of
 * {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}s.
 */
@Slf4j
public class AssertionEvaluationBuilder {

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to a set of accessible
     * fields.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtType<?>>, Set<AccessibleField<?>>> accessibleFields = new HashMap<>();

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to the resulting
     * evaluation instance.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtType<?>>, AssertionEvaluationInformation> typeInformation = new HashMap<>();

    /**
     * A map which contains the evaluation information instances for primitive types. Does not need to be cleared when
     * all other result caches are cleared, since it can only contain up to 16 entries (8 primitive types + their 8
     * wrapper classes). Initialized lazily.
     */
    private final HashMap<CtTypeReference<?>, AssertionEvaluationInformation> primitiveTypeInformation = new HashMap<>(16);

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    public AssertionEvaluationBuilder(List<AccessibilityAwareFieldFinder> fieldFinders, List<EqualsMethodAnalyzer> equalsMethodAnalyzers) {
        this.fieldFinders = fieldFinders;
        this.equalsMethodAnalyzers = equalsMethodAnalyzers;
    }

    public AssertionEvaluationInformation buildEvaluationInformation(AbstractAssertion<?> assertion) {
        Objects.requireNonNull(assertion, "The given abstract assertion cannot be null!");

        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();

        if(assertedTypeRef.isPrimitive()) {
            var primitiveType = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedTypeRef.getSimpleName());
            primitiveTypeInformation.putIfAbsent(primitiveType, new AssertionEvaluationInformation(primitiveType));
            return primitiveTypeInformation.get(primitiveType);
        }

        throw new UnsupportedOperationException();
    }

    /**
     * The type in which the {@link AbstractAssertion#getAssertedExpression() asserted expression} is located in
     * us used as the accessing type of the asserted type.
     *
     * @param assertion
     *
     * @return
     */
    private AccessibleFieldGraph buildGraph(AbstractAssertion<?> assertion) {
        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();
        var assertedType = assertedTypeRef.getDeclaration();

        if(assertedType == null) {
            log.info("Type declaration for '{}' not present! Returning empty graph!",
                    assertedTypeRef.getQualifiedName());
            return AccessibleFieldGraph.EMPTY_GRAPH;
        }

        var accessingType = assertion.getAssertedExpression()
                .getParent(CtType.class);

        return AccessibleFieldGraphBuilder.buildGraph(fieldFinders, accessingType, assertedType);
    }

    private AccessibleFieldGraph buildEqualsGraph(AccessibleFieldGraph baseGraph, AbstractAssertion<?> assertion) {
        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();
        var assertedType = (CtClass<?>) assertedTypeRef.getDeclaration();

        var superClassesIncludingClass = TypeUtil.findExplicitSuperClassesIncludingClass(assertedType);

//        new IterativeEqualsMethodAnalyzer(equalsMethodAnalyzers)
//                .findAccessibleFieldsUsedInEquals();

        return null;
    }

}
