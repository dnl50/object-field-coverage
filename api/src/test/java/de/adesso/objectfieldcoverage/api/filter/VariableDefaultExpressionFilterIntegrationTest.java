package de.adesso.objectfieldcoverage.api.filter;

import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.assertj.core.api.Assertions.assertThat;

class VariableDefaultExpressionFilterIntegrationTest extends AbstractSpoonIntegrationTest {

    private CtModel model;

    @BeforeEach
    void setUp() {
        this.model = buildModel("filter/VariableWithDefaultExpressionFilter.java");
    }

    @Test
    void matchesReturnsTrueForVariableWithMatchingDefaultExpression() {
        // given
        var testClass = findClassWithSimpleName(model, "VariableWithDefaultExpressionFilter");
        var testMethod = testClass.getMethod("initializedVariable");
        var givenExpression = testMethod.getElements(new TypeFilter<>(CtExpression.class))
                .get(0);
        var localVariable = testMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                .get(0);

        var testSubject = new VariableWithDefaultExpressionFilter<>(CtLocalVariable.class, givenExpression);

        // when
        var actualResult = testSubject.matches(localVariable);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void matchesReturnsFalseForVariableWithOtherDefaultExpression() {
        // given
        var testClass = findClassWithSimpleName(model, "VariableWithDefaultExpressionFilter");
        var testMethod = testClass.getMethod("initializedVariable");
        var givenExpression = testMethod.getElements(new TypeFilter<>(CtExpression.class))
                .get(1);
        var localVariable = testMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                .get(0);

        var testSubject = new VariableWithDefaultExpressionFilter<>(CtLocalVariable.class, givenExpression);

        // when
        var actualResult = testSubject.matches(localVariable);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void matchesReturnsTrueForVariablesWithoutDefaultExpressionWhenDefaultExpressionIsNull() {
        // given
        var testClass = findClassWithSimpleName(model, "VariableWithDefaultExpressionFilter");
        var testMethod = testClass.getMethod("uninitializedVariable");
        var localVariable = testMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                .get(0);

        var testSubject = new VariableWithDefaultExpressionFilter<>(CtLocalVariable.class, null);

        // when
        var actualResult = testSubject.matches(localVariable);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void matchesReturnsFalseForVariablesWithDefaultExpressionWhenDefaultExpressionIsNull() {
        // given
        var testClass = findClassWithSimpleName(model, "VariableWithDefaultExpressionFilter");
        var testMethod = testClass.getMethod("initializedVariable");
        var localVariable = testMethod.getElements(new TypeFilter<CtLocalVariable<?>>(CtLocalVariable.class))
                .get(0);

        var testSubject = new VariableWithDefaultExpressionFilter<>(CtLocalVariable.class, null);

        // when
        var actualResult = testSubject.matches(localVariable);

        // then
        assertThat(actualResult).isFalse();
    }

}
