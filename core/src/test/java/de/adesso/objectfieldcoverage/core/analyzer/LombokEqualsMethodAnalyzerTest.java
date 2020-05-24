package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;

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
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtClass<?> classMock,
                                                               @Mock Data dataMock) {
        // given
        given(classMock.getAnnotation(Data.class)).willReturn(dataMock);

        // when
        var actualResult = testSubject.overridesEquals(classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void overridesEqualsReturnsTrueWhenTypeIsAnnotatedWithData(@Mock CtClass<?> classMock,
                                                               @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        given(classMock.getAnnotation(Data.class)).willReturn(null);
        given(classMock.getAnnotation(EqualsAndHashCode.class)).willReturn(equalsAndHashCodeMock);

        // when
        var actualResult = testSubject.overridesEquals(classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void overridesEqualsReturnsFalseWhenTypeIsNotAnnotatedWithDataOrEqualsAndHashCode(@Mock CtClass<?> classMock) {
        // given
        given(classMock.getAnnotation(Data.class)).willReturn(null);
        given(classMock.getAnnotation(EqualsAndHashCode.class)).willReturn(null);

        // when
        var actualResult = testSubject.overridesEquals(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsTrueWhenCallsSuperFlagSet(@Mock CtClass classMock,
                                                    @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);

        given(equalsAndHashCodeMock.callSuper()).willReturn(true);

        // when
        var actualResult = testSubject.callsSuper(classMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void callsSuperReturnsFalseWhenCallsSuperFlagNotSet(@Mock CtClass classMock,
                                                        @Mock EqualsAndHashCode equalsAndHashCodeMock) {
        // given
        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(equalsAndHashCodeMock).when(classMock).getAnnotation(EqualsAndHashCode.class);

        given(equalsAndHashCodeMock.callSuper()).willReturn(false);

        // when
        var actualResult = testSubject.callsSuper(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void callsSuperReturnsFalseWhenDeclaringTypeNotAnnotated(@Mock CtClass classMock) {
        // given
        doReturn(null).when(classMock).getAnnotation(Data.class);
        doReturn(null).when(classMock).getAnnotation(EqualsAndHashCode.class);

        // when
        var actualResult = testSubject.callsSuper(classMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsExplicitlyIncludedFieldsWhenSpecified(@Mock CtClass clazzMock,
                                                                                      @Mock EqualsAndHashCode equalsAndHashCodeMock,
                                                                                      @Mock EqualsAndHashCode.Include includeMock,
                                                                                      @Mock CtField annotatedFieldMock,
                                                                                      @Mock CtField nonAnnotatedFieldMock,
                                                                                      @Mock CtField fieldDeclaredInSuperClassMock,
                                                                                      @Mock AccessibleField annotatedAccessibleFieldMock,
                                                                                      @Mock AccessibleField nonAnnotatedAccessibleFieldMock,
                                                                                      @Mock AccessibleField accessibleFieldNotDeclaredInTypeMock) {
        // given
        given(clazzMock.getFields()).willReturn(List.of(annotatedFieldMock, nonAnnotatedFieldMock));

        doReturn(equalsAndHashCodeMock).when(clazzMock).getAnnotation(EqualsAndHashCode.class);
        doReturn(null).when(clazzMock).getAnnotation(Data.class);
        given(equalsAndHashCodeMock.onlyExplicitlyIncluded()).willReturn(true);
        given(equalsAndHashCodeMock.exclude()).willReturn(new String[]{});

        given(annotatedFieldMock.getAnnotation(EqualsAndHashCode.Include.class)).willReturn(includeMock);
        given(nonAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Include.class)).willReturn(null);

        given(annotatedAccessibleFieldMock.getActualField()).willReturn(annotatedFieldMock);
        given(nonAnnotatedAccessibleFieldMock.getActualField()).willReturn(nonAnnotatedFieldMock);
        given(accessibleFieldNotDeclaredInTypeMock.getActualField()).willReturn(fieldDeclaredInSuperClassMock);

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(clazzMock,
                Set.of(annotatedAccessibleFieldMock, nonAnnotatedAccessibleFieldMock, accessibleFieldNotDeclaredInTypeMock));

        // then
        assertThat(actualResult).containsExactly(annotatedAccessibleFieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findFieldsComparedInEqualsMethodReturnsNonExcludedFields(@Mock CtClass clazzMock,
                                                                  @Mock EqualsAndHashCode equalsAndHashCodeMock,
                                                                  @Mock EqualsAndHashCode.Exclude excludeMock,
                                                                  @Mock CtField excludeAnnotatedFieldMock,
                                                                  @Mock CtField nonAnnotatedFieldMock,
                                                                  @Mock CtField excludedThroughNameFieldMock,
                                                                  @Mock CtField fieldDeclaredInSuperClassMock,
                                                                  @Mock AccessibleField excludeAnnotatedAccessibleFieldMock,
                                                                  @Mock AccessibleField nonAnnotatedAccessibleFieldMock,
                                                                  @Mock AccessibleField excludedThroughNameAccessibleFieldMock,
                                                                  @Mock AccessibleField accessibleFieldNotDeclaredInTypeMock) {
        // given
        var excludedFieldName = "field";

        given(clazzMock.getFields()).willReturn(List.of(excludeAnnotatedFieldMock, nonAnnotatedFieldMock, excludedThroughNameFieldMock));

        doReturn(equalsAndHashCodeMock).when(clazzMock).getAnnotation(EqualsAndHashCode.class);
        doReturn(null).when(clazzMock).getAnnotation(Data.class);
        given(equalsAndHashCodeMock.onlyExplicitlyIncluded()).willReturn(false);
        given(equalsAndHashCodeMock.exclude()).willReturn(new String[]{ excludedFieldName });

        given(excludeAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Exclude.class)).willReturn(excludeMock);
        given(nonAnnotatedFieldMock.getAnnotation(EqualsAndHashCode.Exclude.class)).willReturn(null);

        given(excludeAnnotatedAccessibleFieldMock.getActualField()).willReturn(excludeAnnotatedFieldMock);
        given(nonAnnotatedAccessibleFieldMock.getActualField()).willReturn(nonAnnotatedFieldMock);
        given(excludedThroughNameAccessibleFieldMock.getActualField()).willReturn(excludedThroughNameFieldMock);
        given(accessibleFieldNotDeclaredInTypeMock.getActualField()).willReturn(fieldDeclaredInSuperClassMock);

        given(excludedThroughNameFieldMock.getSimpleName()).willReturn(excludedFieldName);

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(clazzMock,
                Set.of(excludeAnnotatedAccessibleFieldMock, nonAnnotatedAccessibleFieldMock,
                        excludedThroughNameAccessibleFieldMock, accessibleFieldNotDeclaredInTypeMock));

        // then
        assertThat(actualResult).containsExactly(nonAnnotatedAccessibleFieldMock);
    }

}
