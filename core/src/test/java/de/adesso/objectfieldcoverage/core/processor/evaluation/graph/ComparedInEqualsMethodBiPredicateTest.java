package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ComparedInEqualsMethodBiPredicateTest {

    @Mock
    private EqualsMethodAnalyzer equalsMethodAnalyzerMock;

    @Mock
    private AccessibilityAwareFieldFinder fieldFinderMock;

    private ComparedInEqualsMethodBiPredicate testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new ComparedInEqualsMethodBiPredicate(List.of(equalsMethodAnalyzerMock), List.of(fieldFinderMock));
    }

    @Test
    void testReturnsTrueWhenGivenFieldIsComparedInEqualsMethod(@Mock CtField<Character[]> fieldMock,
                                                               @Mock CtTypeReference<String> classRefMock,
                                                               @Mock CtClass<String> classMock) {
        // given
        var accessibleField = new AccessibleField<>(fieldMock, fieldMock);

        given(classRefMock.isClass()).willReturn(true);
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        given(fieldFinderMock.findAccessibleFields(classMock, classRefMock)).willReturn(List.of(accessibleField));

        given(equalsMethodAnalyzerMock.overridesEquals(classRefMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.callsSuper(classRefMock)).willReturn(false);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(classRefMock, Set.of(accessibleField)))
                .willReturn(Set.of(accessibleField));

        // when
        var actualResult = testSubject.test(accessibleField, classRefMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void testReturnsFalseWhenGivenFieldIsNotComparedInEqualsMethod(@Mock CtField<Character[]> fieldMock,
                                                                   @Mock CtTypeReference<String> classRefMock,
                                                                   @Mock CtClass<String> classMock) {
        // given
        var accessibleField = new AccessibleField<>(fieldMock, fieldMock);

        given(classRefMock.isClass()).willReturn(true);
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        given(fieldFinderMock.findAccessibleFields(classMock, classRefMock)).willReturn(List.of(accessibleField));

        given(equalsMethodAnalyzerMock.overridesEquals(classRefMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.callsSuper(classRefMock)).willReturn(false);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(classRefMock, Set.of(accessibleField)))
                .willReturn(Set.of());

        // when
        var actualResult = testSubject.test(accessibleField, classRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void testReturnsFalseWhenGivenTypeIsNotAClass(@Mock AccessibleField<Character[]> accessibleFieldMock,
                                                  @Mock CtTypeReference<String> typeRefMock) {
        // given

        // when
        var actualResult = testSubject.test(accessibleFieldMock, typeRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
