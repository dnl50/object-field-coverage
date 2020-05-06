package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutableUtilIntegrationTest extends AbstractSpoonIntegrationTest {

    private CtClass<?> executableUtilTestClass;

    private CtMethod<?> noArgMethodToInvoke;

    private CtMethod<?> singleArgMethodToInvoke;

    @BeforeEach
    void setUp() {
        var spoonModel = buildModel("util/ExecutableUtilTest.java");
        this.executableUtilTestClass = findClassWithSimpleName(spoonModel, "ExecutableUtilTest");
        this.noArgMethodToInvoke = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodToInvoke");
        this.singleArgMethodToInvoke = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodToInvoke");
    }

    @Test
    void isExecutableInvoked_Single_ReturnsTrueWhenMethodWithoutArgsInvokedOnce() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedOnce");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, noArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Single_ReturnsTrueWhenSingleMethodWithoutArgsInvokedTwice() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedTwice");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, noArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Single_ReturnsFalseWhenSingleMethodWithoutArgsNotInvoked() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, noArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isFalse();
    }

    @Test
    void isExecutableInvoked_Single_ReturnsTrueWhenSingleMethodWithArgsInvokedOnce() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedOnce");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, singleArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Single_ReturnsTrueWhenSingleMethodWithArgsInvokedTwice() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedTwice");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, singleArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Single_ReturnsFalseWhenSingleMethodWithArgsNotInvoked() {
        // given
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(executingMethod, singleArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isFalse();
    }

    @Test
    void isExecutableInvoked_Collection_ReturnsTrueWhenMethodsWithoutArgsInvokedAtLeastOnce() {
        // given
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(List.of(firstExecutingMethod, secondExecutingMethod), noArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Collection_ReturnsFalseWhenMethodsWithoutArgsInvokedAtLeastOnce() {
        // given
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(List.of(firstExecutingMethod, secondExecutingMethod), noArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isFalse();
    }

    @Test
    void isExecutableInvoked_Collection_ReturnsTrueWhenMethodsWithArgsInvokedAtLeastOnce() {
        // given
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(List.of(firstExecutingMethod, secondExecutingMethod), singleArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isTrue();
    }

    @Test
    void isExecutableInvoked_Collection_ReturnsFalseWhenMethodsWithArgsInvokedAtLeastOnce() {
        // given
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualIsInvoked = ExecutableUtil.isExecutableInvoked(List.of(firstExecutingMethod, secondExecutingMethod), singleArgMethodToInvoke);

        // then
        assertThat(actualIsInvoked).isFalse();
    }

    @Test
    void countInvocationsOfExecutable_Single_ReturnsZeroWhenMethodWithoutArgsNotInvoked() {
        // given
        var expectedInvocationCount = 0;
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(executingMethod, noArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Single_ReturnsExpectedNumberWhenMethodWithoutArgsInvoked() {
        // given
        var expectedInvocationCount = 2;
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedTwice");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(executingMethod, noArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Single_ReturnsZeroWhenMethodWithArgsNotInvoked() {
        // given
        var expectedInvocationCount = 0;
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(executingMethod, singleArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Single_ReturnsExpectedNumberWhenMethodWithArgsInvoked() {
        // given
        var expectedInvocationCount = 2;
        var executingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedTwice");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(executingMethod, singleArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Collection_ReturnsZeroWhenMethodWithoutArgsNotInvoked() {
        // given
        var expectedInvocationCount = 0;
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedTwice");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(List.of(firstExecutingMethod, secondExecutingMethod),
                noArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Collection_ReturnsExpectedNumberWhenMethodWithoutArgsInvoked() {
        // given
        var expectedInvocationCount = 3;
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedTwice");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(List.of(firstExecutingMethod, secondExecutingMethod),
                noArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Collection_ReturnsZeroWhenMethodWithArgsNotInvoked() {
        // given
        var expectedInvocationCount = 0;
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgAndSingleArgMethodNotInvoked");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "noArgMethodInvokedOnce");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(List.of(firstExecutingMethod, secondExecutingMethod),
                singleArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void countInvocationsOfExecutable_Collection_ReturnsExpectedNumberWhenMethodWithArgsInvoked() {
        // given
        var expectedInvocationCount = 3;
        var firstExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedOnce");
        var secondExecutingMethod = findMethodWithSimpleName(executableUtilTestClass, "singleArgMethodInvokedTwice");

        // when
        var actualInvocationCount = ExecutableUtil.countInvocationsOfExecutable(List.of(firstExecutingMethod, secondExecutingMethod),
                singleArgMethodToInvoke);

        // then
        assertThat(actualInvocationCount).isEqualTo(expectedInvocationCount);
    }

    @Test
    void isVoidExecutableReturnsTrueWhenExecutableIsDeclaredVoid() {
        // given
        var voidMethod = findMethodWithSimpleName(executableUtilTestClass, "voidMethod");

        // when
        var actualResult = ExecutableUtil.isVoidExecutable(voidMethod);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isVoidExecutableReturnsTrueWhenExecutableReturnsVoidType() {
        // given
        var voidTypeMethod = findMethodWithSimpleName(executableUtilTestClass, "voidTypeMethod");

        // when
        var actualResult = ExecutableUtil.isVoidExecutable(voidTypeMethod);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isVoidExecutableReturnsFalseWhenOtherType() {
        // given
        var intPrimitiveMethod = findMethodWithSimpleName(executableUtilTestClass, "intPrimitiveType");

        // when
        var actualResult = ExecutableUtil.isVoidExecutable(intPrimitiveMethod);

        // then
        assertThat(actualResult).isFalse();
    }

}
