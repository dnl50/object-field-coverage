package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtExecutable;

import java.util.Objects;

//TODO: JavaDoc, Test
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssertionUtil {

    /**
     *
     * @param assertion
     *          The abstract assertion which may assert the result of a invocation of the
     *          given {@code targetExecutable}, not {@code null}.
     *
     * @param targetExecutable
     *          The executable whose invocation result may be asserted by the given {@code assertion},
     *          not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code assertion}'s {@link AbstractAssertion#getAssertedExpression() asserted
     *          expression} is either a invocation of the given {@code targetExecutable} or a {@link CtVariableRead}
     *          of a {@link CtLocalVariable local variable} which contains the result of the target executable.
     */
    public static boolean assertsTargetExecutableInvocationResult(AbstractAssertion<?> assertion, CtExecutable<?> targetExecutable) {
        Objects.requireNonNull(assertion, "assertion cannot be null!");
        Objects.requireNonNull(targetExecutable, "targetExecutable cannot be null!");

        //TODO: take local variable reads into account
        return isInvocationOfExecutable(assertion.getAssertedExpression(), targetExecutable);
    }

    /**
     * Checks whether a given {@link CtExpression expression} is a invocation of a given
     * {@link CtExecutable executable}.
     *
     * @param expression
     *          The expression which might be an invocation of the given {@code executable},
     *          not {@code null}.
     *
     * @param executable
     *          The executable, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code expression} is a {@link CtInvocation invocation} and the invocation's
     *          executable is equal to the given {@code executable}. {@code false} is returned otherwise.
     */
    private static boolean isInvocationOfExecutable(CtExpression<?> expression, CtExecutable<?> executable) {
        if(expression instanceof CtInvocation) {
            var invocation = (CtInvocation<?>) expression;

            return invocation.getExecutable().getDeclaration().equals(executable);
        }

        return false;
    }

}
