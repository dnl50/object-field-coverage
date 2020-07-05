package de.adesso.objectfieldcoverage.api.filter;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.Set;

/**
 * {@link TypeFilter} extension to find method invocations of methods which are declared on types with given
 * qualified names.
 */
public class QualifiedNameMethodInvocationTypeFilter extends TypeFilter<CtInvocation<?>> {

    /**
     * The fully qualified names of the types to find method invocations on.
     */
    private final Set<String> qualifiedTypeNames;

    /**
     *
     * @param qualifiedTypeNames
     *          The fully qualified names of the types to find method invocations on, not {@code null}.
     */
    public QualifiedNameMethodInvocationTypeFilter(Collection<String> qualifiedTypeNames) {
        super(CtInvocation.class);

        this.qualifiedTypeNames = Set.copyOf(qualifiedTypeNames);
    }

    /**
     *
     * @param qualifiedTypeName
     *          The fully qualified name of the type to find method invocations on, not {@code null}.
     */
    public QualifiedNameMethodInvocationTypeFilter(String qualifiedTypeName) {
        this(Set.of(qualifiedTypeName));
    }

    /**
     *
     * @param invocation
     *          The {@link CtInvocation} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code invocation} is a method invocation of a method which is declared on a
     *          type whose qualified name is contained in the given qualified names. {@code false} is returned otherwise.
     */
    @Override
    public boolean matches(CtInvocation<?> invocation) {
        if(!super.matches(invocation)) {
            return false;
        }

        var executableRef = invocation.getExecutable();

        if(executableRef.isConstructor()) {
            return false;
        }

        return qualifiedTypeNames.contains(executableRef.getDeclaringType().getQualifiedName());
    }

}
