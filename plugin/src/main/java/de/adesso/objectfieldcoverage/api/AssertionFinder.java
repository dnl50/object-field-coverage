package de.adesso.objectfieldcoverage.api;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * A functional interface abstraction whose implementations are used to find the
 * {@link AbstractAssertion}s in a given {@link CtMethod test method}. All implementations
 * must declare a <b>public no-arg</b> constructor.
 */
@FunctionalInterface
public interface AssertionFinder {

    /**
     *
     * @param testMethod
     *          The {@link CtMethod test method} to find assertions in, not {@code null}.
     *
     * @return
     *          A list containing all assertions that are present in the given {@code testMethod}.
     *          Cannot be {@code null}.
     */
    List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod);

}
