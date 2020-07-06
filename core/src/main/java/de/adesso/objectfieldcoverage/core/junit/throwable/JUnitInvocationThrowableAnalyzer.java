package de.adesso.objectfieldcoverage.core.junit.throwable;

import de.adesso.objectfieldcoverage.api.InvocationThrowableAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.handler.AssertThrowsInvocationHandler;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

//TODO: Test

/**
 * {@link InvocationThrowableAnalyzer} implementation for JUnit's {@code assertThrows} assertion methods. Uses
 * a {@link JUnitAssertionFinder} in combination with a {@link AssertThrowsInvocationHandler}
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class JUnitInvocationThrowableAnalyzer implements InvocationThrowableAnalyzer {

    /**
     * The {@link JUnitAssertionFinder} implementation used internally to find {@code assertThrows} invocations.
     */
    private final JUnitAssertionFinder jUnitAssertionFinder;

    /**
     * The handler to filter out {@code assertThrows} calls.
     */
    private AssertThrowsInvocationHandler assertThrowsInvocationHandler = new AssertThrowsInvocationHandler();

    /**
     * No-arg constructor as required by the {@link InvocationThrowableAnalyzer} interface.
     */
    @SuppressWarnings("unused")
    public JUnitInvocationThrowableAnalyzer() {
        this.jUnitAssertionFinder = new JUnitAssertionFinder(List.of(assertThrowsInvocationHandler));
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Only takes the method the invocation takes place in into account.
     *
     * @return
     *          {@code true}, if the given {@code invocation} expression is equal to the asserted expression of an
     *          {@link AbstractAssertion} whose {@link AbstractAssertion#expressionRaisesThrowable()} method returns
     *          {@code true}. {@code false} is returned otherwise.
     */
    @Override
    public boolean isExpectedToRaiseThrowable(CtAbstractInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        CtMethod<?> parentMethod = invocation.getParent(CtMethod.class);

        return jUnitAssertionFinder.findAssertions(parentMethod, List.of()).stream()
                .filter(AbstractAssertion::expressionRaisesThrowable)
                .map(AbstractAssertion::getAssertedExpression)
                .anyMatch(invocation::equals);
    }

}
