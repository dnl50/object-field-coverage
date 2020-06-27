package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

//TODO: JavaDoc

/**
 * {@link JUnitAssertionInvocationHandler} implementation for {@code assertTrue} / {@code assertFalse}
 * method invocations with primitive boolean arguments.
 */
@Slf4j
@NoArgsConstructor
public class AssertTrueFalseInvocationHandler implements JUnitAssertionInvocationHandler {

    /**
     * The {@link CtTypeReference} of a primitive boolean.
     */
    private static final CtTypeReference<Boolean> BOOLEAN_PRIMITIVE_TYPE_REF = new TypeFactory().BOOLEAN_PRIMITIVE;

    /**
     * The simple name of the {@link org.junit.Assert#assertTrue(boolean)} and
     * {@link org.junit.jupiter.api.Assertions#assertTrue(boolean)} method.
     */
    private static final String ASSERT_TRUE_SIMPLE_NAME = "assertTrue";

    /**
     * The simple name of the {@link org.junit.Assert#assertTrue(boolean)} and
     * {@link org.junit.jupiter.api.Assertions#assertFalse(boolean)} method.
     */
    private static final String ASSERT_FALSE_SIMPLE_NAME = "assertFalse";

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@code true}, if the simple method name is equal to {@value ASSERT_TRUE_SIMPLE_NAME} or
     *          {@value ASSERT_FALSE_SIMPLE_NAME} and the asserted expression is a primitive boolean expression.
     *          {@code false} is returned otherwise.
     */
    @Override
    public boolean supports(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        var methodSimpleName = staticAssertInvocation.getExecutable().getSimpleName();

        return (ASSERT_TRUE_SIMPLE_NAME.equals(methodSimpleName) || ASSERT_FALSE_SIMPLE_NAME.equals(methodSimpleName))
                && isPrimitiveTypeCall(staticAssertInvocation, junitVersion);
    }

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
     *
     */
    @Override
    public AbstractAssertion<?> getAssertion(CtInvocation<?> staticAssertInvocation, CtMethod<?> testMethod, JUnitVersion junitVersion) {
        var assertedBooleanExpression = getBooleanExpression(staticAssertInvocation, junitVersion);

        return new BooleanTypeAssertion(assertedBooleanExpression, testMethod);
    }

    private boolean isPrimitiveTypeCall(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        switch (junitVersion) {
            case FOUR:
                return true;
            case FIVE:
                var parameters = staticAssertInvocation.getExecutable().getParameters();

                if(parameters.size() > 0) {
                    var firstParameter = parameters.get(0);
                    return BOOLEAN_PRIMITIVE_TYPE_REF.equals(firstParameter);
                }

                return false;
            default:
                throw new IllegalStateException(String.format("Unsupported JUnit Version '%s'!", junitVersion));
        }
    }

    @SuppressWarnings("unchecked")
    private CtExpression<Boolean> getBooleanExpression(CtInvocation<?> staticAssertInvocation, JUnitVersion junitVersion) {
        switch (junitVersion) {
            case FOUR:
                var arguments = staticAssertInvocation.getArguments();
                if(arguments.size() == 1) {
                    return (CtExpression<Boolean>) arguments.get(0);
                } else {
                    return (CtExpression<Boolean>) arguments.get(1);
                }
            case FIVE:
                return (CtExpression<Boolean>) staticAssertInvocation.getArguments().get(0);
            default:
                throw new IllegalStateException(String.format("Unsupported JUnit Version '%s'!", junitVersion));
        }
    }

}
