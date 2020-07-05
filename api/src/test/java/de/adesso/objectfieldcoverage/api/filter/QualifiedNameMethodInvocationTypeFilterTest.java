package de.adesso.objectfieldcoverage.api.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QualifiedNameMethodInvocationTypeFilterTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void matchesReturnsTrueWhenDeclaringTypeQualifiedNameIsContainedInExpected(@Mock CtInvocation<String> invocationMock,
                                                                               @Mock CtExecutableReference<String> methodRefMock,
                                                                               @Mock CtTypeReference targetExpressionTypeRef) {
        // given
        var givenQualifiedTypeName = "java.lang.Object";

        given(invocationMock.getExecutable()).willReturn(methodRefMock);
        given(methodRefMock.isConstructor()).willReturn(false);
        given(methodRefMock.getDeclaringType()).willReturn(targetExpressionTypeRef);
        given(targetExpressionTypeRef.getQualifiedName()).willReturn(givenQualifiedTypeName);

        var testSubject = new QualifiedNameMethodInvocationTypeFilter(givenQualifiedTypeName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void matchesReturnsFalseWhenDeclaringTypeNameIsNotContainedInExpected(@Mock CtInvocation<String> invocationMock,
                                                                          @Mock CtExecutableReference<String> methodRefMock,
                                                                          @Mock CtTypeReference targetExpressionTypeRef) {
        // given
        var givenQualifiedTypeName = "java.lang.Object";
        var methodDeclaringTypeMock = "java.lang.String";

        given(invocationMock.getExecutable()).willReturn(methodRefMock);
        given(methodRefMock.isConstructor()).willReturn(false);
        given(methodRefMock.getDeclaringType()).willReturn(targetExpressionTypeRef);
        given(targetExpressionTypeRef.getQualifiedName()).willReturn(methodDeclaringTypeMock);

        var testSubject = new QualifiedNameMethodInvocationTypeFilter(givenQualifiedTypeName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void matchesReturnsFalseWhenExecutableIsConstructor(@Mock CtInvocation<String> invocationMock,
                                                        @Mock CtExecutableReference<String> constructorRefMock) {
        // given
        var givenQualifiedTypeName = "java.lang.Object";

        given(invocationMock.getExecutable()).willReturn(constructorRefMock);
        given(constructorRefMock.isConstructor()).willReturn(true);

        var testSubject = new QualifiedNameMethodInvocationTypeFilter(givenQualifiedTypeName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
