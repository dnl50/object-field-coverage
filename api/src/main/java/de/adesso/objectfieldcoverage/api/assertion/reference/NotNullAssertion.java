package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

//TODO: Test

/**
 * {@link ReferenceTypeAssertion} implementation for {@code != null} comparisons of Object references.
 *
 * @param <T>
 *          The type of the asserted expression.
 */
public class NotNullAssertion<T> extends ReferenceTypeAssertion<T> {

    /**
     *
     * @param assertedExpression
     *          The {@link CtExpression} which {@code this} assertion asserts, not {@code null}. Must be the actual
     *          invocation or field access on the target instance.
     *
     * @param originTestMethod
     *          The {@link CtMethod} {@code this} assertion originates from, not {@code null}.
     */
    public NotNullAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod) {
        super(assertedExpression, originTestMethod);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          An empty set.
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        return Set.of();
    }

}
