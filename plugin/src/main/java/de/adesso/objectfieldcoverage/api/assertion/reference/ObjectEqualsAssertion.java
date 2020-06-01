package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;

public class ObjectEqualsAssertion<T> extends ClassTypeAssertion<T> {

    public ObjectEqualsAssertion(CtExpression<T> assertedExpression) {
        super(assertedExpression);
    }

    @Override
    public Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation) {
        return null;
    }

}
