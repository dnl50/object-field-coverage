package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.TargetExecutableFinder;
import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import de.adesso.objectfieldcoverage.api.annotation.IgnoreCoverage;
import de.adesso.objectfieldcoverage.core.processor.filter.HelperMethodInvocationFilter;
import de.adesso.objectfieldcoverage.core.util.ExecutableUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    private final List<TargetExecutableFinder> targetExecutableFinders;

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final List<AssertionFinder> assertionFinders;

    @Override
    public void process(CtClass<?> clazz) {
        var testMethodsInClass = findAllTestMethods(clazz);

        if(testMethodsInClass.isEmpty()) {
            log.info("No test methods in class '{}'!", clazz.getQualifiedName());
            return;
        } else {
            log.info("Found {} test methods in test class '{}'!", testMethodsInClass.size(),
                    clazz.getQualifiedName());
        }

        testMethodsInClass.forEach(testMethod -> processTestMethod(testMethod, clazz));
    }

    private void processTestMethod(CtMethod<?> testMethod, CtClass<?> testClazz) {
        log.info("Started processing of test method '{}'!", testMethod.getSimpleName());

        var helperMethods = findHelperMethods(testMethod);


        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
    }

    private void processTargetMethodInvocation() {

    }

    /**
     *
     * @param testMethod
     *          The test method in which the given {@code executables} should get invoked, not {@code null}.
     *
     * @param executables
     *          The corresponding executables, not {@code null}.
     *
     * @return
     *          A set containing all executables which are contained in the given {@code executables}
     *          collection and which are invoked inside the given method.
     */
    private Set<CtExecutable<?>> filterInvokedExecutables(CtMethod<?> testMethod, Collection<CtExecutable<?>> executables) {
        return executables.stream()
                .filter(executable -> {
                    if(!ExecutableUtils.isExecutableInvoked(testMethod, executable)) {
                        log.warn("Executable '{}' not invoked in method '{}'!", executable.getSignature(),
                                testMethod.getSignature());
                        return false;
                    }

                    return true;
                })
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
    private List<CtMethod<?>> findHelperMethods(CtMethod<?> testMethod) {
        return testMethod.getElements(new HelperMethodInvocationFilter(testMethod)).stream()
                .map(CtInvocation::getExecutable)
                .map(executable -> (CtMethod<?>) executable)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to find all test methods in, not {@code null}.
     *
     * @return
     *          A list containing all test methods returned by all registered {@link TestMethodFinder}s
     *          with {@link IgnoreCoverage} annotated methods excluded.
     */
    private List<CtMethod<?>> findAllTestMethods(CtClass<?> clazz) {
        return testMethodFinders.stream()
                .map(finder -> finder.findTestMethods(clazz))
                .flatMap(List::stream)
                .distinct()
                .filter(testMethod -> {
                    if(testMethod.getAnnotation(IgnoreCoverage.class) != null) {
                        log.info("Test method '{}' will be ignored since it is annotated with the @IgnoreCoverage annotation!",
                                testMethod.getSignature());
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     *
     * @param testMethod
     * @param helperMethods
     * @return
     */
    private Set<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return targetExecutableFinders.stream()
                .map(finder -> finder.findTargetExecutables(testMethod, helperMethods))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
