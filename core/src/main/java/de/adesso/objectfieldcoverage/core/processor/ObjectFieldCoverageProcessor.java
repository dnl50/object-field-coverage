package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.annotation.IgnoreCoverage;
import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.core.processor.evaluation.AssertionEvaluationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adesso.objectfieldcoverage.core.processor.util.ProcessorUtils.*;

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    private final List<TargetExecutableFinder> targetExecutableFinders;

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final List<AssertionFinder> assertionFinders;

    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    @Override
    public void process(CtClass<?> clazz) {
        try {
            processInternal(clazz);
        } catch (RuntimeException e) {
            log.error(String.format("Error while processing class '%s': ", clazz.getQualifiedName()), e);
        }
    }

    private void processInternal(CtClass<?> clazz) {
        // exclude anonymous classes
        if(clazz.isAnonymous()) {
            log.debug("Anonymous class '{}' will be ignored!", clazz.getQualifiedName());
            return;
        }

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

        var invokedHelperMethods = findInvokedHelperMethods(testMethod);
        if(log.isDebugEnabled()) {
            log.debug("Test method '{}' invokes {} helper methods: {}", testMethod.getSignature(),
                    invokedHelperMethods.size(), invokedHelperMethods.stream().map(CtExecutable::getSignature).collect(Collectors.toList()));
        }

        var targetExecutables = findTargetExecutables(testMethod, invokedHelperMethods);
        if(log.isDebugEnabled()) {
            log.debug("Test method '{}' targets {} executables: {}", testMethod.getSignature(), targetExecutables.size(),
                    targetExecutables.stream().map(CtExecutable::getSignature).collect(Collectors.toList()));
        }

        var testAndHelperMethods = new HashSet<>(invokedHelperMethods);
        testAndHelperMethods.add(testMethod);
        var invokedTargetExecutables = filterInvokedExecutables(testAndHelperMethods, targetExecutables);
        log.debug("{} of {} target executables are invoked at least once!", invokedTargetExecutables.size(), targetExecutables.size());

        for (var invokedTargetExecutable : invokedTargetExecutables) {
            var invocationsOfTargetExecutable = findInvocationsOfExecutable(testAndHelperMethods, invokedTargetExecutable);
            log.debug("Target executable '{}' invoked {} times!", invokedTargetExecutable.getSignature(),
                    invocationsOfTargetExecutable.size());

            for(var targetExecutableInvocation : invocationsOfTargetExecutable) {
                processTargetExecutableInvocation(testMethod, invokedHelperMethods, targetExecutableInvocation);
            }
        }

        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
    }

    private void processTargetExecutableInvocation(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods, CtInvocation<?> targetExecutableInvocation) {
        var assertionEvalInformation = new AssertionEvaluationBuilder(fieldFinders, equalsMethodAnalyzers)
                .build(testMethod.getDeclaringType(), targetExecutableInvocation.getType());

        int i = 5;
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
    List<CtMethod<?>> findAllTestMethods(CtClass<?> clazz) {
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
     *          The test method to find the target executables of, not {@code null}.
     *
     * @param helperMethods
     *          The helper methods which are invoked inside the given {@code testMethod}, not {@code null}.
     *
     * @return
     *          All target executables of the given {@code testMethod}. The executables might not actually be
     *          invoked inside the given methods.
     */
    Set<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return targetExecutableFinders.stream()
                .map(finder -> finder.findTargetExecutables(testMethod, helperMethods))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
