package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;

// TODO: JavaDoc
// Key Components of an Exception: Its type and its message
public class ExceptionTypeAssertion<T> extends ClassTypeAssertion<T> {

    /**
     * A boolean specifying whether the exceptions type was equals asserted.
     */
    private final boolean exceptionTypeAsserted;

    /**
     * A boolean specifying whether the exceptions message was equals asserted.
     */
    private final boolean exceptionMessageAsserted;

    public ExceptionTypeAssertion(CtExpression<T> assertedExpression, boolean exceptionTypeAsserted, boolean exceptionMessageAsserted) {
        super(assertedExpression);

        this.exceptionTypeAsserted = exceptionTypeAsserted;
        this.exceptionMessageAsserted = exceptionMessageAsserted;
    }

    @Override
    public Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation) {
        //TODO: implement
        return Fraction.ONE;
    }

}
