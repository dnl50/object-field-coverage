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
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
    void testReturnsTrueWhenGivenTypeIsDeclaredInJavaPackage(@Mock AccessibleField<Character[]> accessibleFieldMock,
                                                             @Mock CtPackage packageMock,
                                                             @Mock CtClass<String> classMock) {
        // given
        var packageQualifiedName = "java";

        given(classMock.getPackage()).willReturn(packageMock);
        given(packageMock.getQualifiedName()).willReturn(packageQualifiedName);

        // when
        var actualResult = testSubject.test(accessibleFieldMock, classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void testReturnsTrueWhenGivenTypeIsDeclaredInJavaSubPackage(@Mock AccessibleField<Character[]> accessibleFieldMock,
                                                                @Mock CtPackage packageMock,
                                                                @Mock CtClass<String> classMock) {
        // given
        var packageQualifiedName = "java.lang";

        given(classMock.getPackage()).willReturn(packageMock);
        given(packageMock.getQualifiedName()).willReturn(packageQualifiedName);

        // when
        var actualResult = testSubject.test(accessibleFieldMock, classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void testReturnsTrueWhenGivenFieldIsComparedInEqualsMethod(@Mock CtField<Character[]> fieldMock,
                                                               @Mock CtPackage packageMock,
                                                               @Mock CtClass<String> classMock) {
        // given
        var packageQualifiedName = "java2";
        var accessibleField = new AccessibleField<>(fieldMock, fieldMock);

        given(classMock.getPackage()).willReturn(packageMock);
        given(packageMock.getQualifiedName()).willReturn(packageQualifiedName);

        setUpTypeMockToReturnFields(classMock, Set.of(fieldMock));

        given(fieldFinderMock.isFieldAccessible(classMock, fieldMock)).willReturn(true);
        given(fieldFinderMock.findAccessGrantingElements(classMock, fieldMock)).willReturn(Set.of(fieldMock));

        given(equalsMethodAnalyzerMock.overridesEquals(classMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.callsSuper(classMock)).willReturn(false);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(classMock, Set.of(accessibleField)))
                .willReturn(Set.of(accessibleField));

        // when
        var actualResult = testSubject.test(accessibleField, classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void testReturnsFalseWhenGivenFieldIsNotComparedInEqualsMethod(@Mock CtField<Character[]> fieldMock,
                                                                   @Mock CtPackage packageMock,
                                                                   @Mock CtClass<String> classMock) {
        // given
        var packageQualifiedName = "java2";
        var accessibleField = new AccessibleField<>(fieldMock, fieldMock);

        given(classMock.getPackage()).willReturn(packageMock);
        given(packageMock.getQualifiedName()).willReturn(packageQualifiedName);

        setUpTypeMockToReturnFields(classMock, Set.of(fieldMock));

        given(fieldFinderMock.isFieldAccessible(classMock, fieldMock)).willReturn(true);
        given(fieldFinderMock.findAccessGrantingElements(classMock, fieldMock)).willReturn(Set.of(fieldMock));

        given(equalsMethodAnalyzerMock.overridesEquals(classMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.callsSuper(classMock)).willReturn(false);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(classMock, Set.of(accessibleField)))
                .willReturn(Set.of());

        // when
        var actualResult = testSubject.test(accessibleField, classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void testReturnsFalseWhenGivenTypeIsNotAClass(@Mock AccessibleField<Character[]> accessibleFieldMock,
                                                  @Mock CtType<String> typeMock) {
        // given

        // when
        var actualResult = testSubject.test(accessibleFieldMock, typeMock);

        // then
        assertThat(actualResult).isFalse();
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
