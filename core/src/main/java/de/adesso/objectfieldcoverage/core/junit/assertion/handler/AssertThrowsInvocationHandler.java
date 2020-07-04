package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.reference.ThrowableAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.function.Executable;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.declaration.CtMethod;

//TODO: JavaDoc, Test

public class AssertThrowsInvocationHandler implements JUnitAssertionInvocationHandler {

    /**
     * The index of the {@link Executable} passed to JUnit 5's {@link org.junit.jupiter.api.Assertions#assertThrows(Class, Executable)}
     * methods.
     */
    private static final int JUNIT5_ASSERT_THROWS_EXECUTABLE_INDEX = 1;

    /**
     * The index of the {@link ThrowingRunnable} passed to JUnit 4's {@link org.junit.Assert#assertThrows(Class, ThrowingRunnable)}
     * method.
     */
    private static final int JUNIT4_ASSERT_THROWS_RUNNABLE_INDEX = 1;

    /**
     * The index of the {@link ThrowingRunnable} passed to JUnit 4's {@link org.junit.Assert#assertThrows(String, Class, ThrowingRunnable)}
     * method.
     */
    private static final int JUNIT4_ASSERT_THROWS_RUNNABLE_INDEX_MESSAGE = 2;

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
     *          {@code staticAssertInvocation} is equal to {@value ASSERT_THROWS_SIMPLE_NAME} and the method is called
     *          with a lambda expression. {@code false} is returned otherwise.
     */
    @Override
    public boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        var executableSimpleName = staticAssertInvocation.getExecutable()
                .getSimpleName();

        return ASSERT_THROWS_SIMPLE_NAME.equals(executableSimpleName)
                && isCalledWithLambda(staticAssertInvocation, junitVersion);
    }

    @Override
    public AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertThrowsInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion) {
        var lambdaIndex = getLambdaIndex(staticAssertThrowsInvocation, junitVersion);
        var assertThrowsArguments = staticAssertThrowsInvocation.getArguments();

        var throwingLambda = (CtLambda<?>) assertThrowsArguments.get(lambdaIndex);
        var assertedExpression = throwingLambda.getExpression();

        return new ThrowableAssertion<>(assertedExpression, testMethod)
                .setCoversType(true);
    }

    private boolean isCalledWithLambda(CtInvocation<?> staticAssertThrowsInvocation, JUnitVersion junitVersion) {
        var assertThrowsArguments = staticAssertThrowsInvocation.getArguments();
        var lambdaIndex = getLambdaIndex(staticAssertThrowsInvocation, junitVersion);

        return assertThrowsArguments.get(lambdaIndex) instanceof CtLambda;
    }

    private int getLambdaIndex(CtInvocation<?> staticAssertThrowsInvocation, JUnitVersion junitVersion) {
        var assertThrowsArguments = staticAssertThrowsInvocation.getArguments();

        switch (junitVersion) {
            case FOUR:
                return assertThrowsArguments.size() > 2 ?
                        JUNIT4_ASSERT_THROWS_RUNNABLE_INDEX_MESSAGE : JUNIT4_ASSERT_THROWS_RUNNABLE_INDEX;

            case FIVE:
                return JUNIT5_ASSERT_THROWS_EXECUTABLE_INDEX;

            default:
                throw new IllegalArgumentException(String.format("Unsupported JUnit version '%s'!", junitVersion));
        }
    }

}
