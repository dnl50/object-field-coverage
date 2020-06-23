package de.adesso.objectfieldcoverage.core.processor.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExecutableInvocationTypeFilterTest {

    @Test
    void matchesReturnsTrueWhenGivenInvocationIsInvocationOfGivenExecutable(@Mock CtExecutable<String> executableMock,
                                                                            @Mock CtExecutableReference<String> executableRefMock,
                                                                            @Mock CtInvocation<String> invocationMock) {
        // given
        given(executableMock.getReference()).willReturn(executableRefMock);
        given(invocationMock.getExecutable()).willReturn(executableRefMock);

        var testSubject = new ExecutableInvocationTypeFilter<>(executableMock);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void matchesReturnsfalseWhenGivenInvocationIsInvocationOfOtherExecutable(@Mock CtExecutable<String> executableMock,
                                                                             @Mock CtExecutableReference<String> executableRefMock,
                                                                             @Mock CtInvocation<String> invocationMock,
                                                                             @Mock CtExecutableReference<String> invocationExecutableRefMock) {
        // given
        given(executableMock.getReference()).willReturn(executableRefMock);
        given(invocationMock.getExecutable()).willReturn(invocationExecutableRefMock);

        var testSubject = new ExecutableInvocationTypeFilter<>(executableMock);

        // when
        var actualResult = testSubject.matches(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
