package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

/**
 * {@link ReferenceTypeAssertion} for for reference type expressions which are
 * compared using the {@code ==} operator.
 *
 * @param <T>
 *          The return type of the asserted expression.
 */
public class ReferenceEqualsAssertion<T> extends ReferenceTypeAssertion<T> {

    /**
     *
     * @param assertedExpression
     *          The {@link CtExpression} which {@code this} assertion asserts, not {@code null}. Must be the actual
     *          invocation or field access on the target instance.
     *
     * @param originTestMethod
     *          The {@link CtMethod} {@code this} assertion originates from, not {@code null}.
     */
    public ReferenceEqualsAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod) {
        super(assertedExpression, originTestMethod);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          All paths in the {@link de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph} contained
     *          in the given {@link AssertionEvaluationInformation}.
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        return evaluationInformation.getAccessibleFieldsGraph()
                .getTransitiveReachabilityPaths();
    }

}
