package de.adesso.objectfieldcoverage.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessibilityAwareFieldFinderTest {

    private AccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DefaultAccessibilityAwareFieldFinder();
    }

    @Test
    void isPublicFieldReturnsTrueWhenFieldIsDeclaredPublic(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(true);

        // when
        var actualResult = testSubject.isPublicField(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPublicFieldReturnsFalseWhenFieldIsNotDeclaredPublic(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(false);

        // when
        var actualResult = testSubject.isPublicField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isProtectedFieldReturnsTrueWhenFieldIsDeclaredProtected(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isProtected()).willReturn(true);

        // when
        var actualResult = testSubject.isProtectedField(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isProtectedFieldReturnsFalseWhenFieldIsNotDeclaredProtected(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isProtected()).willReturn(false);

        // when
        var actualResult = testSubject.isProtectedField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isPackagePrivateFieldReturnsTrueWhenFieldIsDeclaredPackagePrivate(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(false);
        given(fieldMock.isProtected()).willReturn(false);
        given(fieldMock.isPrivate()).willReturn(false);

        // when
        var actualResult = testSubject.isPackagePrivateField(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPackagePrivateFieldReturnsFalseWhenFieldIsDeclaredPublic(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(true);

        // when
        var actualResult = testSubject.isPackagePrivateField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isPackagePrivateFieldReturnsFalseWhenFieldIsDeclaredProtected(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(false);
        given(fieldMock.isProtected()).willReturn(true);

        // when
        var actualResult = testSubject.isPackagePrivateField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isPackagePrivateFieldReturnsFalseWhenFieldIsDeclaredPrivate(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPublic()).willReturn(false);
        given(fieldMock.isProtected()).willReturn(false);
        given(fieldMock.isPrivate()).willReturn(true);

        // when
        var actualResult = testSubject.isPackagePrivateField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isPrivateFieldReturnsTrueWhenFieldIsDeclaredPrivate(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPrivate()).willReturn(true);

        // when
        var actualResult = testSubject.isPrivateField(fieldMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPrivateFieldReturnsFalseWhenFieldIsNotDeclaredPrivate(@Mock CtField<?> fieldMock) {
        // given
        given(fieldMock.isPrivate()).willReturn(false);

        // when
        var actualResult = testSubject.isPrivateField(fieldMock);

        // then
        assertThat(actualResult).isFalse();
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
    void isInnerClassOfDeclaringTypeReturnsTrueWhenTestClazzIsInnerClassOfDeclaringType(@Mock CtField fieldMock,
                                                                                        @Mock CtClass fieldDeclaringClassMock,
                                                                                        @Mock CtClass testClazzMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(testClazzMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualResult = testSubject.isInnerClassOfDeclaringType(fieldMock, testClazzMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void isInnerClassOfDeclaringTypeReturnsFalseWhenTestClazzIsNoInnerClassOfDeclaringType(@Mock CtField fieldMock,
                                                                                           @Mock CtClass fieldDeclaringClassMock,
                                                                                           @Mock CtClass testClazzMock) {
        // given
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(testClazzMock.getDeclaringType()).willReturn(null);

        // when
        var actualResult = testSubject.isInnerClassOfDeclaringType(fieldMock, testClazzMock);

        // then
        assertThat(actualResult).isFalse();
    }

    private static class DefaultAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

        @Override
        protected boolean isFieldAccessible(CtClass<?> testClazz, CtField<?> field) {
            return true;
        }

    }

}
