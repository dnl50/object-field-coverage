package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.assertion.reference.EqualsAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

//TODO: Test

/**
 * {@link JUnitAssertionInvocationHandler} implementation for the {@code assertEquals} method.
 */
public class AssertEqualsInvocationHandler implements JUnitAssertionInvocationHandler {

    /**
     * The simple name of the {@link org.junit.Assert#assertEquals(long, long)} and
     * {@link org.junit.jupiter.api.Assertions#assertEquals(int, int)} method.
     */
    private static final String ASSERT_EQUALS_SIMPLE_NAME = "assertEquals";

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@code true}, if the simple name of the invoked executable is equal to {@value ASSERT_EQUALS_SIMPLE_NAME}.
     *          {@code false} is returned otherwise.
     */
    @Override
    public boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        return ASSERT_EQUALS_SIMPLE_NAME.equals(staticAssertInvocation.getExecutable().getSimpleName());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          A {@link PrimitiveTypeAssertion} or a {@link EqualsAssertion} depending on the type
     *          of the asserted expression.
     */
    @Override
    public AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion) {
        CtExpression<?> assertedExpression;
        var arguments =  staticAssertInvocation.getArguments();

        switch (junitVersion) {
            case FOUR:
                var argCount = arguments.size();

                var targetIndex = (argCount == 3) ? 2 : 1;
                assertedExpression = arguments.get(targetIndex);
                break;

            case FIVE:
                assertedExpression = arguments.get(1);
                break;

            default:
                throw new IllegalStateException(String.format("Unsupported JUnit Version '%s'!", junitVersion));
        }

        if(PrimitiveTypeUtils.isPrimitiveOrWrapperType(assertedExpression.getType())) {
            return new PrimitiveTypeAssertion<>(assertedExpression, testMethod, false);
        } else {
            return new EqualsAssertion<>(assertedExpression, testMethod);
        }
    }

}
