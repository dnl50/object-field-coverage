package de.adesso.objectfieldcoverage.core.junit.throwable;

import de.adesso.objectfieldcoverage.api.InvocationThrowableAnalyzer;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.handler.AssertThrowsInvocationHandler;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * {@link InvocationThrowableAnalyzer} implementation for JUnit's {@code assertThrows} assertion methods. Uses
 * a {@link JUnitAssertionFinder} in combination with a {@link AssertThrowsInvocationHandler}
 */
@RequiredArgsConstructor
public class AbstractJUnitInvocationThrowableAnalyzer implements InvocationThrowableAnalyzer {

    /**
     * The {@link JUnitAssertionFinder} implementation used internally to find {@code assertThrows} invocations.
     */
    private final JUnitAssertionFinder jUnitAssertionFinder;

    public AbstractJUnitInvocationThrowableAnalyzer() {
        this.jUnitAssertionFinder = new JUnitAssertionFinder(List.of(new AssertThrowsInvocationHandler()));
    }

    @Override
    public boolean isExpectedToRaiseThrowable(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        CtMethod<?> parentMethod = invocation.getParent(CtMethod.class);

        return false;
    }

}
