package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.core.annotation.TestTarget;
import de.adesso.objectfieldcoverage.core.annotation.TestTargets;
import de.adesso.objectfieldcoverage.core.util.exception.IllegalMethodSignatureException;
import de.adesso.objectfieldcoverage.core.util.exception.TargetMethodNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//TODO: JavaDoc
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutableUtil {

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
        var typeFactory = new TypeFactory();
        var methodReturnType = executable.getType();

        return typeFactory.VOID_PRIMITIVE.equals(methodReturnType) || typeFactory.VOID.equals(methodReturnType);
    }

    /**
     * Utility method to find the executables which are targeted by a given test method. The given
     * {@link CtMethod} must be annotated with either {@link TestTarget} or {@link TestTargets}.
     *
     * @param testMethod
     *          The test method to find the targeted methods of, not {@code null}. Must be annotated
     *          with either {@link TestTarget} or {@link TestTargets}. The {@link TestTargets}
     *          must must contain at least one {@link TestTarget} annotation in case it is present.
     *
     * @return
     *          A list containing the target executables which are specified using the method identifiers
     *          given in the {@link TestTarget} annotation(s).
     *
     * @throws IllegalArgumentException
     *          When the given {@code testMethod} is neither annotated with {@link TestTarget} nor
     *          {@link TestTargets}.
     */
    public static List<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod, CtModel underlyingModel) {
        Objects.requireNonNull(testMethod, "testMethod cannot be null!");
        Objects.requireNonNull(underlyingModel, "underlyingModel cannot be null!");

        var testTargetAnnotation = testMethod.getAnnotation(TestTarget.class);
        if(Objects.nonNull(testTargetAnnotation)) {
            return List.of(findTargetExecutable(testTargetAnnotation, underlyingModel));
        }

        var testTargetsAnnotation = testMethod.getAnnotation(TestTargets.class);
        if(testTargetsAnnotation != null) {
            var testTargetAnnotations = testTargetsAnnotation.value();

            if(testTargetAnnotations.length == 0) {
                var exceptionMessage = String.format("@TestTargets annotation on test method %s is empty!",
                        testMethod.getSimpleName());

                log.error(exceptionMessage);
                throw new IllegalArgumentException(exceptionMessage);
            }

            return Arrays.stream(testTargetsAnnotation.value())
                    .map(testTarget -> findTargetExecutable(testTarget, underlyingModel))
                    .collect(Collectors.toList());
        }

        var exceptionMessage = String.format("Given test method '%s' neither annotated with @TestTarget nor with @TestTargets",
                testMethod.getSimpleName());
        log.error(exceptionMessage);
        throw new IllegalArgumentException(exceptionMessage);
    }

    /**
     * Utility method to find the executable identified by the method identifier specified
     * in a given {@link TestTarget} annotation.
     *
     * @param testTargetAnnotation
     *          The annotation containing the method identifier, not {@code null}.
     *
     * @param underlyingModel
     *          The Spoon meta-model to find the target executable in, not {@code null}.
     *
     * @return
     *          The target executable.
     *
     * @throws TargetMethodNotFoundException
     *          When no executable was found in the given {@code underlyingModel} using the
     *          given {@code methodIdentifier}.
     *
     * @throws IllegalMethodSignatureException
     *          When the target executable is a <i>void</i> method and the {@code exceptionExpected}
     *          flag of the given {@code testTargetAnnotation} is set to {@code false}.
     */
    public static CtExecutable<?> findTargetExecutable(TestTarget testTargetAnnotation, CtModel underlyingModel) {
        Objects.requireNonNull(testTargetAnnotation, "testTargetAnnotation cannot be null!");
        Objects.requireNonNull(underlyingModel, "underlyingModel cannot be null!");

        var methodIdentifier = testTargetAnnotation.value();

        var targetExecutable = TargetExecutableFinder.findTargetExecutable(methodIdentifier, underlyingModel)
                .orElseThrow(() -> new TargetMethodNotFoundException(methodIdentifier));

        if(isVoidExecutable(targetExecutable)) {
            var exceptionMessage = String.format("The target executable '%s' is a void method, but the exceptionExpected flag is set to false!",
                    methodIdentifier);

            log.error(exceptionMessage);
            throw new IllegalMethodSignatureException(exceptionMessage);
        }

        return targetExecutable;
    }

}
