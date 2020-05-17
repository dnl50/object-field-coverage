package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import de.adesso.objectfieldcoverage.core.annotation.TestTargetPreProcessor;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.lombok.LombokAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnit4TestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnitJupiterTestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
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

//TODO: properly implement, currently just a draft implementation

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final List<AssertionFinder> assertionFinders;

    private final TestTargetPreProcessor testTargetPreProcessor;

    //TODO: use Java ServiceLoader
    public ObjectFieldCoverageProcessor() {
        this.fieldFinders = List.of(new LombokAccessibilityAwareFieldFinder(), new DirectAccessAccessibilityAwareFieldFinder(),
                new JavaBeansAccessibilityAwareFieldFinder());
        this.testMethodFinders = List.of(new JUnitJupiterTestMethodFinder(), new JUnit4TestMethodFinder());
        this.assertionFinders = List.of(new JUnitAssertionFinder());
        this.testTargetPreProcessor = new TestTargetPreProcessor();
    }

    @Override
    public void process(CtClass<?> clazz) {
        var testMethodsInClass = testMethodFinders.stream()
                .map(finder -> finder.findTestMethods(clazz))
                .flatMap(List::stream)
                .map(testTargetPreProcessor::addTestTargetAnnotation)
                .collect(Collectors.toList());

        if(testMethodsInClass.isEmpty()) {
            log.info("No test methods in class '{}'!", clazz.getQualifiedName());
            return;
        }

        testMethodsInClass.forEach(testMethod -> processTestMethod(testMethod, clazz));
    }

    private void processTestMethod(CtMethod<?> testMethod, CtClass<?> testClazz) {
        log.info("Starting processing of test method '{}'!", testMethod.getSimpleName());

//        var treeBuilder = new EvaluationTreeBuilder(fieldFinders);
//        var targetExecutables = ExecutableUtil.findTargetExecutables(testMethod, getFactory().getModel());
//
//        // assertion -> evaluation information map
//        var assertionMap = assertionFinders.stream()
//                .map(assertionFinder -> assertionFinder.findAssertions(testMethod))
//                .flatMap(List::stream)
//                .collect(Collectors.toMap(
//                        Function.identity(),
//                        assertion -> treeBuilder.buildEvaluationInformation(assertion.getAssertedExpression().getType(), testClazz)
//                ));
//
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
     *          A set containing all executables
     */
    private Set<CtExecutable<?>> filterExecutables(CtMethod<?> testMethod, Collection<CtExecutable<?>> executables) {
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

}
