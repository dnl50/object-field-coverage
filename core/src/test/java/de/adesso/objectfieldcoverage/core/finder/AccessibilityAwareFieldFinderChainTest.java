package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessibilityAwareFieldFinderChainTest {

    @Mock
    private AccessibilityAwareFieldFinder fieldFinderMock;

    @Mock
    private AccessibilityAwareFieldFinder otherFieldFinderMock;

    private AccessibilityAwareFieldFinderChain testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new AccessibilityAwareFieldFinderChain(List.of(fieldFinderMock, otherFieldFinderMock));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsAggregatedResult(@Mock CtTypeReference typeRefMock,
                                                     @Mock CtClass testClazzMock,
                                                     @Mock CtField fieldMock,
                                                     @Mock CtMethod getterMethodMock) {
        // given
        var firstExpectedResult = new AccessibleField(fieldMock, Set.of(getterMethodMock), false);
        var secondExpectedResult = new AccessibleField(fieldMock, Set.of(fieldMock), false);

        var expectedAggregatedResult = new AccessibleField<>(fieldMock, Set.of(getterMethodMock, fieldMock), false);

        given(fieldFinderMock.findAccessibleFields(testClazzMock, typeRefMock)).willReturn(List.of(firstExpectedResult));
        given(fieldFinderMock.callNext(Pair.of(testClazzMock, typeRefMock))).willReturn(true);

        given(otherFieldFinderMock.findAccessibleFields(testClazzMock, typeRefMock)).willReturn(List.of(secondExpectedResult));
        given(otherFieldFinderMock.callNext(Pair.of(testClazzMock, typeRefMock))).willReturn(true);

        // when
        var actualResult = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualResult).containsExactly(expectedAggregatedResult);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotInvokeOtherFieldFinderWhenCallNextReturnsFalse(@Mock CtTypeReference typeRefMock,
                                                                                   @Mock CtClass testClazzMock) {
        // given
        given(fieldFinderMock.findAccessibleFields(testClazzMock, typeRefMock)).willReturn(List.of());
        given(fieldFinderMock.callNext(Pair.of(testClazzMock, typeRefMock))).willReturn(false);

        // when
        var actualResult = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualResult).isEmpty();

        verify(otherFieldFinderMock, never()).findAccessibleFields(any(), any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsThrowsExceptionWhenPseudoFlagsDiffer(@Mock CtTypeReference typeRefMock,
                                                                  @Mock CtClass testClazzMock,
                                                                  @Mock CtField fieldMock,
                                                                  @Mock CtMethod getterMethodMock) {
        // given
        var fieldSimpleName = "fieldMock";

        var firstExpectedResult = new AccessibleField(fieldMock, Set.of(getterMethodMock), false);
        var secondExpectedResult = new AccessibleField(fieldMock, Set.of(fieldMock), true);

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);

        given(fieldFinderMock.findAccessibleFields(testClazzMock, typeRefMock)).willReturn(List.of(firstExpectedResult));
        given(fieldFinderMock.callNext(Pair.of(testClazzMock, typeRefMock))).willReturn(true);

        given(otherFieldFinderMock.findAccessibleFields(testClazzMock, typeRefMock)).willReturn(List.of(secondExpectedResult));
        given(otherFieldFinderMock.callNext(Pair.of(testClazzMock, typeRefMock))).willReturn(true);

        // when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFields(testClazzMock, typeRefMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The pseudo field flag for field '%s' is not consistent!", fieldSimpleName);
    }

}
