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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void findFieldsComparedInEqualsMethodReturnsEmptySetWhenClassDoesNotOverrideEquals(@Mock CtTypeReference classRefMock,
                                                                                       @Mock AccessibleField accessibleFieldMock) {
        // given
        given(classRefMock.isClass()).willReturn(true);

        doReturn(false).when(testSubjectSpy).overridesEquals(classRefMock);

        // when
        var actualResult = testSubjectSpy.findFieldsComparedInEqualsMethod(classRefMock, Set.of(accessibleFieldMock));

        // then
        assertThat(actualResult).isEmpty();

        verify(testSubjectSpy, never()).findFieldsComparedInEqualsMethodInternal(any(), any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsExpectedSetWhenClassOverridesEquals(@Mock CtTypeReference classRefMock,
                                                                                    @Mock AccessibleField accessibleFieldMock) {
        // given
        given(classRefMock.isClass()).willReturn(true);

        doReturn(true).when(testSubjectSpy).overridesEquals(classRefMock);
        doReturn(Set.of(accessibleFieldMock)).when(testSubjectSpy)
                .findFieldsComparedInEqualsMethodInternal(classRefMock, Set.of(accessibleFieldMock));

        // when
        var actualResult = testSubjectSpy.findFieldsComparedInEqualsMethod(classRefMock, Set.of(accessibleFieldMock));

        // then
        assertThat(actualResult).containsExactly(accessibleFieldMock);
    }

    @Test
    void findFieldsComparedInEqualsMethodThrowsExceptionWhenTypeReferenceIsNoClassReference(@Mock CtTypeReference<?> typeRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(false);

        // when / then
        assertThatThrownBy(() -> testSubjectSpy.findFieldsComparedInEqualsMethod(typeRefMock, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given type reference must be a class or enum reference!");
    }

    @Test
    void findFieldsComparedInEqualsMethodThrowsExceptionWhenTypeReferenceIsNoEnumReference(@Mock CtTypeReference<?> typeRefMock) {
        // given
        given(typeRefMock.isEnum()).willReturn(false);

        // when / then
        assertThatThrownBy(() -> testSubjectSpy.findFieldsComparedInEqualsMethod(typeRefMock, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given type reference must be a class or enum reference!");
    }


    @Test
    void callsSuperReturnsTrueWhenClassOverrideEqualsAndCallsSuper(@Mock CtTypeReference<String> classRefMock) {
        // given
        doReturn(true).when(testSubjectSpy).overridesEquals(classRefMock);
        doReturn(true).when(testSubjectSpy).callsSuperInternal(classRefMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classRefMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void callsSuperReturnsFalseWhenClassOverrideEqualsButDoesNotCallsSuper(@Mock CtTypeReference<String> classRefMock) {
        // given
        doReturn(true).when(testSubjectSpy).overridesEquals(classRefMock);
        doReturn(false).when(testSubjectSpy).callsSuperInternal(classRefMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void callsSuperReturnsFalseWhenClassDoesNotOverrideEquals(@Mock CtTypeReference<String> classRefMock) {
        // given
        doReturn(false).when(testSubjectSpy).overridesEquals(classRefMock);

        // when
        var actualResult = testSubjectSpy.callsSuper(classRefMock);

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
