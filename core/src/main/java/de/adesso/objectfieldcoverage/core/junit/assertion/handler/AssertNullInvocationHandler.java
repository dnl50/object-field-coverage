package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.reference.ReferenceEqualsAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

//TODO: Test

/**
 * {@link JUnitAssertionInvocationHandler} implementation for the {@code assertNull} method.
 */
public class AssertNullInvocationHandler implements JUnitAssertionInvocationHandler {

    /**
     * The simple name of the {@link org.junit.Assert#assertEquals(long, long)} and
     * {@link org.junit.jupiter.api.Assertions#assertEquals(int, int)} method.
     */
    private static final String ASSERT_NULL_SIMPLE_NAME = "assertNull";

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@code true}, when the given executables simple name is equal to {@value ASSERT_NULL_SIMPLE_NAME}.
     *          {@code false} is returned otherwise.
     */
    @Override
    public boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        return ASSERT_NULL_SIMPLE_NAME.equals(staticAssertInvocation.getExecutable().getSimpleName());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          A {@link ReferenceEqualsAssertion} instance.
     */
    @Override
    public AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion) {
        CtExpression<?> assertedExpression;
        var arguments =  staticAssertInvocation.getArguments();

        switch (junitVersion) {
            case FOUR:
                var argCount = arguments.size();

                var targetIndex = (argCount == 2) ? 1 : 0;
                assertedExpression = arguments.get(targetIndex);
                break;

            case FIVE:
                assertedExpression = arguments.get(0);
                break;

            default:
                throw new IllegalStateException(String.format("Unsupported JUnit Version '%s'!", junitVersion));
        }

        return new ReferenceEqualsAssertion<>(assertedExpression, testMethod);
    }

}
