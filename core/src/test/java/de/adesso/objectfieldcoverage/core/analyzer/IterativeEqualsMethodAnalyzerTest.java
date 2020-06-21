package de.adesso.objectfieldcoverage.core.analyzer;

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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class IterativeEqualsMethodAnalyzerTest {

    @Mock
    private EqualsMethodAnalyzer equalsMethodAnalyzerMock;

    private IterativeEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new IterativeEqualsMethodAnalyzer(List.of(equalsMethodAnalyzerMock));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findAccessibleFieldsUsedInEqualsThrowsExceptionWhenClassEntryMisses(@Mock CtTypeReference clazzRefMock,
                                                                             @Mock AccessibleField accessibleFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(accessibleFieldMock);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of();

        given(clazzRefMock.isClass()).willReturn(true);

        // when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                        "a required entry for the given class or a superclass!");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsThrowsExceptionWhenSuperclassEntryMisses(@Mock CtTypeReference clazzRefMock,
                                                                                  @Mock CtTypeReference superClassTypeRef,
                                                                                  @Mock AccessibleField accessibleFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(accessibleFieldMock);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of(superClassTypeRef, Set.of(accessibleFieldMock));

        given(clazzRefMock.isClass()).willReturn(true);
        given(clazzRefMock.getSuperclass()).willReturn(superClassTypeRef);

        // when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                        "a required entry for the given class or a superclass!");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsPassesClassToSupportingAnalyzers(@Mock CtTypeReference clazzRefMock,
                                                                          @Mock CtClass clazzMock,
                                                                          @Mock AccessibleField accessibleFieldMock,
                                                                          @Mock AccessibleField externalAccessibleField) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of(
                clazzRefMock, Set.of(accessibleFieldMock)
        );

        given(clazzRefMock.isClass()).willReturn(true);

        given(equalsMethodAnalyzerMock.overridesEquals(clazzRefMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzRefMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of());

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsPassesClassToSupportingAnalyzersReturningAccessibleField(@Mock CtTypeReference clazzRefMock,
                                                                                                  @Mock CtClass clazzMock,
                                                                                                  @Mock AccessibleField accessibleFieldMock,
                                                                                                  @Mock AccessibleField externalAccessibleField,
                                                                                                  @Mock CtField fieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of(
                clazzRefMock, Set.of(accessibleFieldMock)
        );

        given(clazzRefMock.isClass()).willReturn(true);

        given(equalsMethodAnalyzerMock.overridesEquals(clazzRefMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzRefMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(fieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).containsExactly(externalAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsOmitsFieldIfNotAccessible(@Mock CtTypeReference clazzRefMock,
                                                                   @Mock CtClass clazzMock,
                                                                   @Mock AccessibleField accessibleFieldMock,
                                                                   @Mock AccessibleField externalAccessibleField,
                                                                   @Mock CtField fieldMock,
                                                                   @Mock CtField externalFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of(
                clazzRefMock, Set.of(accessibleFieldMock)
        );

        given(clazzRefMock.isClass()).willReturn(true);

        given(equalsMethodAnalyzerMock.overridesEquals(clazzRefMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzRefMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(externalFieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsWalksUpToParentIfSuperIsCalled(@Mock CtTypeReference clazzRefMock,
                                                            @Mock CtClass clazzMock,
                                                            @Mock CtTypeReference superClassRefMock,
                                                            @Mock AccessibleField accessibleFieldMock,
                                                            @Mock AccessibleField externalAccessibleField,
                                                            @Mock CtField fieldMock) {

        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtTypeReference<?>, Set<AccessibleField<?>>>of(
                clazzRefMock, Set.of(),
                superClassRefMock, Set.of(accessibleFieldMock)
        );

        given(clazzRefMock.isClass()).willReturn(true);
        given(clazzRefMock.getSuperclass()).willReturn(superClassRefMock);

        doReturn(true).when(equalsMethodAnalyzerMock).overridesEquals(clazzRefMock);
        doReturn(true).when(equalsMethodAnalyzerMock).overridesEquals(superClassRefMock);

        doReturn(true).when(equalsMethodAnalyzerMock).callsSuper(clazzRefMock);
        doReturn(false).when(equalsMethodAnalyzerMock).callsSuper(superClassRefMock);

        doReturn(Set.of()).when(equalsMethodAnalyzerMock).findFieldsComparedInEqualsMethod(clazzRefMock, Set.of());
        doReturn(Set.of(accessibleFieldMock)).when(equalsMethodAnalyzerMock)
                .findFieldsComparedInEqualsMethod(superClassRefMock, Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(fieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzRefMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).containsExactly(externalAccessibleField);
    }

}
