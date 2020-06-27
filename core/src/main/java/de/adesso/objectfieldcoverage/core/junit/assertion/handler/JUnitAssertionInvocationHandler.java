package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

/**
 * Interface abstraction for handlers of different invocations of static methods declared in
 * {@link org.junit.Assert} and {@link org.junit.jupiter.api.Assertions}.
 */
public interface JUnitAssertionInvocationHandler {

    /**
     *
     * @param staticAssertInvocation
     *          An invocation of a static method declared in a JUnit assertion utility class, not {@code null}.
     *
     * @param junitVersion
     *          The JUnit version the invoked method is declared in, not {@code null}.
     *
     * @return
     *          {@code true}, if this implementation supports the invocation.
     */
    boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion);

    /**
     *
     * @param staticAssertInvocation
     *          An invocation of a static method declared in a JUnit assertion utility class, not {@code null}.
     *          {@link #supports(CtInvocation, JUnitVersion)} must return {@code true}.
     *
     * @param testMethod
     *          The test method the given invocation originates from, not {@code null}.
     *
     * @param junitVersion
     *          The JUnit version the invoked method is declared in, not {@code null}.
     *
     * @return
     *          The abstract assertion resulting from the given invocation.
     */
    AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion);

}
