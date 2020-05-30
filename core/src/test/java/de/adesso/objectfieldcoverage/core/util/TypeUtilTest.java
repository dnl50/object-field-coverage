package de.adesso.objectfieldcoverage.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TypeUtilTest {

    @Test
    void findExplicitSuperClassesReturnsEmptyListWhenNoSuperClassPresent(@Mock CtType<?> typeMock) {
        // given

        // when
        var actualResult = TypeUtil.findExplicitSuperClasses(typeMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findExplicitSuperClassesExcludesObject(@Mock CtType typeMock,
                                                @Mock CtTypeReference objectClassRefMock,
                                                @Mock CtClass objectClassMock) {
        // given
        given(typeMock.getSuperclass()).willReturn(objectClassRefMock);
        given(objectClassRefMock.getTypeDeclaration()).willReturn(objectClassMock);

        given(objectClassMock.getQualifiedName()).willReturn("java.lang.Object");

        // when
        var actualResult = TypeUtil.findExplicitSuperClasses(typeMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findExplicitSuperClassesExcludesEnum(@Mock CtType typeMock,
                                              @Mock CtTypeReference enumClassRefMock,
                                              @Mock CtClass enumClassMock) {
        // given
        given(typeMock.getSuperclass()).willReturn(enumClassRefMock);
        given(enumClassRefMock.getTypeDeclaration()).willReturn(enumClassMock);

        given(enumClassMock.getQualifiedName()).willReturn("java.lang.Enum");

        // when
        var actualResult = TypeUtil.findExplicitSuperClasses(typeMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findExplicitIncludesSuperClasses(@Mock CtType typeMock,
                                          @Mock CtTypeReference superClassRefMock,
                                          @Mock CtClass superClassMock,
                                          @Mock CtTypeReference superSuperClassRefMock,
                                          @Mock CtClass superSuperClassMock) {
        // given
        given(typeMock.getSuperclass()).willReturn(superClassRefMock);
        given(superClassRefMock.getTypeDeclaration()).willReturn(superClassMock);

        given(superClassMock.getSuperclass()).willReturn(superSuperClassRefMock);
        given(superSuperClassRefMock.getTypeDeclaration()).willReturn(superSuperClassMock);

        // when
        var actualResult = TypeUtil.findExplicitSuperClasses(typeMock);

        // then
        assertThat(actualResult).containsExactly(superClassMock, superSuperClassMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findExplicitSuperClassesIncludingClassReturnsListWithSingleElementWhenNoSuperClassPresent(@Mock CtClass classMock) {
        // given

        // when
        var actualResult = TypeUtil.findExplicitSuperClassesIncludingClass(classMock);

        // then
        assertThat(actualResult).containsExactly(classMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findExplicitSuperClassesIncludingClassIncludesClassItself(@Mock CtClass classMock,
                                                                   @Mock CtTypeReference superClassRefMock,
                                                                   @Mock CtClass superClassMock,
                                                                   @Mock CtTypeReference superSuperClassRefMock,
                                                                   @Mock CtClass superSuperClassMock) {
        // given
        given(classMock.getSuperclass()).willReturn(superClassRefMock);
        given(superClassRefMock.getTypeDeclaration()).willReturn(superClassMock);

        given(superClassMock.getSuperclass()).willReturn(superSuperClassRefMock);
        given(superSuperClassRefMock.getTypeDeclaration()).willReturn(superSuperClassMock);

        // when
        var actualResult = TypeUtil.findExplicitSuperClassesIncludingClass(classMock);

        // then
        assertThat(actualResult).containsExactly(classMock, superClassMock, superSuperClassMock);
    }

}
