package de.adesso.objectfieldcoverage.core.finder.lombok;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LombokAccessibilityAwareFieldFinderTest {

    private LombokAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsAllFieldsWhenDeclaringTypeIsAnnotatedWithData(@Mock CtType typeMock,
                                                                                  @Mock CtField fieldMock,
                                                                                  @Mock CtClass fieldDeclaringClassMock,
                                                                                  @Mock CtClass testClazzMock,
                                                                                  @Mock Data dataAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Data.class, dataAnnotationMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsAllFieldsWhenDeclaringTypeIsAnnotatedWithPublicGetter(@Mock CtType typeMock,
                                                                                          @Mock CtField fieldMock,
                                                                                          @Mock CtClass fieldDeclaringClassMock,
                                                                                          @Mock CtClass testClazzMock,
                                                                                          @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PUBLIC);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPublicGetter(@Mock CtType typeMock,
                                                                              @Mock CtField getterAnnotatedField,
                                                                              @Mock CtField otherFieldMock,
                                                                              @Mock CtClass fieldDeclaringClassMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(getterAnnotatedField, otherFieldMock));

        setUpElementMockToReturnAnnotation(getterAnnotatedField, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PUBLIC);

        given(otherFieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(getterAnnotatedField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithProtectedGetterAndTestClazzIsInSamePackage(@Mock CtType typeMock,
                                                                                                            @Mock CtField fieldMock,
                                                                                                            @Mock CtClass fieldDeclaringClassMock,
                                                                                                            @Mock CtClass testClazzMock,
                                                                                                            @Mock CtPackage packageMock,
                                                                                                            @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithProtectedGetterAndTestClazzIsSubClass(@Mock CtType typeMock,
                                                                                                       @Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtTypeReference fieldDeclaringClassReferenceMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock CtPackage fieldPackageMock,
                                                                                                       @Mock CtPackage testClazzPackageMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldPackageMock);
        given(testClazzMock.getPackage()).willReturn(testClazzPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(fieldDeclaringClassReferenceMock);
        given(fieldDeclaringClassReferenceMock.getTypeDeclaration()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenFieldIsAnnotatedWithProtectedGetterAndTestClazzIsNoSubClass(@Mock CtType typeMock,
                                                                                                               @Mock CtField fieldMock,
                                                                                                               @Mock CtClass fieldDeclaringClassMock,
                                                                                                               @Mock CtClass testClazzMock,
                                                                                                               @Mock CtPackage fieldPackageMock,
                                                                                                               @Mock CtPackage testClazzPackageMock,
                                                                                                               @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldPackageMock);
        given(testClazzMock.getPackage()).willReturn(testClazzPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsInSamePackage(@Mock CtType typeMock,
                                                                                                          @Mock CtField fieldMock,
                                                                                                          @Mock CtClass fieldDeclaringClassMock,
                                                                                                          @Mock CtClass testClazzMock,
                                                                                                          @Mock CtPackage packageMock,
                                                                                                          @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsInnerClass(@Mock CtType typeMock,
                                                                                                       @Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock CtPackage fieldPackageMock,
                                                                                                       @Mock CtPackage testClazzPackageMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldPackageMock);
        given(testClazzMock.getPackage()).willReturn(testClazzPackageMock);

        given(testClazzMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsNoInnerClass(@Mock CtType typeMock,
                                                                                                               @Mock CtField fieldMock,
                                                                                                               @Mock CtClass fieldDeclaringClassMock,
                                                                                                               @Mock CtClass testClazzMock,
                                                                                                               @Mock CtPackage fieldPackageMock,
                                                                                                               @Mock CtPackage testClazzPackageMock,
                                                                                                               @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(fieldDeclaringClassMock.getPackage()).willReturn(fieldPackageMock);
        given(testClazzMock.getPackage()).willReturn(testClazzPackageMock);

        given(testClazzMock.getDeclaringType()).willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPrivateGetterAndTestClazzIsInnerClass(@Mock CtType typeMock,
                                                                                                       @Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PRIVATE);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(testClazzMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPrivateGetterAndTestClazzIsNoInnerClass(@Mock CtType typeMock,
                                                                                                       @Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        setUpElementMockToReturnAnnotation(fieldMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PRIVATE);

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(testClazzMock.getDeclaringType()).willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setUpTypeMockToReturnFields(CtType typeMock, Collection<CtField> fields) {
        var fieldReferences = fields.stream()
                .map(field -> {
                    var fieldReferenceMock = (CtFieldReference<?>) mock(CtFieldReference.class);
                    given(fieldReferenceMock.getFieldDeclaration()).willReturn(field);
                    return fieldReferenceMock;
                })
                .collect(Collectors.toList());

        doReturn(fieldReferences).when(typeMock).getAllFields();
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> void setUpElementMockToReturnAnnotation(CtElement elementMock, Class<T> type, T annotation) {
        given(elementMock.getAnnotation(any(Class.class))).willAnswer(invocation -> {
            var actualAnnotationType = (Class<? extends Annotation>) invocation.getArgument(0);

            if (type.equals(actualAnnotationType)) {
                return annotation;
            }

            return null;
        });
    }

}
