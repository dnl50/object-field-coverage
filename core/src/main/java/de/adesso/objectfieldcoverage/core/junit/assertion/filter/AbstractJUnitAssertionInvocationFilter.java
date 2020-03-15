package de.adesso.objectfieldcoverage.core.junit.assertion.filter;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Abstract {@link TypeFilter} which filters {@link CtInvocation}s of static JUnit assertion methods
 * declared in either JUnit 4's {@code Assert} or JUnit Jupiter's {@code Assertions} class.
 */
public abstract class AbstractJUnitAssertionInvocationFilter extends TypeFilter<CtInvocation<Void>> {

    /**
     * The fully qualified class name of the JUnit Jupiter {@code Assertions} class which contains the
     * static assertion methods of JUnit Jupiter.
     */
    private static final String JUNIT_JUPITER_ASSERTIONS_QUALIFIED_CLASS_NAME = "org.junit.jupiter.api.Assertions";

    /**
     * The fully qualified class name of the JUnit 4 {@code Assert} class which contains the
     * static assertion methods of JUnit 4.
     */
    private static final String JUNIT_4_ASSERT_QUALIFIED_CLASS_NAME = "org.junit.Assert";

    /**
     * no-arg default constructor for super-class initialization.
     */
    public AbstractJUnitAssertionInvocationFilter() {
        super(CtInvocation.class);
    }

    /**
     *
     * @param invocation
     *          The invocation to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the the super-class' {@link TypeFilter#matches(CtElement)} returns {@code true},
     *          the given {@code invocation}'s {@link CtInvocation#getExecutable() executable} is declared <i>static</i>,
     *          the declaring type's qualified name matches either JUnit 4's {@code Assert} or JUnit Jupiter's
     *          {@code Assertions} class and the implementations {@link #methodSignatureOfInvocationMatches(CtExecutableReference)}
     *          returns {@code true}. {@code false} is returned otherwise.
     */
    @Override
    public boolean matches(CtInvocation<Void> invocation) {
        if(!super.matches(invocation) || !isStaticMethodInvocationOnAssertionsClass(invocation)) {
            return false;
        }

        return methodSignatureOfInvocationMatches(invocation.getExecutable());
    }

    /**
     *
     * @param invocation
     *          The invocation to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code invocation}'s {@link CtInvocation#getExecutable() executable}
     *          is declared <i>static</i> and the qualified name of the executable's declaring type is either
     *          JUnit 4's {@code Assert} class or JUnit Jupiter's {@code Assertions} class.
     *          {@code false} is returned otherwise.
     */
    private boolean isStaticMethodInvocationOnAssertionsClass(CtInvocation<Void> invocation) {
        var invocationExecutable = invocation.getExecutable();
        var isStaticInvocation = invocationExecutable.isStatic();

        if(isStaticInvocation) {
            var qualifiedClassName = invocationExecutable.getDeclaringType()
                    .getQualifiedName();

            return JUNIT_JUPITER_ASSERTIONS_QUALIFIED_CLASS_NAME.equals(qualifiedClassName) ||
                    JUNIT_4_ASSERT_QUALIFIED_CLASS_NAME.equals(qualifiedClassName);
        }

        return false;
    }

    /**
     *
     * @param executableRef
     *          The reference to the executable to check the signature of, not {@code null}. The executable
     *          must be declared <i>static</i> and the declaring type's qualified name must either
     *          be equal to JUnit 4's {@code Asserts} or JUnit Jupiter's {@code Assertions} class.
     *
     * @return
     *          {@code true}, if the given method signature of the given {@code executable}
     *          matches a JUnit assertion method signature. {@code false} is returned otherwise.
     */
    protected abstract boolean methodSignatureOfInvocationMatches(CtExecutableReference<Void> executableRef);

}
