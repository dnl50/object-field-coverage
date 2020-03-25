package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtExecutable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssertionUtil {

    /**
     *
     * @param assertion
     *          The abstract assertion
     *
     * @param targetExecutable
     *
     * @return
     *          {@code true}, if the given {@code assertion}'s {@link AbstractAssertion#getAssertedExpression() asserted
     *          expression} is either a invocation of the given {@code targetExecutable} or a {@link CtVariableRead}
     *          of a variable which contains the result of the target executable.
     */
    public static boolean assertsTargetExecutableInvocationResult(AbstractAssertion<?> assertion, CtExecutable<?> targetExecutable) {
        return false;
    }

}
