package de.adesso.objectfieldcoverage.core.processor.filter;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Objects;

/**
 * {@link TypeFilter} extension which filters for {@link CtInvocation}s of a specific {@link CtExecutable}.
 *
 * @param <T>
 *          The return type of the invocation.
 */
public class ExecutableInvocationTypeFilter<T> extends TypeFilter<CtInvocation<T>> {

    /**
     * The executable to find the invocations of.
     */
    private final CtExecutable<T> executable;

    /**
     *
     * @param executable
     *          The executable to find the invocations of, not {@code null}.
     */
    public ExecutableInvocationTypeFilter(CtExecutable<T> executable) {
        super(CtInvocation.class);

        this.executable = Objects.requireNonNull(executable, "The executable cannot be null!");
    }

    /**
     *
     * @param invocation
     *          The invocation to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code invocation}'s executable is equal to the configured
     *          executable. {@code false} is returned otherwise.
     */
    @Override
    public boolean matches(CtInvocation<T> invocation) {
        if(!super.matches(invocation)) {
            return false;
        }

        var invocationQualifiedSignature = buildQualifiedSignature(invocation.getExecutable());
        var executableSignature = buildQualifiedSignature(executable.getReference());

        return executableSignature.equals(invocationQualifiedSignature);
    }

    /**
     *
     * @param executableReference
     *          The {@link CtExecutableReference} to build the string for, not {@code null}.
     *
     * @return
     *          The {@link CtExecutableReference#getSignature() signature} of the given {@code executableReference}
     *          prefixed with the qualified name of the declaring type.
     */
    private String buildQualifiedSignature(CtExecutableReference<?> executableReference) {
        var signature = executableReference.getSignature();
        var declaringTypeQn = executableReference.getDeclaringType()
                .getQualifiedName();

        return String.format("%s.%s", declaringTypeQn, signature);
    }

}
