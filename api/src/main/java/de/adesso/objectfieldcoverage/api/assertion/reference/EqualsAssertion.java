package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

public class EqualsAssertion<T> extends ReferenceTypeAssertion<T> {

    public EqualsAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod) {
        super(assertedExpression, originTestMethod);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          All paths in the equals {@link de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph}
     *          contained in the given {@link AssertionEvaluationInformation}.
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        return evaluationInformation.getAccessibleFieldsUsedInEqualsGraph()
                .getTransitiveReachabilityPaths();
    }

}
