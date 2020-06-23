package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.annotation.TestTarget;
import de.adesso.objectfieldcoverage.annotation.TestTargets;
import de.adesso.objectfieldcoverage.core.finder.executable.AnnotationBasedTargetExecutableFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnotationBasedTargetExecutableFinderTest {

    @Mock
    private CtModel modelMock;

    @Mock
    private CtMethod<?> testMethodMock;

    private AnnotationBasedTargetExecutableFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new AnnotationBasedTargetExecutableFinder();
    }

    @Test
    void findTargetExecutablesReturnsEmptySetWhenTestMethodIsNotAnnotated() {
        // given
        doReturn(null).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).isEmpty();
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenTargetExecutableIsNotFound(@Mock TestTarget testTargetMock) {
        // given
        var givenMethodIdentifier = "org.test.Test#test()";

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();
        given(modelMock.getElements(any())).willReturn(List.of());

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).isEmpty();
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenMethodIdentifierIsValidButMethodDoesNotExist(@Mock CtClass<?> targetClassMock,
                                                                                               @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(null);

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListWhenMethodIdentifierReferencesConstructorAndConstructorExists(@Mock CtClass targetClassMock,
                                                                                                                @Mock CtConstructor targetConstructorMock,
                                                                                                                @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "Test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getConstructors()).willReturn(Set.of(targetMethodName));
        given(targetClassMock.getConstructor()).willReturn(targetConstructorMock);

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetConstructorMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListWhenMethodIdentifierReferencesConstructorAndConstructorWithParametersExists(@Mock CtClass targetClassMock,
                                                                                                                              @Mock CtConstructor targetConstructorMock,
                                                                                                                              @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "Test";
        var primitiveTypeName = "int";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName, primitiveTypeName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getConstructor(any())).willAnswer(invocation -> {
            var actualTypeRef = (CtTypeReference<?>) invocation.getArgument(0);

            var argCountMatches = invocation.getArguments().length == 1;
            var typeRefMatches = actualTypeRef.isPrimitive() && !actualTypeRef.isArray() &&
                    primitiveTypeName.equals(actualTypeRef.getQualifiedName());

            return (argCountMatches && typeRefMatches) ? targetConstructorMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetConstructorMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListWhenMethodIdentifierIsValidAndMethodExists(@Mock CtClass targetClassMock,
                                                                                             @Mock CtMethod targetMethodMock,
                                                                                             @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(targetMethodMock);

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                           @Mock CtMethod targetMethod,
                                                                           @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var actualTypeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);
            var typeRefMatches = actualTypeRef.isPrimitive() && !actualTypeRef.isArray() &&
                    primitiveTypeName.equals(actualTypeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForArrayPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                                @Mock CtMethod targetMethodMock,
                                                                                @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "int";
        var dimensions = "[][][][]";
        var expectedDimensionCount = 4;
        var givenMethodIdentifier = String.format("%s#%s(%s%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName, dimensions);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var actualTypeRef = (CtArrayTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);
            var typeRefMatches = primitiveTypeName.equals(actualTypeRef.getArrayType().getQualifiedName()) &&
                    actualTypeRef.getDimensionCount() == expectedDimensionCount;

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethodMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForArrayPrimitiveTypeAndPrimitiveTypeArguments(@Mock CtClass targetClassMock,
                                                                                                 @Mock CtMethod targetMethodMock,
                                                                                                 @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var arrayPrimitiveTypeName = "int";
        var primitiveTypeName = "short";
        var dimensions = "[][]";
        var expectedDimensionCount = 2;
        var givenMethodIdentifier = String.format("%s#%s(%s%s, %s)", targetClassQualifiedName, targetMethodName,
                arrayPrimitiveTypeName, dimensions, primitiveTypeName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var arrayTypeRef = (CtArrayTypeReference<?>) invocation.getArgument(1);
            var primitiveTypeRef = (CtTypeReference<?>) invocation.getArgument(2);

            var argCountMatches = invocation.getArguments().length == 3;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var arrayTypeRefMatches = arrayPrimitiveTypeName.equals(arrayTypeRef.getArrayType().getQualifiedName()) &&
                    arrayTypeRef.getDimensionCount() == expectedDimensionCount;

            var primitiveTypeRefMatches = primitiveTypeRef.isPrimitive() &&
                    primitiveTypeName.equals(primitiveTypeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && arrayTypeRefMatches && primitiveTypeRefMatches) ? targetMethodMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForFullyQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                                 @Mock CtMethod targetMethodMock,
                                                                                 @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = wkClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethodMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForNonQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                               @Mock CtMethod targetMethodMock,
                                                                               @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "Boolean";
        var expectedFullyQualifiedClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = expectedFullyQualifiedClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethodMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListForQualifiedJavaUtilClass(@Mock CtClass targetClassMock,
                                                                            @Mock CtMethod targetMethodMock,
                                                                            @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.util.List";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = qualifiedClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethodMock : null;
        });

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutablesReturnsPopulatedListAndQueriesModelForNonJavaParameter(@Mock CtClass targetClassMock,
                                                                                     @Mock CtType parameterTypeMock,
                                                                                     @Mock CtTypeReference parameterTypeReferenceMock,
                                                                                     @Mock CtMethod targetMethodMock,
                                                                                     @Mock TestTarget testTargetMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "de.test.User";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));
        given(modelMock.getAllTypes()).willReturn(List.of(parameterTypeMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        given(parameterTypeMock.getQualifiedName()).willReturn(qualifiedClassName);
        given(parameterTypeMock.getReference()).willReturn(parameterTypeReferenceMock);

        given(targetClassMock.getMethod(targetMethodName, parameterTypeReferenceMock)).willReturn(targetMethodMock);

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(testMethodMock, List.of());

        // then
        assertThat(actualTargetExecutables).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenModelDoesNotContainType(@Mock CtClass targetClassMock,
                                                                        @Mock TestTarget testTargetMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "de.test.User";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));
        given(modelMock.getAllTypes()).willReturn(List.of());

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutables(testMethodMock, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The model does not contain the type '%s'!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenClassFromJavaPackageNotFound(@Mock CtClass targetClassMock,
                                                                             @Mock TestTarget testTargetMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.lang.UnknownClass123456";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();


        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutables(testMethodMock, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenClassFromJavaPackageNotFoundAfterAddingJavaLangPrefix(@Mock CtClass targetClassMock,
                                                                                                      @Mock TestTarget testTargetMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var unqualifiedClassName = "UnknownClass123456";
        var expectedQualifiedClassName = "java.lang.UnknownClass123456";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                unqualifiedClassName);

        doReturn(testTargetMock).when(testMethodMock).getAnnotation(TestTarget.class);
        doReturn(null).when(testMethodMock).getAnnotation(TestTargets.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);

        this.setUpTestMethodMockToReturnModelMock();

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutables(testMethodMock, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", expectedQualifiedClassName);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenClassNameIsMissing() {
        // given
        var givenMethodIdentifier = "#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenClassNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "Te st#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMethodNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "test.Test#te st()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMethodNameIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenPackageNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "0test.Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenClassNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.0Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMethodNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.Test#0test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMultipleDotsFollowEachOther() {
        // given
        var givenMethodIdentifier = "test..Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenNoRhombPresent() {
        // given
        var givenMethodIdentifier = "test.Test test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMultipleRhombsPresent() {
        // given
        var givenMethodIdentifier = "test.Test#Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterListBracketsAreMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterListClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterListOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterListBracketArePresentMultipleTimes() {
        // given
        var givenMethodIdentifier = "test.Test#test(())";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterDimensionOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterDimensionClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenMultipleCommaFollowEachOther() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,,java.lang.String)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterListEndsWithComma() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutablesThrowsExceptionWhenFormalParameterDimensioBracketsAreNested() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[[]])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    private void assertIllegalArgumentExceptionIsThrown(String givenMethodIdentifier) {
        // given
        this.setUpTestMethodMockToReturnModelMock();

        var testTargetMock = mock(TestTarget.class);
        given(testTargetMock.value()).willReturn(givenMethodIdentifier);
        given(testMethodMock.getAnnotation(TestTarget.class)).willReturn(testTargetMock);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutables(testMethodMock, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Method identifier '%s' is not a valid identifier!", givenMethodIdentifier);
    }

    private void setUpTestMethodMockToReturnModelMock() {
        // when
        var factoryMock = mock(Factory.class);

        given(testMethodMock.getFactory()).willReturn(factoryMock);
        given(factoryMock.getModel()).willReturn(modelMock);
    }

}
