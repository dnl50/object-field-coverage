package de.adesso.objectfieldcoverage.api.assertion;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

/**
 * The base interface abstraction for an <i>Assertion</i>.
 *
 * @param <T>
 *          The type of the asserted object.
 */
public interface AbstractAssertion<T> {

    /**
     *
     * @return
     *          The {@link CtExpression} which {@code this} assertion asserts, not {@code null}.
     */
    CtExpression<T> getAssertedExpression();

    /**
     * It might be important to have additional information of the test method from which this assertion
     * <i>originated</i> from. This is the {@link CtMethod} which lead to {@code this} assertion being made.
     * Can differ from the parent method of the {@link #getAssertedExpression() asserted expression} in case the
     * method containing this assertion is located in a different class.
     *
     * @return
     *          The {@link CtType} of the original test class from {@code this} assertion originates
     *          from, not {@code null}.
     */
    //TODO: make abstract
    default CtMethod<?> getOriginTestMethod() {
        return null;
    }

    /**
     *
     * @param evaluationInformation
     *          The {@link AssertionEvaluationInformation} that contains additional information
     *          about the type of the {@link #getAssertedExpression() asserted expression}.
     *
     * @return
     *          A {@link Fraction} in the interval {@code [0,1]}, not {@code null}.
     */
    Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation);

}
