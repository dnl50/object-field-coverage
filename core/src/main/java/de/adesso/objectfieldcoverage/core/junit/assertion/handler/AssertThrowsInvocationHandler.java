package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.function.Executable;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

//TODO: JavaDoc, Test
public class AssertThrowsInvocationHandler implements JUnitAssertionInvocationHandler {

    /**
     * The simple name of the {@link org.junit.Assert#assertThrows(Class, ThrowingRunnable)} /
     * {@link org.junit.jupiter.api.Assertions#assertThrows(Class, Executable)} method.
     */
    private static final String ASSERT_THROWS_SIMPLE_NAME = "assertThrows";

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@code true}, if the simple name {@link spoon.reflect.declaration.CtExecutable} of the given
     *          {@code staticAssertInvocation} is equal to {@value ASSERT_THROWS_SIMPLE_NAME}. {@code false} is returned
     *          otherwise.
     */
    @Override
    public boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        var executableSimpleName = staticAssertInvocation.getExecutable()
                .getSimpleName();

        return ASSERT_THROWS_SIMPLE_NAME.equals(executableSimpleName);
    }

    @Override
    public AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion) {
        return null;
    }

}
