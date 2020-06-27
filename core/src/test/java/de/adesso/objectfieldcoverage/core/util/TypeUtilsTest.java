package de.adesso.objectfieldcoverage.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TypeUtilsTest {

    @Test
    void findExplicitSuperClassesReturnsEmptyListWhenNoSuperClassPresent(@Mock CtTypeReference<?> typeRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        // when
        var actualResult = TypeUtils.findExplicitSuperClasses(typeRefMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesExcludesObject(@Mock CtTypeReference typeRefMock,
                                                @Mock CtTypeReference objectClassRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        given(typeRefMock.getSuperclass()).willReturn(objectClassRefMock);
        given(objectClassRefMock.getQualifiedName()).willReturn("java.lang.Object");

        // when
        var actualResult = TypeUtils.findExplicitSuperClasses(typeRefMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesExcludesEnum(@Mock CtTypeReference typeRefMock,
                                              @Mock CtTypeReference enumClassRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        given(typeRefMock.getSuperclass()).willReturn(enumClassRefMock);
        given(enumClassRefMock.getQualifiedName()).willReturn("java.lang.Enum");

        // when
        var actualResult = TypeUtils.findExplicitSuperClasses(typeRefMock);

        // then
        assertThat(actualResult).isEmpty();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesIncludesSuperClasses(@Mock CtTypeReference typeRefMock,
                                                      @Mock CtTypeReference superClassRefMock,
                                                      @Mock CtTypeReference superSuperClassRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        given(typeRefMock.getSuperclass()).willReturn(superClassRefMock);
        given(superClassRefMock.getSuperclass()).willReturn(superSuperClassRefMock);

        // when
        var actualResult = TypeUtils.findExplicitSuperClasses(typeRefMock);

        // then
        assertThat(actualResult).containsExactly(superClassRefMock, superSuperClassRefMock);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesIncludingClassReturnsListWithSingleElementWhenNoSuperClassPresent(@Mock CtTypeReference typeRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        // when
        var actualResult = TypeUtils.findExplicitSuperClassesIncludingClass(typeRefMock);

        // then
        assertThat(actualResult).containsExactly(typeRefMock);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesThrowsExceptionWhenTypeReferenceIsNotAClassReference(@Mock CtTypeReference typeRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(false);

        // when / then
        assertThatThrownBy(() -> TypeUtils.findExplicitSuperClasses(typeRefMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given type reference is not a class reference!");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesIncludingClassIncludesClassItself(@Mock CtTypeReference typeRefMock,
                                                                   @Mock CtTypeReference superClassRefMock,
                                                                   @Mock CtTypeReference superSuperClassRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(true);

        given(typeRefMock.getSuperclass()).willReturn(superClassRefMock);
        given(superClassRefMock.getSuperclass()).willReturn(superSuperClassRefMock);

        // when
        var actualResult = TypeUtils.findExplicitSuperClassesIncludingClass(typeRefMock);

        // then
        assertThat(actualResult).containsExactly(typeRefMock, superClassRefMock, superSuperClassRefMock);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findExplicitSuperClassesIncludingClassThrowsExceptionWhenTypeReferenceIsNotAClassReference(@Mock CtTypeReference typeRefMock) {
        // given
        given(typeRefMock.isClass()).willReturn(false);

        // when / then
        assertThatThrownBy(() -> TypeUtils.findExplicitSuperClassesIncludingClass(typeRefMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given type reference is not a class reference!");
    }

    @Test
    void findAllSuperInterfacesReturnsAllSuperInterfaces() {
        // given
        var typeFactory = new TypeFactory();
        var givenType = typeFactory.createReference(TreeSet.class);

        var expectedSuperInterfaces = Set.of(
                typeFactory.createReference(NavigableSet.class),
                typeFactory.createReference(Cloneable.class),
                typeFactory.createReference(Serializable.class),
                typeFactory.createReference(SortedSet.class),
                typeFactory.createReference(Set.class),
                typeFactory.createReference(Collection.class),
                typeFactory.createReference(Iterable.class)
        );

        // when
        var actualSuperInterfaces = TypeUtils.findAllSuperInterfaces(givenType);

        // then
        assertThat(actualSuperInterfaces).containsExactlyInAnyOrderElementsOf(expectedSuperInterfaces);
    }

    @Test
    void isPotentialTestClassReturnsFalseWhenFileNotPresent(@Mock CtClass<?> classMock,
                                                            @Mock SourcePosition sourcePositionMock) {
        // given
        given(classMock.getPosition()).willReturn(sourcePositionMock);
        given(sourcePositionMock.getFile()).willReturn(null);

        // when
        var actualResult = TypeUtils.isPotentialTestClass(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "C:\\User\\Test\\src\\test\\java\\Test.java", // windows path separators
            "src\\test\\java\\Test.java", // windows path separators
            "/user/test/src/test/java/Test.java", // unix path separator
            "src/test/java/Test.java" // unix path separator
    })
    void isPotentialTestClassReturnsTrueWhenAbsolutePathMatches(String absolutePath, @Mock CtClass<?> classMock,
                                                                @Mock SourcePosition sourcePositionMock, @Mock File fileMock) {
        // given
        given(classMock.getPosition()).willReturn(sourcePositionMock);
        given(sourcePositionMock.getFile()).willReturn(fileMock);
        given(fileMock.getAbsolutePath()).willReturn(absolutePath);

        // when
        var actualResult = TypeUtils.isPotentialTestClass(classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "C:\\User\\Test\\src\\test\\java\\Test", // missing java file ending
            "src\\main\\java\\Test.java", // production class
            "main/java/Test.java" // not in src folder
    })
    void isPotentialTestClassReturnsFalseWhenAbsolutePathDoesNotMatch(String absolutePath, @Mock CtClass<?> classMock,
                                                                      @Mock SourcePosition sourcePositionMock, @Mock File fileMock) {
        // given
        given(classMock.getPosition()).willReturn(sourcePositionMock);
        given(sourcePositionMock.getFile()).willReturn(fileMock);
        given(fileMock.getAbsolutePath()).willReturn(absolutePath);

        // when
        var actualResult = TypeUtils.isPotentialTestClass(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

}
