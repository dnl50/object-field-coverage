package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.annotation.CalculateCoverage;
import de.adesso.objectfieldcoverage.annotation.IgnoreCoverage;
import de.adesso.objectfieldcoverage.api.*;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.core.processor.evaluation.AssertionEvaluationBuilder;
import de.adesso.objectfieldcoverage.core.util.AnnotationUtils;
import de.adesso.objectfieldcoverage.core.util.ExecutableUtils;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.fraction.Fraction;
import org.codehaus.plexus.util.CollectionUtils;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adesso.objectfieldcoverage.core.processor.util.ProcessorUtils.*;

/**
 * Central {@link AbstractProcessor} implementation for calculating the Object Field Coverage Metric for
 * a given {@link CtClass}. Caches the analyzed classes and prints an overall metric value after the
 * {@link #processingDone()} method is called.
 */
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
     * The {@link AssertionFinder}s used to find assertions in a test method and executed helper methods in.
     */
    private final List<AssertionFinder> assertionFinders;

    /**
     * The {@link EqualsMethodAnalyzer}s used to build the equals graph with.
     */
    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    /**
     * A list of {@link InvocationThrowableAnalyzer}s to check whether an {@link CtInvocation}
     * is expected to raise a throwable.
     */
    private final List<InvocationThrowableAnalyzer> invocationThrowableAnalyzers;

    /**
     * The {@link InvocationResultTracker} instance used to check if a given expression references the result
     * of an target executable invocation.
     */
    private final InvocationResultTracker invocationResultTracker;

    /**
     * Internal result cache.
     */
    private final Map<Pair<CtClass<?>, CtInvocation<?>>, Fraction> coverageResult = new HashMap<>();

    /**
     * The {@link Settings} instance to modify the internal behaviour.
     */
    @Getter
    @Setter
    private Settings settings = Settings.defaultSettings();

    /**
     * Clears the internal result cache.
     */
    @Override
    public void init() {
        super.init();

        coverageResult.clear();
    }

    /**
     *
     * @param candidate
     *          The candidate {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true}, when the {@link #shouldBeExcluded(CtClass)} method returns {@code false}. {@code false}
     *          is returned otherwise.
     */
    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        return !shouldBeExcluded(candidate);
    }

    /**
     * Calculates the overall metric value and logs it with info level.
     */
    @Override
    public void processingDone() {
        super.processingDone();

        log.info("Processing finished! Printing result....");

        var coverageResultsGroupedByClass = coverageResult.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getLeft()));

    }

    /**
     * Catches all runtime exceptions thrown by the internal processing and logs them with error severity.
     *
     * @param clazz
     *          The {@link CtClass} instance to analyze, not {@code null}.
     */
    @Override
    public void process(CtClass<?> clazz) {
        try {
            processInternal(clazz);
        } catch (RuntimeException e) {
            log.error(String.format("Error while processing class '%s': ", clazz.getQualifiedName()), e);
        }
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} instance to analyze, not {@code null}.
     */
    private void processInternal(CtClass<?> clazz) {
        var testMethodsInClass = findTestMethods(clazz);

        if(testMethodsInClass.isEmpty()) {
            log.info("No test methods in class '{}'!", clazz.getQualifiedName());
            return;
        } else {
            log.info("Found {} test methods in test class '{}'!", testMethodsInClass.size(),
                    clazz.getQualifiedName());
        }

        testMethodsInClass.forEach(testMethod -> processTestMethod(testMethod, clazz));
    }

    /**
     *
     * @param testMethod
     *          The {@link CtMethod} to process, not {@code null}.
     *
     * @param testClass
     *          The {@link CtClass} the given {@code testMethod} is declared in, not {@code null}.
     */
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
                            targetExecutableInvocation, fullInfoForReturnedType.getAccessibleFieldsGraph());

                    var coveredPathsOfAssertion = assertion.getCoveredPaths(evaluationInformation);

                    if(pathPrefix.isEmpty()) {
                        log.warn("No path prefix for assertion '{}' required!", assertion);
                        return coveredPathsOfAssertion;
                    } else {
                        return coveredPathsOfAssertion.stream()
                                .map(coveredPath -> coveredPath.prepend(pathPrefix.get()))
                                .collect(Collectors.toSet());
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var allPaths = new HashSet<>(fullInfoForReturnedType.getAccessibleFieldsGraph()
                .getTransitiveReachabilityPaths());
        var coveredPathsContainedInAllPaths = CollectionUtils.intersection(allPaths, coveredPaths);
        var coverage = Fraction.getReducedFraction(coveredPathsContainedInAllPaths.size(), allPaths.size());

        log.info("{} out of {} paths of target executable invocation '{}' are covered! [Object Field Coverage: {}%]",
                coveredPathsContainedInAllPaths.size(), allPaths.size(), targetExecutableInvocation, coverage);

        coverageResult.put(Pair.of(testClass, targetExecutableInvocation), coverage);
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

    private boolean isNonVoidExecutableOrThrows(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return !ExecutableUtils.isVoidExecutable(invocation.getExecutable()) ||
                throwsThrowable(invocation, testMethod, helperMethods);
    }

    private boolean throwsThrowable(CtInvocation<?> invocation, CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        return invocationThrowableAnalyzers.stream()
                .anyMatch(analyzer -> analyzer.isExpectedToRaiseThrowable(invocation, testMethod, helperMethods));
    }

    private Map<CtInvocation<?>, List<AbstractAssertion<?>>> mapInvocationToAssertions(Collection<AbstractAssertion<?>> abstractAssertions,
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

    /**
     *
     * @param assertions
     *          The {@link AbstractAssertion}s to check, not {@code null}.
     *
     * @return
     *           {@code true}, if the {@link AbstractAssertion#expressionRaisesThrowable()} method of at least one of the
     *           given assertions returns {@code true}. {@code false} is returned otherwise.
     */
    private boolean containsThrowingExpression(Collection<AbstractAssertion<?>> assertions) {
        return assertions.stream()
                .anyMatch(AbstractAssertion::expressionRaisesThrowable);
    }

    /**
     *
     * @param assertions
     *          The {@link AbstractAssertion}s to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the {@link AbstractAssertion#expressionRaisesThrowable()} method of every
     *          given assertions returns {@code true}. {@code false} is returned otherwise.
     */
    private boolean allExpressionsRaiseThrowable(Collection<AbstractAssertion<?>> assertions) {
        return assertions.stream()
                .allMatch(AbstractAssertion::expressionRaisesThrowable);
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if at least one following conditions is true
     *          <ul>
     *              <li>the class is not a top level class</li>
     *              <li>the {@link Settings#onlyIncludeAnnotated} flag is set to true and neither the class nor any contained
     *              method is annotated with {@link CalculateCoverage}</li>
     *              <li>the class is not located in the directory matching that of a test class</li>
     *              <li>the class is annotated with {@link IgnoreCoverage}</li>
     *              <li>the class is an anonymous class</li>
     *          </ul>
     */
    private boolean shouldBeExcluded(CtClass<?> clazz) {
        if(!clazz.isTopLevel()) {
            log.debug("Class '{}' is not a top level and will therefore be ignored!", clazz.getQualifiedName());
            return true;
        }

        if(!TypeUtils.isPotentialTestClass(clazz)) {
            log.debug("Class '{}' is not a potential test class since it is not declared in a source file matching " +
                    "the criteria!", clazz.getQualifiedName());
            return true;
        }

        if(AnnotationUtils.isAnnotatedWith(clazz, IgnoreCoverage.class)) {
            log.debug("Class '{}' is annotated with @IgnoreCoverage and will therefore be ignored!",
                    clazz.getQualifiedName());
            return true;
        }

        if(settings.onlyIncludeAnnotated && !AnnotationUtils.isAnnotatedWith(clazz, CalculateCoverage.class) &&
                !AnnotationUtils.childElementAnnotatedWith(clazz, CtTypeMember.class, CalculateCoverage.class) &&
                !AnnotationUtils.isAnnotatedWith(TypeUtils.getEnclosingTypes(clazz), CalculateCoverage.class)) {
            log.debug("Setting to only include @CalculateCoverage annotated types and methods is activated and the " +
                    "given class '{}' nor any method or enclosing type is not annotated and will therefore be ignored!", clazz.getQualifiedName());
            return true;
        }

        // exclude anonymous classes
        if(clazz.isAnonymous()) {
            log.debug("Anonymous class '{}' will be ignored!", clazz.getQualifiedName());
            return true;
        }

        return false;
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to find the test methods in, not {@code null}.
     *
     * @return
     *          A list containing the test methods of the given {@code clazz} which should be included.
     */
    private List<CtMethod<?>> findTestMethods(CtClass<?> clazz) {
        var onlyIncludeAnnotated = settings.onlyIncludeAnnotated;
        var testMethodsInClass = findAllTestMethods(clazz);

        if(!onlyIncludeAnnotated || testMethodsInClass.isEmpty()) {
            return testMethodsInClass;
        }

        var isWholeClassIncluded = AnnotationUtils.isAnnotatedWith(clazz, CalculateCoverage.class);
        if(isWholeClassIncluded) {
            return testMethodsInClass;
        }

        return testMethodsInClass.stream()
                .filter(testMethod -> {
                    if(AnnotationUtils.isAnnotatedWith(testMethod, CalculateCoverage.class)) {
                        return true;
                    }

                    log.info("Setting to only include @CalculateCoverage annotated types and methods is activated and the " +
                            "but neither the class '{}' nor the test method '{}' are annotated!",clazz.getSimpleName(),
                            testMethod.getSignature());
                    return false;
                })
                .collect(Collectors.toList());
    }

    /**
     * Data class to configure an {@link ObjectFieldCoverageProcessor}.
     */
    @Data
    @Builder
    public static class Settings {

        /**
         *
         * @return
         *          Default settings instance with all values set to their default
         *          value.
         */
        public static Settings defaultSettings() {
            return Settings.builder()
                    .build();
        }

        /**
         * Specifies if classes and methods should be ignored which are not annotated with
         * {@link de.adesso.objectfieldcoverage.annotation.CalculateCoverage}.
         */
        private boolean onlyIncludeAnnotated;

    }

}
