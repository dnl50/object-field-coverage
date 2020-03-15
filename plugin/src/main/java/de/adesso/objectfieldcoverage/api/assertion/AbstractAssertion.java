package de.adesso.objectfieldcoverage.api.assertion;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;

public interface AbstractAssertion<T> {

    CtExpression<T> getAssertedExpression();

    Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation);

}
