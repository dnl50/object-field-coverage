package de.adesso.objectfieldcoverage.core.processor.filter;

import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Objects;

/**
 * {@link TypeFilter} extension which searches for {@link CtInvocation}s of helper methods. A helper method is a
 * concrete method which is declared in same class or any parent class as the specified test method.
 */
@Slf4j
public class HelperMethodInvocationTypeFilter extends TypeFilter<CtInvocation<?>> {

    /**
     * The test method to find the invocations of helper methods for. Must be declared
     * in a {@link spoon.reflect.declaration.CtClass}.
     */
    private final CtMethod<?> testMethod;

    /**
     *
     * @param testMethod
     *          The test method which is used to find the helper methods invocations in, not {@code null}.
     */
    public HelperMethodInvocationTypeFilter(CtMethod<?> testMethod) {
        super(CtInvocation.class);

        this.testMethod = Objects.requireNonNull(testMethod, "The test method cannot be null!");
    }

    /**
     *
     * @param invocation
     *          The {@link CtInvocation} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code invocation} is an invocation of a concrete {@link CtMethod} and the
     *          method is declared in the given {@code testMethod}'s declaring class or a parent class of it.
     *          {@code false} is returned otherwise.
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

        var invokedMethod = (CtMethod<?>) executableRef.getDeclaration();
        if(invokedMethod == null) {
            log.debug("CtMethod instance of invoked method '{}' not present!", executableRef.getSignature());
            return false;
        }

        if(invokedMethod.isAbstract()) {
            return false;
        }

        var testClassReference = testMethod.getParent(CtClass.class).getReference();
        var superClassesOfTestClass = TypeUtils.findExplicitSuperClassesIncludingClass(testClassReference);
        var invokedMethodParentClass = invokedMethod.getDeclaringType().getReference();

        return superClassesOfTestClass.contains(invokedMethodParentClass);
    }

}
