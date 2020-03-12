package de.adesso.objectfieldcoverage.api.assertion;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtVariableRead;

import java.util.Optional;

public interface AbstractAssertion<T> {

    Optional<CtVariableRead<T>> getAssertedVariableRead();

    Optional<CtAbstractInvocation<T>> getAssertedAbstractInvocation();

    Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation);

}
