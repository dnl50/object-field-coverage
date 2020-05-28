package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregatingAccessibilityAwareFieldFinderTest {

    @Mock
    private AccessibilityAwareFieldFinder fieldFinderMock;

    @Mock
    private AccessibilityAwareFieldFinder otherFieldFinderMock;

    private AggregatingAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new AggregatingAccessibilityAwareFieldFinder(List.of(fieldFinderMock, otherFieldFinderMock));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsAggregatedResult(@Mock CtType typeMock,
                                                     @Mock CtClass testClazzMock,
                                                     @Mock CtField fieldMock,
                                                     @Mock CtMethod getterMethodMock) {
        // given
        var expectedResult = new AccessibleField<>(fieldMock, Set.of(getterMethodMock, fieldMock));

        given(fieldFinderMock.isFieldAccessible(testClazzMock, fieldMock)).willReturn(true);
        given(fieldFinderMock.findAccessGrantingElements(testClazzMock, fieldMock)).willReturn(Set.of(fieldMock));

        given(otherFieldFinderMock.isFieldAccessible(testClazzMock, fieldMock)).willReturn(true);
        given(otherFieldFinderMock.findAccessGrantingElements(testClazzMock, fieldMock)).willReturn(Set.of(getterMethodMock));

        setUpTypeMockToReturnFields(typeMock, Set.of(fieldMock));

        // when
        var actualResult = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualResult).containsExactly(expectedResult);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsOmitsFieldFindersWhichDoNotAssumeFieldAccessible(@Mock CtType typeMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtField fieldMock) {
        // given
        var expectedResult = new AccessibleField<>(fieldMock, Set.of(fieldMock));

        given(fieldFinderMock.isFieldAccessible(testClazzMock, fieldMock)).willReturn(true);
        given(fieldFinderMock.findAccessGrantingElements(testClazzMock, fieldMock)).willReturn(Set.of(fieldMock));

        given(otherFieldFinderMock.isFieldAccessible(testClazzMock, fieldMock)).willReturn(false);

        setUpTypeMockToReturnFields(typeMock, Set.of(fieldMock));

        // when
        var actualResult = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        verify(otherFieldFinderMock, never()).findAccessGrantingElements(any(), any());

        assertThat(actualResult).containsExactly(expectedResult);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setUpTypeMockToReturnFields(CtType typeMock, Collection<CtField> fields) {
        var fieldReferences = fields.stream()
                .map(field -> {
                    var fieldReferenceMock = (CtFieldReference<?>) mock(CtFieldReference.class);
                    given(fieldReferenceMock.getFieldDeclaration()).willReturn(field);
                    return fieldReferenceMock;
                })
                .collect(Collectors.toList());

        doReturn(fieldReferences).when(typeMock).getAllFields();
    }

}
