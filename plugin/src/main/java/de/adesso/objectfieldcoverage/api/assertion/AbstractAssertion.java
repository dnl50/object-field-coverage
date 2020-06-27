package de.adesso.objectfieldcoverage.api.assertion;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

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
     *          The {@link CtMethod} {@code this} assertion originates from, not {@code null}.
     */
    CtMethod<?> getOriginTestMethod();

    /**
     *
     * @param evaluationInformation
     *          The {@link AssertionEvaluationInformation} that contains additional information
     *          about the type of the {@link #getAssertedExpression() asserted expression}. Must be a
     *          an instance for a {@link Throwable} in case the {@link #expressionRaisesThrowable()} method
     *          returns true. A runtime exception might be thrown otherwise.
     *
     * @return
     *          A set containing the {@link Path}s of the {@link AssertionEvaluationInformation#getAccessibleFieldsGraph()
     *          accessible field graph} which are covered by {@code this} assertion. Might be empty in case no paths are
     *          covered.
     */
    Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation);

    /**
     *
     * @return
     *          {@code true}, if the expression {@code this} instance is expected to raise a throwable
     *          when evaluating. {@code false} is returned otherwise.
     */
    default boolean expressionRaisesThrowable() {
        return false;
    }

}
