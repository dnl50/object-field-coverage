package de.adesso.objectfieldcoverage.api;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Functional interface abstraction to analyze whether a given {@link CtInvocation} is expected to raise
 * a {@link Throwable}.
 */
@FunctionalInterface
public interface InvocationThrowableAnalyzer {

    /**
     *
     * @param invocation
     *          The {@link CtInvocation} which should be checked, not {@code null}.
     *
     * @param testMethod
     *          The test method the invocation originated from, not {@code null}.
     *
     * @param helperMethods
     *          A list of helper methods which are invoked inside the given {@code testMethod}, not {@code null}.
     *
     * @return
     *          {@code true}, if the invocation is expected to raise a {@link Throwable}. {@code false} is returned
     *          otherwise.
     */
    boolean isExpectedToRaiseThrowable(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods);

}
