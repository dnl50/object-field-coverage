package de.adesso.objectfieldcoverage.core.processor.util;

import de.adesso.objectfieldcoverage.core.processor.filter.ExecutableInvocationTypeFilter;
import de.adesso.objectfieldcoverage.core.processor.filter.HelperMethodInvocationTypeFilter;
import de.adesso.objectfieldcoverage.core.processor.filter.InvokedExecutableFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing static methods for executable invocations and more.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessorUtils {

    /**
     *
     * @param methods
     *          The methods in which the given {@code executables} should get invoked.
     *
     * @param executables
     *          The corresponding executables, not {@code null}.
     *
     * @return
     *          A set containing all executables which are contained in the given {@code executables}
     *          collection and which are invoked inside the given methods.
     */
    public static Set<CtExecutable<?>> filterInvokedExecutables(Collection<CtMethod<?>> methods, Collection<CtExecutable<?>> executables) {
        var methodsSet = methods == null ? Set.<CtMethod<?>>of() : Set.copyOf(methods);
        var invokedExecutableFilter = new InvokedExecutableFilter(methodsSet);

        return executables.stream()
                .filter(invokedExecutableFilter)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param testMethod
     *          The test method to find all invoked helper methods in, not {@code null}.
     *
     * @return
     *          A list containing all helper methods in order of their invocation.
     */
    public static List<CtMethod<?>> findInvokedHelperMethods(CtMethod<?> testMethod) {
        return testMethod.getElements(new HelperMethodInvocationTypeFilter(testMethod)).stream()
                .map(CtInvocation::getExecutable)
                .map(executable -> (CtMethod<?>) executable)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param methods
     *          The methods to find the invocations of the given {@code executable} in, not {@code null}.
     *
     * @param executable
     *          The executable to find the invocations of, not {@code null}.
     *
     * @param <T>
     *          The return type of the executable.
     *
     * @return
     *          A set containing all invocations of the given {@code executable}.
     */
    public static <T> Set<CtInvocation<T>> findInvocationsOfExecutable(Set<CtMethod<?>> methods, CtExecutable<T> executable) {
        var invocationFilter = new ExecutableInvocationTypeFilter<>(executable);

        return methods.stream()
                .map(method -> method.getElements(invocationFilter))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
