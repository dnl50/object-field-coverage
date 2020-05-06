package de.adesso.objectfieldcoverage.core.finder.lombok;

import lombok.AccessLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LombokGetterMethodGeneratorImplTest {

    private LombokGetterMethodGeneratorImpl testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokGetterMethodGeneratorImpl();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenNoMethodPresent(@Mock CtField fieldMock,
                                                                                         @Mock CtTypeReference fieldTypeRefMock,
                                                                                         @Mock CtType declaringTypeMock) {
        // given
        var givenAccessLevel = AccessLevel.MODULE;
        var givenFieldSimpleName = "test";
        var givenFieldTypeQualifiedName  = "boolean";
        var givenFieldDeclaringTypeQualifiedName = "de.adesso.DeclaringType";
        var expectedGetterMethodName = "isTest";

        given(fieldMock.getSimpleName()).willReturn(givenFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldTypeRefMock);
        given(fieldMock.getDeclaringType()).willReturn(declaringTypeMock);

        given(fieldTypeRefMock.getQualifiedName()).willReturn(givenFieldTypeQualifiedName);
        given(fieldTypeRefMock.isPrimitive()).willReturn(true);

        given(declaringTypeMock.getMethod(fieldTypeRefMock, expectedGetterMethodName)).willReturn(null);
        given(declaringTypeMock.getQualifiedName()).willReturn(givenFieldDeclaringTypeQualifiedName);

        // when
        var actualIsPresent = testSubject.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, givenAccessLevel);

        // then
        assertThat(actualIsPresent).isFalse();
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenMethodWithSameAccessLevelPresent_Public(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PUBLIC, false);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsTrueWhenMethodWithDifferentAccessLevelPresent_Public(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(false);
        given(existingGetterMethodMock.isPrivate()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PUBLIC, true);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenMethodWithSameAccessLevelPresent_Protected(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(false);
        given(existingGetterMethodMock.isProtected()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PROTECTED, false);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsTrueWhenMethodWithDifferentAccessLevelPresent_Protected(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PROTECTED, true);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenMethodWithSameAccessLevelPresent_Module(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(false);
        given(existingGetterMethodMock.isProtected()).willReturn(false);
        given(existingGetterMethodMock.isPrivate()).willReturn(false);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.MODULE, false);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsTrueWhenMethodWithDifferentAccessLevelPresent_Module(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.MODULE, true);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenMethodWithSameAccessLevelPresent_Package(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(false);
        given(existingGetterMethodMock.isProtected()).willReturn(false);
        given(existingGetterMethodMock.isPrivate()).willReturn(false);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PACKAGE, false);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsTrueWhenMethodWithDifferentAccessLevelPresent_Package(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PACKAGE, true);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsFalseWhenMethodWithSameAccessLevelPresent_Private(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(false);
        given(existingGetterMethodMock.isProtected()).willReturn(false);
        given(existingGetterMethodMock.isPrivate()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PRIVATE, false);
    }

    @Test
    void isGetterMethodWithDifferentAccessModifierPresentReturnsTrueWhenMethodWithDifferentAccessLevelPresent_Private(@Mock CtMethod<?> existingGetterMethodMock) {
        // given
        given(existingGetterMethodMock.isPublic()).willReturn(true);

        // when / then
        testIsGetterMethodWithDifferentAccessModifierPresent(existingGetterMethodMock, AccessLevel.PRIVATE, true);
    }

    @SuppressWarnings("unchecked")
    private void testIsGetterMethodWithDifferentAccessModifierPresent(CtMethod<?> existingMethod, AccessLevel accessLevel, boolean expectedResult) {
        // given
        var givenFieldSimpleName = "test";
        var givenFieldTypeQualifiedName  = "de.adesso.Test";
        var givenFieldDeclaringTypeQualifiedName = "de.adesso.DeclaringType";
        var expectedGetterMethodName = "getTest";

        var fieldMock = mock(CtField.class);
        var fieldTypeRefMock = mock(CtTypeReference.class);
        var declaringTypeMock = mock(CtType.class);

        given(fieldMock.getSimpleName()).willReturn(givenFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldTypeRefMock);
        given(fieldMock.getDeclaringType()).willReturn(declaringTypeMock);

        given(fieldTypeRefMock.getQualifiedName()).willReturn(givenFieldTypeQualifiedName);
        given(fieldTypeRefMock.isPrimitive()).willReturn(false);

        given(declaringTypeMock.getMethod(fieldTypeRefMock, expectedGetterMethodName)).willReturn(existingMethod);
        given(declaringTypeMock.getQualifiedName()).willReturn(givenFieldDeclaringTypeQualifiedName);

        // when
        var actualIsPresent = testSubject.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, accessLevel);

        // then
        assertThat(actualIsPresent).isEqualTo(expectedResult);
    }

}
