package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import de.adesso.objectfieldcoverage.core.annotation.TestTarget;
import de.adesso.objectfieldcoverage.core.annotation.TestTargetPreProcessor;
import de.adesso.objectfieldcoverage.core.annotation.TestTargets;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.lombok.LombokAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnit4TestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnitJupiterTestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.assertion.JUnitAssertionFinder;
import de.adesso.objectfieldcoverage.core.processor.evaluation.EvaluationTreeBuilder;
import de.adesso.objectfieldcoverage.core.processor.exception.IllegalMethodSignatureException;
import de.adesso.objectfieldcoverage.core.processor.exception.TargetMethodNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO: properly implement, currently just a draft implementation

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final List<AssertionFinder> assertionFinders;

    private final TestTargetPreProcessor testTargetPreProcessor;

    private final TargetExecutableFinder targetExecutableFinder;

    //TODO: use Java ServiceLoader
    public ObjectFieldCoverageProcessor() {
        this.fieldFinders = List.of(new LombokAccessibilityAwareFieldFinder(), new DirectAccessAccessibilityAwareFieldFinder(),
                new JavaBeansAccessibilityAwareFieldFinder());
        this.testMethodFinders = List.of(new JUnitJupiterTestMethodFinder(), new JUnit4TestMethodFinder());
        this.assertionFinders = List.of(new JUnitAssertionFinder());
        this.testTargetPreProcessor = new TestTargetPreProcessor();
        this.targetExecutableFinder = new TargetExecutableFinder();
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

        var treeBuilder = new EvaluationTreeBuilder(fieldFinders);
        var targetExecutables = findTargetExecutables(testMethod);

        //TODO: make sure target executables are invoked in test method or sub-method called from
        // test method

        // assertion -> evaluation information map
        var assertionMap = assertionFinders.stream()
                .map(assertionFinder -> assertionFinder.findAssertions(testMethod))
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        Function.identity(),
                        assertion -> treeBuilder.buildEvaluationInformation(assertion.getAssertedExpression().getType(), testClazz)
                ));

        assertionMap.entrySet().forEach(entry -> {
            var assertion = entry.getKey();
            var evaluationInformation = entry.getValue();

            var result = assertion.calculateMetricValue(evaluationInformation);
            log.info("Result: {}", result);
        });

        //TODO:
        // - build assertion evaluation obj for asserted asserted type of each assertion
        //   - primitive types? root = leaf
        //   - String?

        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
    }

    /**
     *
     * @param testMethod
     *          The test method to find the target methods of, not {@code null}. Must be annotated
     *          with either {@link TestTarget} or {@link TestTargets}. The {@link TestTargets}
     *          must must contain at least one {@link TestTarget} annotation in case it is present.
     *
     * @return
     *          A list containing the target executables which are specified using the method identifiers
     *          given in the annotation(s).
     */
    private List<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod) {
        var testTargetAnnotation = testMethod.getAnnotation(TestTarget.class);
        if(Objects.nonNull(testTargetAnnotation)) {
            return List.of(findTargetExecutable(testTargetAnnotation));
        }

        var testTargetsAnnotation = testMethod.getAnnotation(TestTargets.class);
        if(Objects.nonNull(testTargetsAnnotation)) {
            var testTargetAnnotations = testTargetsAnnotation.value();

            if(testTargetAnnotations.length == 0) {
                var exceptionMessage = String.format("@TestTargets annotation on test method %s is empty!",
                        testMethod.getSimpleName());

                log.error(exceptionMessage);
                throw new IllegalArgumentException(exceptionMessage);
            }

            return Arrays.stream(testTargetsAnnotation.value())
                    .map(this::findTargetExecutable)
                    .collect(Collectors.toList());
        }

        var exceptionMessage = String.format("Given test method '%s' neither annotated with @TestTarget nor with @TestTargets",
                testMethod.getSimpleName());
        log.error(exceptionMessage);
        throw new IllegalArgumentException(exceptionMessage);
    }

    /**
     *
     * @param testTargetAnnotation
     *          The annotation containing the method identifier, not {@code null}.
     *
     * @return
     *          The target executable.
     *
     * @throws TargetMethodNotFoundException
     *          When no executable was found in the underlying {@link spoon.reflect.CtModel} using the
     *          given {@code methodIdentifier}.
     *
     * @throws IllegalMethodSignatureException
     *          When the target executable is a <i>void</i> function and the {@code exceptionExpected}
     *          flag of the given {@code testTargetAnnotation} is set to {@code false}.
     */
    private CtExecutable<?> findTargetExecutable(TestTarget testTargetAnnotation) {
        var methodIdentifier = testTargetAnnotation.value();
        var exceptionExpected = testTargetAnnotation.exceptionExpected();

        var underlyingModel = this.getFactory().getModel();
        var targetExecutable = targetExecutableFinder.findTargetExecutable(methodIdentifier, underlyingModel)
                .orElseThrow(() -> new TargetMethodNotFoundException(methodIdentifier));

        if(!exceptionExpected && isVoidExecutable(targetExecutable)) {
            var exceptionMessage = String.format("The target executable '%s' is a void method, but the exceptionExpected flag is set to false!",
                    methodIdentifier);

            log.error(exceptionMessage);
            throw new IllegalMethodSignatureException(exceptionMessage);
        }

        return targetExecutable;
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
    private boolean isVoidExecutable(CtExecutable<?> executable) {
        var typeFactory = new TypeFactory();
        var methodReturnType = executable.getType();

        return typeFactory.VOID_PRIMITIVE.equals(methodReturnType) || typeFactory.VOID.equals(methodReturnType);
    }

}
