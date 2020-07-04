package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.annotation.IgnoreCoverage;
import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import de.adesso.objectfieldcoverage.core.processor.evaluation.AssertionEvaluationBuilder;
import de.adesso.objectfieldcoverage.core.util.AnnotationUtils;
import de.adesso.objectfieldcoverage.core.util.ExecutableUtils;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.CollectionUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adesso.objectfieldcoverage.core.processor.util.ProcessorUtils.*;

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    /**
     * The {@link TargetExecutableFinder}s to find the targeted executables of a test method
     * of.
     */
    private final List<TargetExecutableFinder> targetExecutableFinders;

    /**
     * The {@link AccessibilityAwareFieldFinder}s which are responsible to find the accessible fields
     * in the return value / thrown exception of a given target executable.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * The {@link TestMethodFinder}s which are responsible to find the test methods in a given
     * {@link CtClass}.
     */
    private final List<TestMethodFinder> testMethodFinders;

    /**
     * The
     */
    private final List<AssertionFinder> assertionFinders;

    /**
     *
     */
    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    /**
     * A list of {@link InvocationThrowableAnalyzer}s to check whether an {@link CtInvocation}
     * is expected to raise a throwable.
     */
    private final List<InvocationThrowableAnalyzer> invocationThrowableAnalyzers;

    private final InvocationResultTracker invocationResultTracker;

    private final List<Predicate<CtClass<?>>> classPreFilters;

    @Override
    public void process(CtClass<?> clazz) {
        try {
            processInternal(clazz);
        } catch (RuntimeException e) {
            log.error(String.format("Error while processing class '%s': ", clazz.getQualifiedName()), e);
        }
    }

    private void processInternal(CtClass<?> clazz) {
        if(excludedByFilters(clazz)) {
            log.info("Class '{}' is excluded by at least one additional pre filter!", clazz.getQualifiedName());
            return;
        }

        if(!TypeUtils.isPotentialTestClass(clazz)) {
            log.debug("Class '{}' is not a potential test class since it is not declared in a source file matching " +
                    "the criteria!", clazz.getQualifiedName());
            return;
        }

        if(AnnotationUtils.isAnnotatedWith(clazz, IgnoreCoverage.class)) {
            log.debug("Class '{}' is annotated with @IgnoreCoverage and will therefore be ignored!",
                    clazz.getQualifiedName());
            return;
        }

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

    private void processTestMethod(CtMethod<?> testMethod, CtClass<?> testClass) {
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

        var allAssertions = findAssertions(testMethod, invokedHelperMethods);
        if(log.isDebugEnabled()) {
            log.debug("Found {} assertions in test method '{}' and helper methods '{}'!", allAssertions.size(),
                    testMethod.getSignature(), invokedHelperMethods.stream().map(CtExecutable::getSignature).collect(Collectors.toList()));
        }

        for (var invokedTargetExecutable : invokedTargetExecutables) {
            Set<CtInvocation<?>> invocationsOfTargetExecutable = findInvocationsOfExecutable(testAndHelperMethods, invokedTargetExecutable);
            log.debug("Target executable '{}' invoked {} times!", invokedTargetExecutable.getSignature(),
                    invocationsOfTargetExecutable.size());

            var invocationToAssertionsMap = mapInvocationToAssertions(allAssertions, invocationsOfTargetExecutable);

            invocationToAssertionsMap.forEach((invocation, assertions) -> {
                if(!isNonVoidExecutableOrThrows(invocation, testMethod, invokedHelperMethods)) {
                    throw new IllegalStateException("Target executable invocation is void and does not throw!");
                }

                processTargetExecutableInvocation(testClass, invocation, assertions);
            });
        }

        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
    }

    private void processTargetExecutableInvocation(CtClass<?> testClass, CtInvocation<?> targetExecutableInvocation, List<AbstractAssertion<?>> assertions) {
        var evaluationInfoBuilder = new AssertionEvaluationBuilder(fieldFinders, equalsMethodAnalyzers);
        var fullInfoForReturnedType = evaluationInfoBuilder.build(testClass, targetExecutableInvocation.getType());

        var coveredPaths = assertions.stream()
                .map(assertion -> {
                    var evaluationInformation = evaluationInfoBuilder.build(assertion);
                    var pathPrefix = invocationResultTracker.getPathPrefixForAccess(assertion.getAssertedExpression(),
                            targetExecutableInvocation, evaluationInformation.getAccessibleFieldsGraph());

                    if(pathPrefix.isEmpty()) {
                        log.warn("No path prefix for assertion '{}' present!", assertion);
                        return Set.<Path>of();
                    }

                    return assertion.getCoveredPaths(evaluationInformation).stream()
                        .map(coveredPath -> coveredPath.prepend(pathPrefix.get()))
                        .collect(Collectors.toSet());
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var allPaths = new HashSet<>(fullInfoForReturnedType.getAccessibleFieldsGraph()
                .getTransitiveReachabilityPaths());
        var coveredPathsContainedInAllPaths = CollectionUtils.intersection(allPaths, coveredPaths);
        var coverage = String.format("%.2f", (double) allPaths.size() / (double) coveredPathsContainedInAllPaths.size());

        log.info("{} out of {} paths of target executable invocation '{}' are covered! [Object Field Coverage: {}%]",
                coveredPathsContainedInAllPaths.size(), allPaths.size(), targetExecutableInvocation, coverage);
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
                    if(AnnotationUtils.isAnnotatedWith(testMethod, IgnoreCoverage.class)) {
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

    List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return assertionFinders.stream()
                .map(assertionFinder -> assertionFinder.findAssertions(testMethod, helperMethods))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    boolean isNonVoidExecutableOrThrows(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return !ExecutableUtils.isVoidExecutable(invocation.getExecutable()) ||
                throwsThrowable(invocation, testMethod, helperMethods);
    }

    boolean throwsThrowable(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return invocationThrowableAnalyzers.stream()
                .anyMatch(analyzer -> analyzer.isExpectedToRaiseThrowable(invocation, testMethod, helperMethods));
    }

    Map<CtInvocation<?>, List<AbstractAssertion<?>>> mapInvocationToAssertions(Collection<AbstractAssertion<?>> abstractAssertions,
                                                                               Collection<CtInvocation<?>> invocations) {
        return invocations.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        invocation -> abstractAssertions.stream()
                                .filter(assertion -> invocationResultTracker.accessesTargetInvocationResult(assertion.getAssertedExpression(),
                                        invocation))
                                .collect(Collectors.toList())
                ));
    }

    private boolean excludedByFilters(CtClass<?> clazz) {
        return classPreFilters.stream()
                .anyMatch(predicate -> predicate.test(clazz));
    }

    private boolean containsThrowingExpression(Collection<AbstractAssertion<?>> assertions) {
        return assertions.stream()
                .anyMatch(AbstractAssertion::expressionRaisesThrowable);
    }

    private boolean allExpressionsThrow(Collection<AbstractAssertion<?>> assertions) {
        return assertions.stream()
                .allMatch(AbstractAssertion::expressionRaisesThrowable);
    }

}
