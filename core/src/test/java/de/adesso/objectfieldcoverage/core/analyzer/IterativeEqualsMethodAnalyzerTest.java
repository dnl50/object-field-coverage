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
import spoon.reflect.declaration.CtType;
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
    void findAccessibleFieldsUsedInEqualsThrowsExceptionWhenClassEntryMisses(@Mock CtClass clazzMock,
                                                                             @Mock AccessibleField accessibleFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(accessibleFieldMock);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of();

        // when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                        "a required entry for the given clazz or a superclass!");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsThrowsExceptionWhenSuperclassEntryMisses(@Mock CtClass clazzMock,
                                                                                  @Mock CtTypeReference superClassTypeRef,
                                                                                  @Mock CtClass superClazzMock,
                                                                                  @Mock AccessibleField accessibleFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(accessibleFieldMock);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of(superClazzMock, Set.of(accessibleFieldMock));

        given(clazzMock.getSuperclass()).willReturn(superClassTypeRef);
        given(superClassTypeRef.getDeclaration()).willReturn(superClazzMock);

        // when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                        "a required entry for the given clazz or a superclass!");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsPassesClassToSupportingAnalyzers(@Mock CtClass clazzMock,
                                                                          @Mock AccessibleField accessibleFieldMock,
                                                                          @Mock AccessibleField externalAccessibleField) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of(
                clazzMock, Set.of(accessibleFieldMock)
        );

        given(equalsMethodAnalyzerMock.overridesEquals(clazzMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of());

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsPassesClassToSupportingAnalyzersReturningAccessibleField(@Mock CtClass clazzMock,
                                                                                                  @Mock AccessibleField accessibleFieldMock,
                                                                                                  @Mock AccessibleField externalAccessibleField,
                                                                                                  @Mock CtField fieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of(
                clazzMock, Set.of(accessibleFieldMock)
        );

        given(equalsMethodAnalyzerMock.overridesEquals(clazzMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(fieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).containsExactly(externalAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsUsedInEqualsOmitsFieldIfNotAccessible(@Mock CtClass clazzMock,
                                                                   @Mock AccessibleField accessibleFieldMock,
                                                                   @Mock AccessibleField externalAccessibleField,
                                                                   @Mock CtField fieldMock,
                                                                   @Mock CtField externalFieldMock) {
        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of(
                clazzMock, Set.of(accessibleFieldMock)
        );

        given(equalsMethodAnalyzerMock.overridesEquals(clazzMock)).willReturn(true);
        given(equalsMethodAnalyzerMock.findFieldsComparedInEqualsMethod(clazzMock, Set.of(accessibleFieldMock)))
                .willReturn(Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(externalFieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsWalksUpToParentIfSuperIsCalled(@Mock CtClass clazzMock,
                                                            @Mock CtTypeReference superClassTypeRef,
                                                            @Mock CtClass superClazzMock,
                                                            @Mock AccessibleField accessibleFieldMock,
                                                            @Mock AccessibleField externalAccessibleField,
                                                            @Mock CtField fieldMock) {

        // given
        var givenAccessibleFields = Set.<AccessibleField<?>>of(externalAccessibleField);
        var givenMap = Map.<CtType<?>, Set<AccessibleField<?>>>of(
                clazzMock, Set.of(),
                superClazzMock, Set.of(accessibleFieldMock)
        );

        given(clazzMock.getSuperclass()).willReturn(superClassTypeRef);
        given(superClassTypeRef.getDeclaration()).willReturn(superClazzMock);

        doReturn(true).when(equalsMethodAnalyzerMock).overridesEquals(clazzMock);
        doReturn(true).when(equalsMethodAnalyzerMock).overridesEquals(superClazzMock);

        doReturn(true).when(equalsMethodAnalyzerMock).callsSuper(clazzMock);
        doReturn(false).when(equalsMethodAnalyzerMock).callsSuper(superClazzMock);

        doReturn(Set.of()).when(equalsMethodAnalyzerMock).findFieldsComparedInEqualsMethod(clazzMock, Set.of());
        doReturn(Set.of(accessibleFieldMock)).when(equalsMethodAnalyzerMock)
                .findFieldsComparedInEqualsMethod(superClazzMock, Set.of(accessibleFieldMock));

        given(accessibleFieldMock.getActualField()).willReturn(fieldMock);
        given(externalAccessibleField.getActualField()).willReturn(fieldMock);

        // when
        var actualResult = testSubject.findAccessibleFieldsUsedInEquals(clazzMock, givenAccessibleFields, givenMap);

        // then
        assertThat(actualResult).containsExactly(externalAccessibleField);
    }

}
