package de.adesso.objectfieldcoverage.core.junit.assertion.filter;

import org.junit.jupiter.api.BeforeEach;
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
class AbstractJUnitAssertionInvocationFilterTest {

    private AbstractJUnitAssertionInvocationFilter testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DefaultJUnitAssertionInvocationFilter();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void matchesReturnsTrueWhenMethodInvocationExecutableIsStaticAndInAssertClass(@Mock CtInvocation<Void> invocationMock,
                                                                                  @Mock CtExecutableReference<Void> executableRefMock,
                                                                                  @Mock CtTypeReference typeRefMock) {
        // given
        var executableDeclaringQualifiedName = "org.junit.Assert";

        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(true);
        given(executableRefMock.getDeclaringType()).willReturn(typeRefMock);

        given(typeRefMock.getQualifiedName()).willReturn(executableDeclaringQualifiedName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void matchesReturnsTrueWhenMethodInvocationExecutableIsStaticAndInAssertionsClass(@Mock CtInvocation<Void> invocationMock,
                                                                                      @Mock CtExecutableReference<Void> executableRefMock,
                                                                                      @Mock CtTypeReference typeRefMock) {
        // given
        var executableDeclaringQualifiedName = "org.junit.jupiter.api.Assertions";

        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(true);
        given(executableRefMock.getDeclaringType()).willReturn(typeRefMock);

        given(typeRefMock.getQualifiedName()).willReturn(executableDeclaringQualifiedName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void matchesReturnsFalseWhenMethodInvocationExecutableIsNotStatic(@Mock CtInvocation<Void> invocationMock,
                                                                      @Mock CtExecutableReference<Void> executableRefMock) {
        // given
        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(false);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void matchesReturnsFalseWhenMethodInvocationExecutableIsDeclaredInWrongClass(@Mock CtInvocation<Void> invocationMock,
                                                                                 @Mock CtExecutableReference<Void> executableRefMock,
                                                                                 @Mock CtTypeReference typeRefMock) {
        // given
        var executableDeclaringQualifiedName = "no.junit.Class";

        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(true);
        given(executableRefMock.getDeclaringType()).willReturn(typeRefMock);

        given(typeRefMock.getQualifiedName()).willReturn(executableDeclaringQualifiedName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

    private static class DefaultJUnitAssertionInvocationFilter extends AbstractJUnitAssertionInvocationFilter {

        @Override
        protected boolean methodSignatureOfInvocationMatches(CtExecutableReference<Void> executableRef) {
            return true;
        }

    }

}
