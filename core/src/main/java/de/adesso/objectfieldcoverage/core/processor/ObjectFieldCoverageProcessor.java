package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import de.adesso.objectfieldcoverage.core.annotation.TestTarget;
import de.adesso.objectfieldcoverage.core.annotation.TestTargetPreProcessor;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.lombok.LombokAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnit4TestMethodFinder;
import de.adesso.objectfieldcoverage.core.junit.JUnitJupiterTestMethodFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//TODO: properly implement, currently just a draft implementation

@Slf4j
@RequiredArgsConstructor
public class ObjectFieldCoverageProcessor extends AbstractProcessor<CtClass<?>> {

    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    private final List<TestMethodFinder> testMethodFinders;

    private final TestTargetPreProcessor testTargetPreProcessor;

    private final TargetMethodFinder targetMethodFinder;

    //TODO: use Java ServiceLoader
    public ObjectFieldCoverageProcessor() {
        this.fieldFinders = List.of(new LombokAccessibilityAwareFieldFinder(), new DirectAccessAccessibilityAwareFieldFinder(),
                new JavaBeansAccessibilityAwareFieldFinder());
        this.testMethodFinders = List.of(new JUnitJupiterTestMethodFinder(), new JUnit4TestMethodFinder());
        this.testTargetPreProcessor = new TestTargetPreProcessor();
        this.targetMethodFinder = new TargetMethodFinder();
    }

    @Override
    public void init() {
        super.init();
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



        log.info("Finished processing of test method '{}'!", testMethod.getSimpleName());
    }

    /**
     *
     * @param testMethod
     *          The test method to find the target methods of, not {@code null}. Must be annotated
     *          with either {@link TestTarget} or {@link de.adesso.objectfieldcoverage.core.annotation.TestTargets}.
     *
     * @return
     *          A list containing the target methods which are specified using the method identifiers
     *          given in the annotation(s).
     */
    private List<CtMethod<?>> findTargetMethods(CtMethod<?> testMethod) {
        var underlyingModel = this.getFactory().getModel();

        //TODO: implement


        var testTargetAnnotation = testMethod.getAnnotation(TestTarget.class);
        if(Objects.nonNull(testTargetAnnotation)) {
            var methodIdentifier = testTargetAnnotation.value();
            var exceptionExpected = testTargetAnnotation.exceptionExpected();


        }

        return List.of();
    }

}
