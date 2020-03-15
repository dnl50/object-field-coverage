package de.adesso.objectfieldcoverage.api.assertion.primitive.bool;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BooleanTypeAssertion extends PrimitiveTypeAssertion<Boolean> {

    private final CtExpression<Boolean> assertedBooleanExpression;

    @Override
    public PrimitiveType getAssertedPrimitiveType() {
        return () -> "boolean";
    }

    @Override
    public CtExpression<Boolean> getAssertedExpression() {
        return assertedBooleanExpression;
    }

    @Override
    public Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation) {
        return Fraction.ONE;
    }

}
