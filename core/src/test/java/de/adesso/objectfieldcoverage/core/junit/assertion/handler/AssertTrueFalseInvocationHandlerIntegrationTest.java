package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.assertj.core.api.Assertions.assertThat;

class AssertTrueFalseInvocationHandlerIntegrationTest extends AbstractSpoonIntegrationTest {

    private AssertTrueFalseInvocationHandler testSubject;

    private CtClass<?> testClass;

    @BeforeEach
    void setUp() {
        this.testSubject = new AssertTrueFalseInvocationHandler();

        var model = buildModel("junit/handler/AssertTrueFalseTest.java");
        this.testClass = findClassWithSimpleName(model, "AssertTrueFalseTest");
    }

    @ParameterizedTest
    @ValueSource(ints = {
        0, 1, 2, 3
    })
    void supportsReturnsTrueForSupportedJUnit4Methods(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit4assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        // when
        var actualResult = testSubject.supports(givenInvocation, JUnitVersion.FOUR);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0, 1, 2, 3
    })
    void supportsReturnsTrueForSupportedJUnit5Methods(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit5assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        // when
        var actualResult = testSubject.supports(givenInvocation, JUnitVersion.FIVE);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {
            4, 5, 6, 7
    })
    void supportsReturnsFalseForUnsupportedJUnit5Methods(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit5assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        // when
        var actualResult = testSubject.supports(givenInvocation, JUnitVersion.FIVE);

        // then
        assertThat(actualResult).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0, 1, 2, 3
    })
    @SuppressWarnings("unchecked")
    void getAssertionReturnsExpectedBooleanAssertionForJunit5AssertTrueFalse(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit5assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        var expectedExpression = (CtExpression<Boolean>) givenInvocation.getArguments().get(0);
        var expectedAssertion = new PrimitiveTypeAssertion<>(expectedExpression, testMethod, false);

        // when
        var actualAssertion = testSubject.getAssertion(givenInvocation, testMethod, JUnitVersion.FIVE);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0, 2
    })
    @SuppressWarnings("unchecked")
    void getAssertionReturnsExpectedBooleanAssertionForJunit4AssertTrueFalseWithoutMessage(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit4assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        var expectedExpression = (CtExpression<Boolean>) givenInvocation.getArguments().get(0);
        var expectedAssertion = new PrimitiveTypeAssertion<>(expectedExpression, testMethod, false);

        // when
        var actualAssertion = testSubject.getAssertion(givenInvocation, testMethod, JUnitVersion.FOUR);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            1, 3
    })
    @SuppressWarnings("unchecked")
    void getAssertionReturnsExpectedBooleanAssertionForJunit4AssertTrueFalseWithMessage(int invocationIndex) {
        // given
        var testMethod = testClass.getMethod("junit4assertTrueFalse");
        var givenInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(invocationIndex);

        var expectedExpression = (CtExpression<Boolean>) givenInvocation.getArguments().get(1);
        var expectedAssertion = new PrimitiveTypeAssertion<>(expectedExpression, testMethod, false);

        // when
        var actualAssertion = testSubject.getAssertion(givenInvocation, testMethod, JUnitVersion.FOUR);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

}
