package de.adesso.objectfieldcoverage.api;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccessibilityAwareFieldFinderTest {

    private AccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DefaultAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsOmitsTransientAndCompileTimeConstantFields(@Mock CtType accessingTypeMock,
                                                                        @Mock CtTypeReference typeRefMock,
                                                                        @Mock CtField transientFieldMock,
                                                                        @Mock CtField staticFinalFieldMock,
                                                                        @Mock CtField finalFieldMock) {
        // given
        given(transientFieldMock.getModifiers()).willReturn(Set.of(ModifierKind.TRANSIENT));
        given(staticFinalFieldMock.getModifiers()).willReturn(Set.of(ModifierKind.STATIC, ModifierKind.FINAL));
        given(finalFieldMock.getModifiers()).willReturn(Set.of(ModifierKind.FINAL));

        setUpTypeRefMockToReturnFields(typeRefMock, Set.of(transientFieldMock, staticFinalFieldMock, finalFieldMock));

        var expectedAccessibleField = new AccessibleField<>(finalFieldMock, Set.of());

        // when
        var accessibleFields = testSubject.findAccessibleFields(accessingTypeMock, typeRefMock);

        // then
        assertThat(accessibleFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isRealSubClassOfDeclaringClassReturnsTrueWhenDeclaringTypeIsSuperClass(@Mock CtField fieldMock,
                                                                                @Mock CtClass fieldDeclaringClassMock,
                                                                                @Mock CtTypeReference fieldDeclaringClassRefMock,
                                                                                @Mock CtClass testClazz) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(testClazz.getSuperclass()).willReturn(fieldDeclaringClassRefMock);
        given(fieldDeclaringClassRefMock.getTypeDeclaration()).willReturn(fieldDeclaringClassMock);

        // when
        var actualResult = testSubject.isRealSubClassOfDeclaringClass(fieldMock, testClazz);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isRealSubClassOfDeclaringClassReturnsFalseWhenDeclaringTypeIsNoSuperClass(@Mock CtField fieldMock,
                                                                                   @Mock CtClass fieldDeclaringClassMock,
                                                                                   @Mock CtClass testClazzMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(testClazzMock.getSuperclass()).willReturn(null);

        // when
        var actualResult = testSubject.isRealSubClassOfDeclaringClass(fieldMock, testClazzMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isInSamePackageAsDeclaringTypeReturnsTrueWhenInSamePackage(@Mock CtField fieldMock,
                                                                    @Mock CtClass fieldDeclaringClassMock,
                                                                    @Mock CtClass testClazzMock,
                                                                    @Mock CtPackage packageMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualResult = testSubject.isInSamePackageAsDeclaringType(fieldMock, testClazzMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isInSamePackageAsDeclaringTypeReturnsFalseWhenDifferentSamePackage(@Mock CtField fieldMock,
                                                                            @Mock CtClass fieldDeclaringClassMock,
                                                                            @Mock CtClass testClazzMock,
                                                                            @Mock CtPackage fieldPackageMock,
                                                                            @Mock CtPackage testClazzPackageMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldPackageMock);
        given(testClazzMock.getPackage()).willReturn(testClazzPackageMock);

        // when
        var actualResult = testSubject.isInSamePackageAsDeclaringType(fieldMock, testClazzMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isAccessibleAccordingToJlsReturnsTrueWhenAccessingTypeAndMemberHaveSameTopLevelType(@Mock CtField fieldMock,
                                                                                             @Mock CtClass fieldDeclaringClassMock) {
        // given
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(fieldDeclaringClassMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualResult = testSubject.isAccessibleAccordingToJls(fieldDeclaringClassMock, fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isAccessibleAccordingToJlsReturnsFalseWhenDeclaringTypeNotAccessible(@Mock CtField fieldMock,
                                                                              @Mock CtClass fieldDeclaringClassMock,
                                                                              @Mock CtClass fieldDeclaringClassTopLevelTypeMock,
                                                                              @Mock CtType accessingType,
                                                                              @Mock CtPackage accessingTypePackageMock,
                                                                              @Mock CtPackage fieldDeclaringClassPackageMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringClassTopLevelTypeMock);
        given(fieldDeclaringClassMock.isProtected()).willReturn(true);
        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldDeclaringClassPackageMock);

        given(accessingType.getTopLevelType()).willReturn(accessingType);
        given(accessingType.getPackage()).willReturn(accessingTypePackageMock);

        // when
        var actualResult = testSubject.isAccessibleAccordingToJls(accessingType, fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isAccessibleAccordingToJlsReturnsFalseWhenMemberPrivateAndDifferentTopLevelTypes(@Mock CtField fieldMock,
                                                                                          @Mock CtClass fieldDeclaringClassMock,
                                                                                          @Mock CtType accessingType) {
        // given
        given(fieldMock.isPrivate()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(fieldDeclaringClassMock.isPublic()).willReturn(true);

        given(accessingType.getTopLevelType()).willReturn(accessingType);

        // when
        var actualResult = testSubject.isAccessibleAccordingToJls(accessingType, fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isAccessibleAccordingToJlsReturnsFalseWhenMemberPackagePrivateAndNotInSamePackage(@Mock CtField fieldMock,
                                                                                           @Mock CtClass fieldDeclaringClassMock,
                                                                                           @Mock CtType accessingType,
                                                                                           @Mock CtPackage accessingTypePackageMock,
                                                                                           @Mock CtPackage fieldDeclaringClassPackageMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldDeclaringClassPackageMock);

        given(accessingType.getTopLevelType()).willReturn(accessingType);
        given(accessingType.getPackage()).willReturn(accessingTypePackageMock);

        // when
        var actualResult = testSubject.isAccessibleAccordingToJls(accessingType, fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isAccessibleAccordingToJlsReturnsFalseWhenMemberProtectedAndNotInSamePackage(@Mock CtField fieldMock,
                                                                                      @Mock CtClass fieldDeclaringClassMock,
                                                                                      @Mock CtType accessingType,
                                                                                      @Mock CtPackage accessingTypePackageMock,
                                                                                      @Mock CtPackage fieldDeclaringClassPackageMock) {
        // given
        given(fieldMock.isProtected()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldDeclaringClassPackageMock);

        given(accessingType.getTopLevelType()).willReturn(accessingType);
        given(accessingType.getPackage()).willReturn(accessingTypePackageMock);

        // when
        var actualResult = testSubject.isAccessibleAccordingToJls(accessingType, fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isTransientReturnsTrueWhenTransientFieldModifierIsPresent(@Mock CtField<String> fieldMock) {
        // given
        given(fieldMock.getModifiers()).willReturn(Set.of(ModifierKind.TRANSIENT, ModifierKind.PUBLIC));

        // when
        var actualResult = testSubject.isTransient(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isTransientReturnsFalseWhenTransientFieldModifierIsNotPresent(@Mock CtField<String> fieldMock) {
        // given
        given(fieldMock.getModifiers()).willReturn(Set.of(ModifierKind.STATIC, ModifierKind.PUBLIC));

        // when
        var actualResult = testSubject.isTransient(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isCompileTimeConstantReturnsTrueWhenStaticAndFinalModifiersPresent(@Mock CtField<String> fieldMock) {
        // given
        given(fieldMock.getModifiers()).willReturn(Set.of(ModifierKind.STATIC, ModifierKind.FINAL));

        // when
        var actualResult = testSubject.isCompileTimeConstant(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isCompileTimeConstantReturnsFalseWhenStaticModifierIsNotPresent(@Mock CtField<String> fieldMock) {
        // given
        given(fieldMock.getModifiers()).willReturn(Set.of(ModifierKind.FINAL));

        // when
        var actualResult = testSubject.isCompileTimeConstant(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isCompileTimeConstantReturnsFalseWhenFinalModifierIsNotPresent(@Mock CtField<String> fieldMock) {
        // given
        given(fieldMock.getModifiers()).willReturn(Set.of(ModifierKind.STATIC));

        // when
        var actualResult = testSubject.isCompileTimeConstant(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setUpTypeRefMockToReturnFields(CtTypeReference<?> typeRefMock, Set<CtField<?>> fieldMocks) {
       Set fieldRefMocks = fieldMocks.stream()
                .map(fieldMock -> {
                    var fieldRefMock = mock(CtFieldReference.class);
                    given(fieldRefMock.getFieldDeclaration()).willReturn(fieldMock);

                    return fieldRefMock;
                })
                .collect(Collectors.toSet());

        given(typeRefMock.getAllFields()).willReturn(fieldRefMocks);
    }

    private static class DefaultAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

        @Override
        public boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field) {
            return true;
        }

        @Override
        public <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field) {
            return Set.of();
        }

        @Override
        public boolean callNext(Pair<CtType<?>, CtTypeReference<?>> ctTypeCtTypeReferencePair) {
            return false;
        }

    }

}
