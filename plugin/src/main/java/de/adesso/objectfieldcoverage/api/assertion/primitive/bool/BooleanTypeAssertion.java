package de.adesso.objectfieldcoverage.api.assertion.primitive.bool;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.EqualsAndHashCode;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

@EqualsAndHashCode(callSuper = false)
public class BooleanTypeAssertion extends PrimitiveTypeAssertion<Boolean> {

    public BooleanTypeAssertion(CtExpression<Boolean> assertedExpression) {
        super(assertedExpression);
    }

    @Override
    public PrimitiveType getAssertedPrimitiveType() {
        return null;
    }

    @Override
    public CtMethod<?> getOriginTestMethod() {
        return null;
    }

    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        return null;
    }

}
