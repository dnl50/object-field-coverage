package de.adesso.objectfieldcoverage.core.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TargetMethodFinderTest {

    @Mock
    private CtModel modelMock;

    private TargetMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TargetMethodFinder();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodIdentifierIsValidButClassDoesNotExist() {
        // given
        var givenMethodIdentifier = "org.test.Test#test()";

        given(modelMock.getElements(any())).willReturn(List.of());

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodIdentifierIsValidButMethodDoesNotExist(@Mock CtClass<?> targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(null);

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalWhenMethodIdentifierIsValidAndMethodExists(@Mock CtClass targetClassMock,
                                                                                            @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var givenMethodIdentifier = String.format("%s#%s()", targetClassQualifiedName, targetMethodName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(targetMethodName)).willReturn(targetMethod);

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                          @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

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
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForArrayPrimitiveTypeArgument(@Mock CtClass targetClassMock,
                                                                               @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var primitiveTypeName = "int";
        var dimensions = "[][][][]";
        var expectedDimensionCount = 4;
        var givenMethodIdentifier = String.format("%s#%s(%s%s)", targetClassQualifiedName, targetMethodName,
                primitiveTypeName, dimensions);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var actualTypeRef = (CtArrayTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);
            var typeRefMatches = primitiveTypeName.equals(actualTypeRef.getArrayType().getQualifiedName()) &&
                    actualTypeRef.getDimensionCount() == expectedDimensionCount;

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForArrayPrimitiveTypeAndPrimitiveTypeArguments(@Mock CtClass targetClassMock,
                                                                                                @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var arrayPrimitiveTypeName = "int";
        var primitiveTypeName = "short";
        var dimensions = "[][]";
        var expectedDimensionCount = 2;
        var givenMethodIdentifier = String.format("%s#%s(%s%s, %s)", targetClassQualifiedName, targetMethodName,
                arrayPrimitiveTypeName, dimensions, primitiveTypeName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

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

            return (argCountMatches && methodNameEquals && arrayTypeRefMatches && primitiveTypeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForFullyQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                                @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = wkClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForNonQualifiedJavaLangClass(@Mock CtClass targetClassMock,
                                                                              @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var wkClassName = "Boolean";
        var expectedFullyQualifiedClassName = "java.lang.Boolean";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                wkClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = expectedFullyQualifiedClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalForQualifiedJavaUtilClass(@Mock CtClass targetClassMock,
                                                                              @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.util.List";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);
        given(targetClassMock.getMethod(eq(targetMethodName), any())).willAnswer(invocation -> {
            var actualMethodName = invocation.getArgument(0);
            var typeRef = (CtTypeReference<?>) invocation.getArgument(1);

            var argCountMatches = invocation.getArguments().length == 2;
            var methodNameEquals = targetMethodName.equals(actualMethodName);

            var typeRefMatches = qualifiedClassName.equals(typeRef.getQualifiedName());

            return (argCountMatches && methodNameEquals && typeRefMatches) ? targetMethod : null;
        });

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);

        verify(modelMock, never()).getAllTypes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findTargetMethodReturnsPopulatedOptionalAndQueriesModelForNonJavaParameter(@Mock CtClass targetClassMock,
                                                                                    @Mock CtType parameterTypeMock,
                                                                                    @Mock CtTypeReference parameterTypeReferenceMock,
                                                                                    @Mock CtMethod targetMethod) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "de.test.User";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));
        given(modelMock.getAllTypes()).willReturn(List.of(parameterTypeMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        given(parameterTypeMock.getQualifiedName()).willReturn(qualifiedClassName);
        given(parameterTypeMock.getReference()).willReturn(parameterTypeReferenceMock);

        given(targetClassMock.getMethod(targetMethodName, parameterTypeReferenceMock)).willReturn(targetMethod);

        // when
        var actualTargetMethodOptional = testSubject.findTargetMethod(givenMethodIdentifier, modelMock);

        // then
        assertThat(actualTargetMethodOptional).contains(targetMethod);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetMethodThrowsExceptionWhenModelDoesNotContainType(@Mock CtClass targetClassMock) {
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
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, modelMock))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("The model does not contain the type '%s'!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetMethodThrowsExceptionWhenClassFromJavaPackageNotFound(@Mock CtClass targetClassMock) {
        // given
        var targetClassQualifiedName = "org.test.Test";
        var targetMethodName = "test";
        var qualifiedClassName = "java.lang.UnknownClass123456";
        var givenMethodIdentifier = String.format("%s#%s(%s)", targetClassQualifiedName, targetMethodName,
                qualifiedClassName);

        given(modelMock.getElements(any())).willReturn(List.of(targetClassMock));

        given(targetClassMock.getQualifiedName()).willReturn(targetClassQualifiedName);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", qualifiedClassName);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findTargetMethodThrowsExceptionWhenClassFromJavaPackageNotFoundAfterAddingJavaLangPrefix(@Mock CtClass targetClassMock) {
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
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Class '%s' was not found!", expectedQualifiedClassName);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameIsMissing() {
        // given
        var givenMethodIdentifier = "#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "Te st#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameContainsSpaces() {
        // given
        var givenMethodIdentifier = "test.Test#te st()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenPackageNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "0test.Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenClassNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.0Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMethodNameStartsWithDigit() {
        // given
        var givenMethodIdentifier = "test.Test#0test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleDotsFollowEachOther() {
        // given
        var givenMethodIdentifier = "test..Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenNoRhombPresent() {
        // given
        var givenMethodIdentifier = "test.Test test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleRhombsPresent() {
        // given
        var givenMethodIdentifier = "test.Test#Test#test()";

        // when
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListBracketsAreMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListBracketArePresentMultipleTimes() {
        // given
        var givenMethodIdentifier = "test.Test#test(())";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensionOpeningBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensionClosingBracketIsMissing() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenMultipleCommaFollowEachOther() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,,java.lang.String)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterListEndsWithComma() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String,)";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    @Test
    void findTargetMethodThrowsExceptionWhenFormalParameterDimensioBracketsAreNested() {
        // given
        var givenMethodIdentifier = "test.Test#test(java.lang.String[[]])";

        // when / then
        assertIllegalArgumentExceptionIsThrown(givenMethodIdentifier);
    }

    private void assertIllegalArgumentExceptionIsThrown(String givenMethodIdentifier) {
        // when / then
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, modelMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Given method identifier '%s' is not a valid identifier!", givenMethodIdentifier);
    }

}
