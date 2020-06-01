package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.core.analyzer.ObjectsEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.analyzer.PrimitiveTypeEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.lombok.LombokAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnit4TestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnitJupiterTestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
import de.adesso.objectfieldcoverage.core.processor.evaluation.AssertionEvaluationBuilder;
import de.adesso.objectfieldcoverage.core.util.ExecutableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.processing.AbstractProcessor;
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

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final List<AssertionFinder> assertionFinders;

    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    private final List<TestMethodPreProcessor> testMethodPreProcessors;

    public ObjectFieldCoverageProcessor() {
        this.fieldFinders = List.of(new LombokAccessibilityAwareFieldFinder(), new DirectAccessAccessibilityAwareFieldFinder(),
                new JavaBeansAccessibilityAwareFieldFinder());
        this.testMethodFinders = List.of(new JUnitJupiterTestMethodFinder(), new JUnit4TestMethodFinder());
        this.assertionFinders = List.of(new JUnitAssertionFinder());
        this.equalsMethodAnalyzers = List.of(new ObjectsEqualsMethodAnalyzer(), new PrimitiveTypeEqualsMethodAnalyzer());
        this.testMethodPreProcessors = List.of();
    }

    /**
     * The central entry point to calculate the Object Field Coverage for a given {@link CtClass}.
     *
     * @param clazz
     *          The {@link CtClass} which should be processed.
     */
    @Override
    public void process(CtClass<?> clazz) {
        var qualifiedNameOfClass = clazz.getQualifiedName();
        log.info("Starting processing of class '{}'...", qualifiedNameOfClass);

        var testMethodsInClass = findTestMethods(clazz);

        if(testMethodsInClass.isEmpty()) {
            log.info("No test methods in class '{}' found!", qualifiedNameOfClass);
        } else {
            log.info("Found {} test methods in class '{}'", testMethodsInClass.size(), qualifiedNameOfClass);
            var preprocessedTestMethods = preProcessTestMethods(testMethodsInClass);
            preprocessedTestMethods.forEach(testMethod -> processTestMethod(testMethod, clazz));
        }

        log.info("Finished processing of class '{}'!", qualifiedNameOfClass);
    }

    private void processTestMethod(CtMethod<?> testMethod, CtClass<?> testClazz) {
        log.info("Start processing of test method '{}'...", testMethod.getSignature());
        var targetExecutables = findTargetExecutablesReturningClassType(testMethod);
        log.info("Test method '{}' has {} target executable(s): {}", testMethod.getSignature(), targetExecutables.size(),
                targetExecutables.stream().map(Object::toString).collect(Collectors.joining()));

        if(targetExecutables.isEmpty()) {
            log.warn("No target executables returning a class type found!");
        } else {

        }

        var assertionEvaluationBuilder = new AssertionEvaluationBuilder(fieldFinders, equalsMethodAnalyzers);


//        assertionMap.entrySet().forEach(entry -> {
//            var assertion = entry.getKey();
//            var evaluationInformation = entry.getValue();
//
//            var result = assertion.calculateMetricValue(evaluationInformation);
//            log.info("Result: {}", result);
//        });

        //TODO:
        // - build assertion evaluation obj for asserted type of each assertion
        //   - primitive types? root = leaf
        //   - String?

        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
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
                    if(!ExecutableUtil.isExecutableInvoked(testMethod, executable)) {
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
     * @param potentialTestClass
     *          The {@link CtClass} which could potentially contain <i>test methods</i>, not {@code null}.
     *
     * @return
     *          A list containing all {@link CtMethod}s which are identified as test method by the registered
     *          {@link TestMethodFinder}s.
     */
    private List<CtMethod<?>> findTestMethods(CtClass<?> potentialTestClass) {
        return testMethodFinders.stream()
                .map(finder -> finder.findTestMethods(potentialTestClass))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * TODO: take other places potentially containing assertions into account
     *
     * @param testMethod
     *          The test method to find assertions in, not {@code null}.
     *
     * @return
     *          A list containing all {@link AbstractAssertion}s which have been found in the given {@code testMethod}.
     */
    private List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod) {
        return assertionFinders.stream()
                .map(assertionFinder -> assertionFinder.findAssertions(testMethod))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param testMethod
     *          The test method to get the target executables of, not {@code null}. Must be annotated with
     *          either {@link de.adesso.objectfieldcoverage.core.annotation.TestTarget} or {@link de.adesso.objectfieldcoverage.core.annotation.TestTargets}.
     *
     * @return
     *          The {@link CtExecutable}s which return a class type and are targeted by the given {@code testMethod}.
     */
    private Set<CtExecutable<?>> findTargetExecutablesReturningClassType(CtMethod<?> testMethod) {
        return ExecutableUtil.findTargetExecutables(testMethod, getFactory().getModel()).stream()
                .filter(targetExecutable -> {
                    if(targetExecutable.getType().isInterface()) {
                        log.warn("Target executable '{}' returns an interface type for which the metric is not defined!",
                                targetExecutable.getSignature());
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param testMethods
     *          A list containing all {@link CtMethod}s which should be processed by the
     *          registered {@link TestMethodPreProcessor}.
     *
     * @return
     *          A list containing the preprocessed methods.
     */
    private List<CtMethod<?>> preProcessTestMethods(List<CtMethod<?>> testMethods) {
        return testMethods.stream()
                .map(testMethod -> {
                    var processedTestMethod = testMethod;

                    for(var preProcessor : testMethodPreProcessors) {
                        log.info("Applying preprocessor '{}' to test method '{}'!", preProcessor.getClass().getName(),
                                testMethod.getSimpleName());
                        processedTestMethod = preProcessor.processTestMethod(processedTestMethod);
                    }

                    return processedTestMethod;
                })
                .collect(Collectors.toList());
    }

}
