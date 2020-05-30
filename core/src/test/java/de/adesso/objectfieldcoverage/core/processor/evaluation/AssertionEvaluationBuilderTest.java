package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.analyzer.ObjectsEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.analyzer.PrimitiveTypeEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.analyzer.lombok.LombokEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.finder.DirectAccessAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.lombok.LombokAccessibilityAwareFieldFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AssertionEvaluationBuilderIntegrationTest extends AbstractSpoonIntegrationTest {

    private AssertionEvaluationBuilder testSubject;

    @BeforeEach
    void setUp() {
        var fieldFinders = List.of(new DirectAccessAccessibilityAwareFieldFinder(), new JavaBeansAccessibilityAwareFieldFinder(),
                new LombokAccessibilityAwareFieldFinder());
        var equalsMethodAnalyzers = List.of(new PrimitiveTypeEqualsMethodAnalyzer(), new ObjectsEqualsMethodAnalyzer(),
                new LombokEqualsMethodAnalyzer());

        this.testSubject = new AssertionEvaluationBuilder(fieldFinders, equalsMethodAnalyzers);
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildReturnsExpectedResultForPrimitiveAssertion() {
        // given
        var model = buildModel("processor/evaluation/PrimitiveTypeAssertionTest.java");
        var testClass = findClassWithSimpleName(model, "PrimitiveTypeAssertionTest");
        var testMethod = (CtMethod<Boolean>) findMethodWithSimpleName(testClass, "isTestReturnsTrue");
        var assertedExpression = (CtInvocation<Boolean>) testMethod.getElements(new TypeFilter<>(CtInvocation.class))
                .get(1);
        var givenAssertion = new BooleanTypeAssertion(assertedExpression);

        var expectedAssertedTypeRef = new TypeFactory().BOOLEAN_PRIMITIVE;
        var expectedAccessibleFieldGraph = new AccessibleFieldGraph(expectedAssertedTypeRef, testClass.getReference());
        var expectedResult = new AssertionEvaluationInformation(expectedAssertedTypeRef, expectedAccessibleFieldGraph,
                expectedAccessibleFieldGraph, Set.of());

        // when
        var actualResult = testSubject.build(givenAssertion);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    //TODO: add tests for reference type assertions with the following cases
    //  - all compared
    //  - at least one root node not compared
    //  - cyclic accessible field graph and cycle not compared in equals

}
