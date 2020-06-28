package de.adesso.objectfieldcoverage.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

//TODO: JavaDoc
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutableUtils {

    /**
     * The {@link TypeFactory} used to get type references from.
     */
    private static final TypeFactory TYPE_FACTORY = new TypeFactory();

    /**
     * Checks whether a given {@link CtExecutable executable} was invoked inside a given
     * {@link CtMethod method}.
     *
     * @param method
     *          The method to check, not {@code null}.
     *
     * @param executable
     *          The executable to find an invocation in the given {@code method} of, not
     *          {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code executable} is invoked at least once in the
     *          given {@code method}. {@code false} is returned otherwise.
     *
     * @see #countInvocationsOfExecutable(CtMethod, CtExecutable)
     */
    public static boolean isExecutableInvoked(CtMethod<?> method, CtExecutable<?> executable) {
        return countInvocationsOfExecutable(method, executable) > 0L;
    }

    /**
     * Checks whether a given {@link CtExecutable executable} was invoked inside a given
     * {@link CtMethod method}.
     *
     * @param methods
     *          The methods to check, not {@code null}.
     *
     * @param executable
     *          The executable to find an invocation in the given {@code methods} of, not
     *          {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code executable} is invoked at least once
     *          in one of the given {@code methods}. {@code false} is returned otherwise.
     *
     * @see #countInvocationsOfExecutable(CtMethod, CtExecutable)
     */
    public static boolean isExecutableInvoked(Collection<CtMethod<?>> methods, CtExecutable<?> executable) {
        return countInvocationsOfExecutable(methods, executable) > 0L;
    }

    /**
     * Counts the invocations of a given {@link CtExecutable execuable} in a given {@link CtMethod method}.
     * The executable must be invoked directly inside the given method.
     *
     * @param method
     *          The method to count the invocations of the given {@code executable} in,
     *          not {@code null}.
     *
     * @param executable
     *          The executable to count the invocations of, not {@code null}.
     *
     * @return
     *          The number of invocations of the given {@code executable} in the given {@code method}.
     *
     * @see #countInvocationsOfExecutable(Collection, CtExecutable)
     */
    public static long countInvocationsOfExecutable(CtMethod<?> method, CtExecutable<?> executable) {
        Objects.requireNonNull(method, "method cannot be null!");

        return countInvocationsOfExecutable(List.of(method), executable);
    }

    /**
     * Counts the invocations of a given {@link CtExecutable execuable} in a given collection of
     * {@link CtMethod methods}. The executable must be invoked directly inside the given methods.
     *
     * @param methods
     *          The methods to count the invocations of the given {@code executable} in,
     *          not {@code null}.
     *
     * @param executable
     *          The executable to count the invocations of, not {@code null}.
     *
     * @return
     *          The number of invocations of the given {@code executable} in the given {@code method}.
     */
    public static long countInvocationsOfExecutable(Collection<CtMethod<?>> methods, CtExecutable<?> executable) {
        Objects.requireNonNull(methods, "methods cannot be null!");
        Objects.requireNonNull(executable, "executable cannot be null!");

        if(methods.isEmpty()) {
            return 0L;
        }

        var executableRef = executable.getReference();

        return methods.stream()
                .map(method -> method.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class)))
                .flatMap(Collection::stream)
                .map(CtAbstractInvocation::getExecutable)
                .filter(executableRef::equals)
                .count();
    }

    /**
     *
     * @param executable
     *          The executable to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the {@link CtMethod#getType() return type} of the given executable
     *          is either {@link Void} or the executable is declared as <i>void</i>. {@code false}
     *          is returned otherwise.
     */
    public static boolean isVoidExecutable(CtExecutable<?> executable) {
        return isVoidExecutable(executable.getReference());
    }

    /**
     *
     * @param executableRef
     *          The reference of an executable to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the {@link CtExecutableReference#getType() return type} of the given executable
     *          is either {@link Void} or the executable is declared as <i>void</i>. {@code false}
     *          is returned otherwise.
     */
    public static boolean isVoidExecutable(CtExecutableReference<?> executableRef) {
        var methodReturnType = executableRef.getType();

        return TYPE_FACTORY.VOID_PRIMITIVE.equals(methodReturnType) || TYPE_FACTORY.VOID.equals(methodReturnType);
    }

}
