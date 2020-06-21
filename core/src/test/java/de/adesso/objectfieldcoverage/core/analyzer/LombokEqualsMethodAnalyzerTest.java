package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.analyzer.lombok.LombokEqualsMethodAnalyzer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class LombokEqualsMethodAnalyzerTest {

    private LombokEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokEqualsMethodAnalyzer();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtTypeReference classRefMock,
                                                               @Mock CtClass classMock,
                                                               @Mock Data dataMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        given(classMock.getAnnotation(Data.class)).willReturn(dataMock);

        // when
        var actualResult = testSubject.overridesEquals(classRefMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtTypeReference classRefMock,
                                                               @Mock CtClass classMock,
                                                               @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        given(classMock.getAnnotation(Data.class)).willReturn(null);
        given(classMock.getAnnotation(EqualsAndHashCode.class)).willReturn(equalsAndHashCodeMock);

        // when
        var actualResult = testSubject.overridesEquals(classRefMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void overridesEqualsReturnsFalseWhenTypeIsNotAnnotatedWithDataOrEqualsAndHashCode(@Mock CtTypeReference classRefMock,
                                                                                      @Mock CtClass classMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        given(classMock.getAnnotation(Data.class)).willReturn(null);
        given(classMock.getAnnotation(EqualsAndHashCode.class)).willReturn(null);

        // when
        var actualResult = testSubject.overridesEquals(classRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsTrueWhenCallsSuperFlagSet(@Mock CtTypeReference classRefMock,
                                                    @Mock CtClass classMock,
                                                    @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);

        given(equalsAndHashCodeMock.callSuper()).willReturn(true);

        // when
        var actualResult = testSubject.callsSuper(classRefMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsFalseWhenCallsSuperFlagNotSet(@Mock CtTypeReference classRefMock,
                                                        @Mock CtClass classMock,
                                                        @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);

        given(equalsAndHashCodeMock.callSuper()).willReturn(false);

        // when
        var actualResult = testSubject.callsSuper(classRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void callsSuperReturnsFalseWhenDeclaringTypeNotAnnotated(@Mock CtTypeReference classRefMock,
                                                             @Mock CtClass classMock) {
        // given
        given(classRefMock.getTypeDeclaration()).willReturn(classMock);

        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(null).when(classMock).getAnnotation(EqualsAndHashCode.class);

        // when
        var actualResult = testSubject.callsSuper(classRefMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsExplicitlyIncludedFieldsWhenSpecified(@Mock CtTypeReference clazzRefMock,
                                                                                      @Mock CtClass classMock,
                                                                                      @Mock EqualsAndHashCode equalsAndHashCodeMock,
                                                                                      @Mock EqualsAndHashCode.Include includeMock,
                                                                                      @Mock CtField annotatedFieldMock,
                                                                                      @Mock CtField nonAnnotatedFieldMock,
                                                                                      @Mock CtField fieldDeclaredInSuperClassMock,
                                                                                      @Mock AccessibleField annotatedAccessibleFieldMock,
                                                                                      @Mock AccessibleField nonAnnotatedAccessibleFieldMock,
                                                                                      @Mock AccessibleField accessibleFieldNotDeclaredInTypeMock) {
        // given
        given(clazzRefMock.isClass()).willReturn(true);
        given(clazzRefMock.getTypeDeclaration()).willReturn(classMock);
        given(classMock.getFields()).willReturn(List.of(annotatedFieldMock, nonAnnotatedFieldMock));

        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);
        doReturn(null).when(classMock).getAnnotation(Data.class);
        given(equalsAndHashCodeMock.onlyExplicitlyIncluded()).willReturn(true);
        given(equalsAndHashCodeMock.exclude()).willReturn(new String[]{});

        given(annotatedFieldMock.getAnnotation(EqualsAndHashCode.Include.class)).willReturn(includeMock);
        given(nonAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Include.class)).willReturn(null);

        given(annotatedAccessibleFieldMock.getActualField()).willReturn(annotatedFieldMock);
        given(nonAnnotatedAccessibleFieldMock.getActualField()).willReturn(nonAnnotatedFieldMock);
        given(accessibleFieldNotDeclaredInTypeMock.getActualField()).willReturn(fieldDeclaredInSuperClassMock);

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(clazzRefMock,
                Set.of(annotatedAccessibleFieldMock, nonAnnotatedAccessibleFieldMock, accessibleFieldNotDeclaredInTypeMock));

        // then
        assertThat(actualResult).containsExactly(annotatedAccessibleFieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsNonExcludedFields(@Mock CtTypeReference clazzRefMock,
                                                                  @Mock CtClass classMock,
                                                                  @Mock EqualsAndHashCode equalsAndHashCodeMock,
                                                                  @Mock EqualsAndHashCode.Exclude excludeMock,
                                                                  @Mock CtField excludeAnnotatedFieldMock,
                                                                  @Mock CtField nonAnnotatedFieldMock,
                                                                  @Mock CtField excludedThroughNameFieldMock,
                                                                  @Mock CtField fieldDeclaredInSuperClassMock,
                                                                  @Mock AccessibleField excludeAnnotatedAccessibleFieldMock,
                                                                  @Mock AccessibleField nonAnnotatedAccessibleFieldMock,
                                                                  @Mock AccessibleField excludedThroughNameAccessibleFieldMock,
                                                                  @Mock AccessibleField accessibleFieldDeclaredInSuperClassMock) {
        // given
        var excludedFieldName = "excludedByName";
        var excludeAnnotatedFieldName = "excludeAnnotated";
        var nonAnnotatedFieldName = "nonAnnotated";

        given(clazzRefMock.isClass()).willReturn(true);
        given(clazzRefMock.getTypeDeclaration()).willReturn(classMock);
        given(classMock.getFields()).willReturn(List.of(excludeAnnotatedFieldMock, nonAnnotatedFieldMock,
                excludedThroughNameFieldMock, fieldDeclaredInSuperClassMock));

        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);
        doReturn(null).when(classMock).getAnnotation(Data.class);
        given(equalsAndHashCodeMock.onlyExplicitlyIncluded()).willReturn(false);
        given(equalsAndHashCodeMock.exclude()).willReturn(new String[]{ excludedFieldName });

        given(excludeAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Exclude.class)).willReturn(excludeMock);
        given(excludeAnnotatedFieldMock.getSimpleName()).willReturn(excludeAnnotatedFieldName);
        given(nonAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Exclude.class)).willReturn(null);
        given(nonAnnotatedFieldMock.getSimpleName()).willReturn(nonAnnotatedFieldName);
        given(fieldDeclaredInSuperClassMock.getAnnotation(EqualsAndHashCode.Exclude.class)).willReturn(null);
        given(fieldDeclaredInSuperClassMock.getSimpleName()).willReturn(nonAnnotatedFieldName);

        given(excludeAnnotatedAccessibleFieldMock.getActualField()).willReturn(excludeAnnotatedFieldMock);
        given(nonAnnotatedAccessibleFieldMock.getActualField()).willReturn(nonAnnotatedFieldMock);
        given(excludedThroughNameAccessibleFieldMock.getActualField()).willReturn(excludedThroughNameFieldMock);
        given(accessibleFieldDeclaredInSuperClassMock.getActualField()).willReturn(fieldDeclaredInSuperClassMock);

        given(excludedThroughNameFieldMock.getSimpleName()).willReturn(excludedFieldName);

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(clazzRefMock,
                Set.of(excludeAnnotatedAccessibleFieldMock, nonAnnotatedAccessibleFieldMock,
                        excludedThroughNameAccessibleFieldMock, accessibleFieldDeclaredInSuperClassMock));

        // then
        assertThat(actualResult).containsExactlyInAnyOrder(nonAnnotatedAccessibleFieldMock,
                accessibleFieldDeclaredInSuperClassMock);
    }

}
