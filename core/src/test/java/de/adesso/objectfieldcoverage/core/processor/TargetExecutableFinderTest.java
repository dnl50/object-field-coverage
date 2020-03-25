package de.adesso.objectfieldcoverage.core.processor;

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
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TargetExecutableFinderTest {

    @Mock
    private CtModel modelMock;

    private TargetExecutableFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TargetExecutableFinder();
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenMethodIdentifierIsValidButClassDoesNotExist() {
        // given
        var givenMethodIdentifier = "org.test.Test#test()";

        given(modelMock.getElements(any())).willReturn(List.of());

        // when
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenMethodIdentifierIsValidButMethodDoesNotExist(@Mock CtClass<?> targetClassMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(null);

        // when
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalWhenMethodIdentifierReferencesConstructorAndConstructorExists(@Mock CtClass targetClassMock,
                                                                                                                   @Mock CtConstructor targetConstructorMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "Test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getConstructors()).willReturn(Set.of(targetMethodName));
        given(targetClassMock.getConstructor()).willReturn(targetConstructorMock);

        // when
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetConstructorMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalWhenMethodIdentifierReferencesConstructorAndConstructorWithParametersExists(@Mock CtClass targetClassMock,
                                                                                                                                 @Mock CtConstructor targetConstructorMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "Test";
        var primitiveTypeName = "int";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName, primitiveTypeName);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetConstructorMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalWhenMethodIdentifierIsValidAndMethodExists(@Mock CtClass targetClassMock,
                                                                                                @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(targetMethodMock);

        // when
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                              @Mock CtMethod targetMethod) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForArrayPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                                   @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "int";
        var dimensions = "[][][][]";
        var expectedDimensionCount = 4;
        var givenMethodIdentifier = String.format("%s#%s(%s%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName, dimensions);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForArrayPrimitiveTypeAndPrimitiveTypeArguments(@Mock CtClass targetClassMock,
                                                                                                    @Mock CtMethod targetMethodMock) {
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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForFullyQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                                    @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForNonQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                                  @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "Boolean";
        var expectedFullyQualifiedClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalForQualifiedJavaUtilClass(@Mock CtClass targetClassMock,
                                                                               @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.util.List";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

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
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetExecutableReturnsPopulatedOptionalAndQueriesModelForNonJavaParameter(@Mock CtClass targetClassMock,
                                                                                        @Mock CtType parameterTypeMock,
                                                                                        @Mock CtTypeReference parameterTypeReferenceMock,
                                                                                        @Mock CtMethod targetMethodMock) {
        // given
        var targetClassSimpleName = "Test";
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "de.test.User";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));
        given(modelMock.getAllTypes()).willReturn(List.of(parameterTypeMock));

        given(targetClassMock.getSimpleName()).willReturn(targetClassSimpleName);
        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        given(parameterTypeMock.getQualifiedName()).willReturn(qualifiedClassName);
        given(parameterTypeMock.getReference()).willReturn(parameterTypeReferenceMock);

        given(targetClassMock.getMethod(targetMethodName, parameterTypeReferenceMock)).willReturn(targetMethodMock);

        // when
        var actualTargetMethodOptional = testSubject.findTargetExecutable(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethodMock);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenModelDoesNotContainType(@Mock CtClass targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "de.test.User";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));
        given(modelMock.getAllTypes()).willReturn(List.of());

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutable(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The model does not contain the type '%s'!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenClassFromJavaPackageNotFound(@Mock CtClass targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.lang.UnknownClass123456";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutable(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetExecutableThrowsExceptionWhenClassFromJavaPackageNotFoundAfterAddingJavaLangPrefix(@Mock CtClass targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var unqualifiedClassName = "UnknownClass123456";
        var expectedQualifiedClassName = "java.lang.UnknownClass123456";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                unqualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutable(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", expectedQualifiedClassName);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenClassNameIsMissing() {
        // given
        var givenMethodIdentifier = "#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenClassNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "Te st#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMethodNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "test.Test#te st()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMethodNameIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenPackageNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "0test.Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenClassNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.0Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMethodNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.Test#0test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMultipleDotsFollowEachOther() {
        // given
        var givenMethodIdentifier = "test..Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenNoRhombPresent() {
        // given
        var givenMethodIdentifier = "test.Test test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMultipleRhombsPresent() {
        // given
        var givenMethodIdentifier = "test.Test#Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterListBracketsAreMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterListClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterListOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterListBracketArePresentMultipleTimes() {
        // given
        var givenMethodIdentifier = "test.Test#test(())";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterDimensionOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterDimensionClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenMultipleCommaFollowEachOther() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,,java.lang.String)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterListEndsWithComma() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenFormalParameterDimensioBracketsAreNested() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[[]])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    private void assertIllegalArgumentExceptionIsThrown(String givenMethodIdentifier) {
        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutable(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given method identifier '%s' is not a valid identifier!", givenMethodIdentifier);
    }

}
