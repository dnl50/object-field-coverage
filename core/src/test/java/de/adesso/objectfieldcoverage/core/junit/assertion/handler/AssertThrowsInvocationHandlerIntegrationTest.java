package de.adesso.objectfieldcoverage.core.junit.assertion.handler;

import de.adesso.objectfieldcoverage.api.assertion.reference.ThrowableAssertion;
import de.adesso.objectfieldcoverage.core.junit.JUnitVersion;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.assertj.core.api.Assertions.assertThat;

class AssertThrowsInvocationHandlerIntegrationTest extends AbstractSpoonIntegrationTest {

    private CtClass<?> testClass;

    private AssertThrowsInvocationHandler testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new AssertThrowsInvocationHandler();

        var model = buildModel("junit/handler/AssertThrowsInvocationHandlerIntegrationTest.java");
        this.testClass = findClassWithSimpleName(model, "AssertThrowsInvocationHandlerIntegrationTest");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "junit4AssertThrows",
            "junit4AssertThrowsWithMessage"
    })
    void getAssertionReturnsExpectedAssertionForJUnit4AssertThrows(String simpleTestMethodName) {
        // given
        var testMethod = findMethodWithSimpleName(testClass, simpleTestMethodName);
        var assertThrowsInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(0);
        var assertedExpression = testMethod.getElements(new TypeFilter<CtConstructorCall<?>>(CtConstructorCall.class))
                .get(0);

        var expectedAssertion = new ThrowableAssertion<>(assertedExpression, testMethod)
                .setCoversType(true);

        // when
        var actualAssertion = testSubject.getAssertion(assertThrowsInvocation, testMethod, JUnitVersion.FOUR);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

    @Test
    void getAssertionReturnsExpectedAssertionForJUnit5AssertThrows() {
        // given
        var testMethod = findMethodWithSimpleName(testClass, "junit5AssertThrows");
        var assertThrowsInvocation = testMethod.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
                .get(0);
        var assertedExpression = testMethod.getElements(new TypeFilter<CtConstructorCall<?>>(CtConstructorCall.class))
                .get(0);

        var expectedAssertion = new ThrowableAssertion<>(assertedExpression, testMethod)
                .setCoversType(true);

        // when
        var actualAssertion = testSubject.getAssertion(assertThrowsInvocation, testMethod, JUnitVersion.FIVE);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

}
