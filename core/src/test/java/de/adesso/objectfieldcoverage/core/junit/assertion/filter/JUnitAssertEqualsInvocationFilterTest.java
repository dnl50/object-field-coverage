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
class JUnitAssertEqualsInvocationFilterTest {

    private JUnitAssertEqualsInvocationFilter testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JUnitAssertEqualsInvocationFilter();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void matchesReturnsTrueWhenMethodInvocationExecutableSimpleNameMatches(@Mock CtInvocation<Void> invocationMock,
                                                                           @Mock CtExecutableReference<Void> executableRefMock,
                                                                           @Mock CtTypeReference typeRefMock) {
        // given
        var executableDeclaringQualifiedName = "org.junit.jupiter.api.Assertions";
        var executableSimpleName = "assertEquals";

        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(true);
        given(executableRefMock.getDeclaringType()).willReturn(typeRefMock);
        given(executableRefMock.getSimpleName()).willReturn(executableSimpleName);

        given(typeRefMock.getQualifiedName()).willReturn(executableDeclaringQualifiedName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void matchesReturnsFalseWhenMethodInvocationExecutableSimpleNameDoesNotMatch(@Mock CtInvocation<Void> invocationMock,
                                                                                 @Mock CtExecutableReference<Void> executableRefMock,
                                                                                 @Mock CtTypeReference typeRefMock) {
        // given
        var executableDeclaringQualifiedName = "org.junit.jupiter.api.Assertions";
        var executableSimpleName = "otherMethod";

        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        given(executableRefMock.isStatic()).willReturn(true);
        given(executableRefMock.getDeclaringType()).willReturn(typeRefMock);
        given(executableRefMock.getSimpleName()).willReturn(executableSimpleName);

        given(typeRefMock.getQualifiedName()).willReturn(executableDeclaringQualifiedName);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
