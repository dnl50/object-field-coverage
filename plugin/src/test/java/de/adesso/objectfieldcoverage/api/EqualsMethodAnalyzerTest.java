package de.adesso.objectfieldcoverage.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EqualsMethodAnalyzerTest {

    private final TypeFactory TYPE_FACTORY = new TypeFactory();

    private final CtTypeReference<Object> OBJ_TYPE_REF = TYPE_FACTORY.OBJECT;

    private final CtTypeReference<Boolean> BOOLEAN_TYPE_REF = TYPE_FACTORY.BOOLEAN_PRIMITIVE;

    @Spy
    private EqualsMethodAnalyzer testSubjectSpy;

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsEmptySetWhenClassDoesNotOverrideEquals(@Mock CtClass classMock,
                                                                                       @Mock AccessibleField accessibleFieldMock) {
        // given
        doReturn(false).when(testSubjectSpy).overridesEquals(classMock);

        // when
        var actualResult = testSubjectSpy.findFieldsComparedInEqualsMethod(classMock, Set.of(accessibleFieldMock));

        // then
        assertThat(actualResult).isEmpty();

        verify(testSubjectSpy, never()).findFieldsComparedInEqualsMethodInternal(any(), any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsExpectedSetWhenClassOverridesEquals(@Mock CtClass classMock,
                                                                                    @Mock AccessibleField accessibleFieldMock) {
        // given
        doReturn(true).when(testSubjectSpy).overridesEquals(classMock);
        doReturn(Set.of(accessibleFieldMock)).when(testSubjectSpy)
                .findFieldsComparedInEqualsMethodInternal(classMock, Set.of(accessibleFieldMock));

        // when
        var actualResult = testSubjectSpy.findFieldsComparedInEqualsMethod(classMock, Set.of(accessibleFieldMock));

        // then
        assertThat(actualResult).containsExactly(accessibleFieldMock);
    }

    @Test
    void callsSuperReturnsTrueWhenClassOverrideEqualsAndCallsSuper(@Mock CtClass<String> classMock) {
        // given
        doReturn(true).when(testSubjectSpy).overridesEquals(classMock);
        doReturn(true).when(testSubjectSpy).callsSuperInternal(classMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void callsSuperReturnsFalseWhenClassOverrideEqualsButDoesNotCallsSuper(@Mock CtClass<String> classMock) {
        // given
        doReturn(true).when(testSubjectSpy).overridesEquals(classMock);
        doReturn(false).when(testSubjectSpy).callsSuperInternal(classMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void callsSuperReturnsFalseWhenClassDoesNotOverrideEquals(@Mock CtClass<String> classMock) {
        // given
        doReturn(false).when(testSubjectSpy).overridesEquals(classMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classMock);

        // then
        assertThat(actualResult).isFalse();

        verify(testSubjectSpy, never()).callsSuperInternal(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findOverriddenEqualsMethodInClassReturnsPopulatedOptionalWhenEqualsInGivenClass(@Mock CtClass classMock,
                                                                                         @Mock CtMethod equalsMethodMock) {
        // given
        given(classMock.getMethod(BOOLEAN_TYPE_REF, "equals", OBJ_TYPE_REF)).willReturn(equalsMethodMock);
        given(equalsMethodMock.getDeclaringType()).willReturn(classMock);

        // when
        var actualResult = testSubjectSpy.findOverriddenEqualsMethodInClass(classMock);

        // then
        assertThat(actualResult).contains(equalsMethodMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findOverriddenEqualsMethodInClassReturnsEmptyOptionalWhenEqualsMethodNotPresent(@Mock CtClass classMock) {
        // given
        given(classMock.getMethod(BOOLEAN_TYPE_REF, "equals", OBJ_TYPE_REF)).willReturn(null);

        // when
        var actualResult = testSubjectSpy.findOverriddenEqualsMethodInClass(classMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findOverriddenEqualsMethodInClassReturnsEmptyOptionalWhenEqualsNotInGivenClass(@Mock CtClass classMock,
                                                                                        @Mock CtClass equalsMethodDeclaringClassMock,
                                                                                        @Mock CtMethod equalsMethodMock) {
        // given
        given(classMock.getMethod(BOOLEAN_TYPE_REF, "equals", OBJ_TYPE_REF)).willReturn(equalsMethodMock);
        given(equalsMethodMock.getDeclaringType()).willReturn(equalsMethodDeclaringClassMock);

        // when
        var actualResult = testSubjectSpy.findOverriddenEqualsMethodInClass(classMock);

        // then
        assertThat(actualResult).isEmpty();
    }

}
