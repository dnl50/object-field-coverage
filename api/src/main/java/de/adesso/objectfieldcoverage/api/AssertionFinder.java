package de.adesso.objectfieldcoverage.api;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * A functional interface abstraction whose implementations are used to find {@link AbstractAssertion}s in a
 * given {@link CtMethod test method} and invoked {@link CtMethod helper} methods. All implementations
 * must declare a <b>public no-arg</b> constructor.
 */
@FunctionalInterface
public interface AssertionFinder {

    /**
     * A helper method is a method which is declared in the same class or a parent class
     * of the given {@code testMethod} which is invoked inside the given {@code testMethod}.
     *
     * @param testMethod
     *          The test method to find assertions in, not {@code null}.
     *
     * @param invokedHelperMethods
     *          The helper methods which are invoked inside the given {@code testMethod}, not {@code null}.
     *
     * @return
     *          A list containing all {@link AbstractAssertion}s which are made inside the given
     *          {@code testMethod} and {@code invokedHelperMethods}.
     */
    List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod, List<CtMethod<?>> invokedHelperMethods);

}
