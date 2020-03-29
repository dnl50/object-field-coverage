package de.adesso.objectfieldcoverage.core.junit.assertion.filter;

import spoon.reflect.reference.CtExecutableReference;

import java.util.Set;

/**
 * {@link AbstractJUnitAssertionInvocationFilter} implementation which filters for {@code assertEquals} method
 * invocations.
 */
public class JUnitAssertEqualsInvocationFilter extends AbstractJUnitAssertionInvocationFilter {

    /**
     *
     * @param executableRef
     *          The reference to the executable to check the signature of, not {@code null}. The executable
     *          must be declared <i>static</i> and the declaring type's qualified name must either
     *          be equal to JUnit 4's {@code Asserts} or JUnit Jupiter's {@code Assertions} class.
     *
     * @return
     *          {@code true}, if the given {@code executableRef}'s {@link CtExecutableReference#getSimpleName() simple
     *          name} is equal to {@code assertEquals}. {@code false is returned otherwise}.
     */
    @Override
    protected boolean methodSignatureOfInvocationMatches(CtExecutableReference<Void> executableRef) {
        var executableSimpleName = executableRef.getSimpleName();
        return Set.of("assertEquals", "assertNull").contains(executableSimpleName);
    }

}
