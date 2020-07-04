package de.adesso.objectfieldcoverage.core.processor.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class InvokedExecutableFilterTest {

    @Test
    @SuppressWarnings("unchecked")
    void testReturnsTrueWhenExecutableIsInvoked(@Mock CtMethod<String> firstMethodMock,
                                                @Mock CtMethod<Integer> secondMethodMock,
                                                @Mock CtInvocation<String> firstInvocationMock,
                                                @Mock CtInvocation<Integer> secondInvocationMock,
                                                @Mock CtExecutableReference<String> firstExecutableRefMock,
                                                @Mock CtExecutableReference<Integer> secondExecutableRefMock,
                                                @Mock CtExecutable<String> firstExecutableMock,
                                                @Mock CtExecutable<Integer> secondExecutableMock) {
        // given
        given(firstMethodMock.getElements(any(TypeFilter.class))).willReturn(List.of(firstInvocationMock));
        given(secondMethodMock.getElements(any(TypeFilter.class))).willReturn(List.of(secondInvocationMock));

        given(firstInvocationMock.getExecutable()).willReturn(firstExecutableRefMock);
        given(firstExecutableRefMock.getDeclaration()).willReturn(firstExecutableMock);
        given(secondInvocationMock.getExecutable()).willReturn(secondExecutableRefMock);
        given(secondExecutableRefMock.getDeclaration()).willReturn(secondExecutableMock);

        var testSubject = new InvokedExecutableFilter(Set.of(firstMethodMock, secondMethodMock));

        // when
        var actualResult = testSubject.test(firstExecutableMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReturnsFalseWhenExecutableIsNotInvoked(@Mock CtMethod<String> firstMethodMock,
                                                    @Mock CtMethod<Integer> secondMethodMock,
                                                    @Mock CtExecutable<String> executableMock) {
        // given
        given(firstMethodMock.getElements(any(TypeFilter.class))).willReturn(List.of());
        given(secondMethodMock.getElements(any(TypeFilter.class))).willReturn(List.of());

        var testSubject = new InvokedExecutableFilter(Set.of(firstMethodMock, secondMethodMock));

        // when
        var actualResult = testSubject.test(executableMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
