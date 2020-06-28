package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.AccessibleFieldGraphBuilder;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationResultTrackerIntegrationTest extends AbstractSpoonIntegrationTest {

    private CtClass<?> testClass;

    private CtModel model;

    private InvocationResultTracker testSubject;

    @BeforeEach
    void setUp() {
        this.model = buildModel("processor/InvocationResultTrackerIntegrationTest.java");
        this.testClass = findClassWithSimpleName(this.model, "InvocationResultTrackerIntegrationTest");
        this.testSubject = new InvocationResultTracker();
    }

    @Test
    void accessesTargetInvocationResultReturnsTrueForSimpleLocalVariableReadContainingResult() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "simpleLocalVariableRead");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(0);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtVariableRead.class))
                .get(0);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void accessesTargetInvocationResultReturnsTrueForMultiStageLocalVariableReadContainingResult() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "multiStageAssignmentLocalVariableRead");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(0);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtVariableRead.class))
                .get(2);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void accessesTargetInvocationResultReturnsForSingleMethodInvocationOnInvocation() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "singleMethodInvocationOnInvocation");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(2);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(1);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void accessesTargetInvocationResultReturnsForMultipleMethodInvocationsOnInvocation() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "multipleMethodInvocationsOnInvocation");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(4);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(1);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void accessesTargetInvocationResultReturnsForMultipleMethodInvocationsAndFieldsAccessesOnInvocation() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "multipleMethodInvocationsAndFieldAccessesOnInvocation");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(2);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtFieldAccess.class))
                .get(0);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void accessesTargetInvocationResultReturnsForMethodInvocationOnLocalVariableContainingResult() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "methodInvocationOnLocalVariableContainingInvocationResult");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(0);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(2);

        // when
        var actualResult = testSubject.accessesTargetInvocationResult(assertedExpression, targetMethodInvocation);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void getPathPrefixForAccessReturnsExpectedPath() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "multipleMethodInvocationsAndFieldAccessesOnInvocation");
        var targetMethodInvocation = testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(2);
        var assertedExpression = testMethod.getElements(new TypeFilter<>(CtFieldAccess.class))
                .get(0);
        var targetMethodReturnType = findClassWithSimpleName(model, "Data");

        var graph = new AccessibleFieldGraphBuilder(List.of(new DirectAccessAccessibilityAwareFieldFinder(), new JavaBeansAccessibilityAwareFieldFinder()), testClass)
                .buildGraph(targetMethodReturnType.getReference());
        var parentFieldNode = graph.getRootNodes().iterator().next();

        var expectedPath = new Path(parentFieldNode, parentFieldNode, parentFieldNode);

        // when
        var actualPath = testSubject.getPathPrefixForAccess(assertedExpression, targetMethodInvocation, graph);

        // then
        assertThat(actualPath).contains(expectedPath);
    }

}
