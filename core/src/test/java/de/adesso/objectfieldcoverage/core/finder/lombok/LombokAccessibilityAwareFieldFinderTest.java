package de.adesso.objectfieldcoverage.core.finder.lombok;

import de.adesso.objectfieldcoverage.api.AccessibleField;
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

    @Mock
    private LombokGetterMethodGenerator methodGeneratorMock;

    private LombokAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokAccessibilityAwareFieldFinder(methodGeneratorMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenGetterWithDifferentAccessLevelPresent(@Mock CtType typeMock,
                                                                                         @Mock CtField fieldMock,
                                                                                         @Mock CtClass fieldDeclaringClassMock,
                                                                                         @Mock CtClass testClazzMock,
                                                                                         @Mock Data dataAnnotationMock) {
        // given
        var getterAccessLevelMock = AccessLevel.PUBLIC;

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);
        given(fieldMock.getAnnotation(Getter.class)).willReturn(null);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Data.class, dataAnnotationMock);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, getterAccessLevelMock))
                .willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenClassAnnotatedWithData(@Mock CtField fieldMock,
                                                                    @Mock CtClass fieldDeclaringClassMock,
                                                                    @Mock CtClass testClazzMock,
                                                                    @Mock CtMethod getterMethodMock,
                                                                    @Mock Data dataAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Data.class, dataAnnotationMock);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PUBLIC))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PUBLIC))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenDeclaringTypeIsAnnotatedWithPublicGetter(@Mock CtType typeMock,
                                                                                      @Mock CtField fieldMock,
                                                                                      @Mock CtClass fieldDeclaringClassMock,
                                                                                      @Mock CtClass testClazzMock,
                                                                                      @Mock CtMethod getterMethodMock,
                                                                                      @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PUBLIC);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PUBLIC))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PUBLIC))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPublicGetter(@Mock CtField getterAnnotatedField,
                                                                              @Mock CtField otherFieldMock,
                                                                              @Mock CtClass fieldDeclaringClassMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtMethod getterMethodMock,
                                                                              @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(getterAnnotatedField, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(getterAnnotatedField, otherFieldMock));

        given(otherFieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(getterAnnotatedField, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PUBLIC);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(getterAnnotatedField, AccessLevel.PUBLIC))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(getterAnnotatedField, AccessLevel.PUBLIC))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithProtectedGetterAndTestClassIsInSamePackage(@Mock CtField fieldMock,
                                                                                                            @Mock CtClass fieldDeclaringClassMock,
                                                                                                            @Mock CtClass testClazzMock,
                                                                                                            @Mock CtPackage packageMock,
                                                                                                            @Mock CtMethod getterMethodMock,
                                                                                                            @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PROTECTED))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PROTECTED))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isProtected()).willReturn(true);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithProtectedGetterAndTestClazzIsSubClass(@Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtTypeReference fieldDeclaringClassRefMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock CtPackage packageMock,
                                                                                                       @Mock CtPackage otherPackageMock,
                                                                                                       @Mock CtMethod getterMethodMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PROTECTED))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PROTECTED))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isProtected()).willReturn(true);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(otherPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(fieldDeclaringClassRefMock);
        given(fieldDeclaringClassRefMock.getTypeDeclaration()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenFieldIsAnnotatedWithProtectedGetterAndNotInSamePackageOrSubClass(@Mock CtField fieldMock,
                                                                                                                    @Mock CtClass fieldDeclaringClassMock,
                                                                                                                    @Mock CtClass testClazzMock,
                                                                                                                    @Mock CtPackage packageMock,
                                                                                                                    @Mock CtPackage otherPackageMock,
                                                                                                                    @Mock CtMethod getterMethodMock,
                                                                                                                    @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PROTECTED);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PROTECTED))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PROTECTED))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isProtected()).willReturn(true);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(otherPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsInSamePackage(@Mock CtField fieldMock,
                                                                                                          @Mock CtClass fieldDeclaringClassMock,
                                                                                                          @Mock CtClass testClazzMock,
                                                                                                          @Mock CtPackage packageMock,
                                                                                                          @Mock CtMethod getterMethodMock,
                                                                                                          @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PACKAGE))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PACKAGE))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(false);
        given(getterMethodMock.isProtected()).willReturn(false);
        given(getterMethodMock.isPrivate()).willReturn(false);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsInnerClass(@Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock CtPackage packageMock,
                                                                                                       @Mock CtMethod getterMethodMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PACKAGE))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PACKAGE))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenFieldIsAnnotatedWithPackageGetterAndTestClazzIsNoInnerClass(@Mock CtField fieldMock,
                                                                                                               @Mock CtClass fieldDeclaringClassMock,
                                                                                                               @Mock CtClass testClazzMock,
                                                                                                               @Mock CtPackage packageMock,
                                                                                                               @Mock CtPackage otherPackageMock,
                                                                                                               @Mock CtMethod getterMethodMock,
                                                                                                               @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PACKAGE);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PACKAGE))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PACKAGE))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(false);
        given(getterMethodMock.isProtected()).willReturn(false);
        given(getterMethodMock.isPrivate()).willReturn(false);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(otherPackageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenFieldIsAnnotatedWithPrivateGetterAndTestClazzIsInnerClass(@Mock CtField fieldMock,
                                                                                                       @Mock CtClass fieldDeclaringClassMock,
                                                                                                       @Mock CtClass testClazzMock,
                                                                                                       @Mock CtPackage packageMock,
                                                                                                       @Mock CtPackage otherPackageMock,
                                                                                                       @Mock CtMethod getterMethodMock,
                                                                                                       @Mock Getter getterAnnotationMock) {
        // given
        var expectedField = new AccessibleField<>(fieldMock, getterMethodMock);

        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PRIVATE);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PRIVATE))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PRIVATE))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

        // then
        assertThat(actualFields).containsExactly(expectedField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsDoesNotReturnFieldWhenFieldIsAnnotatedWithPrivateGetterAndTestClazzIsNoInnerClass(@Mock CtField fieldMock,
                                                                                                               @Mock CtClass fieldDeclaringClassMock,
                                                                                                               @Mock CtClass testClazzMock,
                                                                                                               @Mock CtPackage packageMock,
                                                                                                               @Mock CtPackage otherPackageMock,
                                                                                                               @Mock CtMethod getterMethodMock,
                                                                                                               @Mock Getter getterAnnotationMock) {
        // given
        setUpTypeMockToReturnFields(fieldDeclaringClassMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        setUpElementMockToReturnAnnotation(fieldDeclaringClassMock, Getter.class, getterAnnotationMock);
        given(getterAnnotationMock.value()).willReturn(AccessLevel.PRIVATE);

        given(methodGeneratorMock.isGetterMethodWithDifferentAccessModifierPresent(fieldMock, AccessLevel.PRIVATE))
                .willReturn(false);
        given(methodGeneratorMock.generateGetterMethod(fieldMock, AccessLevel.PRIVATE))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(fieldDeclaringClassMock);

        given(getterMethodMock.getTopLevelType()).willReturn(fieldDeclaringClassMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldDeclaringClassMock.isPublic()).willReturn(true);
        given(getterMethodMock.isPublic()).willReturn(false);
        given(getterMethodMock.isProtected()).willReturn(false);
        given(getterMethodMock.isPrivate()).willReturn(false);

        given(fieldDeclaringClassMock.getPackage()).willReturn(packageMock);
        given(testClazzMock.getPackage()).willReturn(otherPackageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringClassMock);

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
