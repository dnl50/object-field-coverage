package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JavaBeansAccessibilityAwareFieldFinderTest {

    private JavaBeansAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JavaBeansAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPrimitiveBooleanFieldWithGetterPresent(@Mock CtType typeMock,
                                                                           @Mock CtTypeReference typeRefMock,
                                                                           @Mock CtClass testClazzMock,
                                                                           @Mock CtField fieldMock,
                                                                           @Mock CtTypeReference fieldReferenceTypeMock,
                                                                           @Mock CtType fieldTypeMock,
                                                                           @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(true);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);
        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsBooleanReferenceTypeFieldWithGetterPresent(@Mock CtType typeMock,
                                                                               @Mock CtTypeReference typeRefMock,
                                                                               @Mock CtClass testClazzMock,
                                                                               @Mock CtField fieldMock,
                                                                               @Mock CtTypeReference fieldReferenceTypeMock,
                                                                               @Mock CtType fieldTypeMock,
                                                                               @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);
        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterForPrimitiveBooleanFieldPresentButNotPublic(@Mock CtType typeMock,
                                                                                                   @Mock CtTypeReference typeRefMock,
                                                                                                   @Mock CtClass testClazzMock,
                                                                                                   @Mock CtField fieldMock,
                                                                                                   @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                                   @Mock CtType fieldTypeMock,
                                                                                                   @Mock CtMethod getterMethodMock,
                                                                                                   @Mock CtPackage typePackageMock,
                                                                                                   @Mock CtPackage testPackageMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);
        given(typeMock.getPackage()).willReturn(typePackageMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);
        given(testClazzMock.getPackage()).willReturn(testPackageMock);

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(false);

        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);
        given(getterMethodMock.isProtected()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterForPrimitiveBooleanFieldPresentButStatic(@Mock CtType typeMock,
                                                                                                @Mock CtTypeReference typeRefMock,
                                                                                                @Mock CtClass testClazzMock,
                                                                                                @Mock CtField fieldMock,
                                                                                                @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                                @Mock CtType fieldTypeMock,
                                                                                                @Mock CtMethod getterMethodMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isStatic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenNoGetterPresentForPrimitiveBooleanField(@Mock CtType typeMock,
                                                                                         @Mock CtTypeReference typeRefMock,
                                                                                         @Mock CtClass testClazzMock,
                                                                                         @Mock CtField fieldMock,
                                                                                         @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                         @Mock CtType fieldTypeMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(true);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsReferenceTypeFieldWithGetterPresent(@Mock CtType typeMock,
                                                                        @Mock CtTypeReference typeRefMock,
                                                                        @Mock CtClass testClazzMock,
                                                                        @Mock CtField fieldMock,
                                                                        @Mock CtTypeReference fieldReferenceTypeMock,
                                                                        @Mock CtType fieldTypeMock,
                                                                        @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);

        given(typeMock.isPublic()).willReturn(true);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);
        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsStaticReferenceTypeFieldWithGetterPresent(@Mock CtType typeMock,
                                                                              @Mock CtTypeReference typeRefMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtField fieldMock,
                                                                              @Mock CtTypeReference fieldReferenceTypeMock,
                                                                              @Mock CtType fieldTypeMock,
                                                                              @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);
        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenNoGetterPresentForReferenceTypeField(@Mock CtType typeMock,
                                                                                      @Mock CtTypeReference typeRefMock,
                                                                                      @Mock CtClass testClazzMock,
                                                                                      @Mock CtField fieldMock,
                                                                                      @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                      @Mock CtType fieldTypeMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterPresentButStatic(@Mock CtType typeMock,
                                                                        @Mock CtTypeReference typeRefMock,
                                                                        @Mock CtClass testClazzMock,
                                                                        @Mock CtField fieldMock,
                                                                        @Mock CtTypeReference fieldReferenceTypeMock,
                                                                        @Mock CtType fieldTypeMock,
                                                                        @Mock CtMethod getterMethodMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isStatic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterPresentButNotPublicAndNotAccessible(@Mock CtType typeMock,
                                                                                           @Mock CtTypeReference typeRefMock,
                                                                                           @Mock CtClass testClazzMock,
                                                                                           @Mock CtField fieldMock,
                                                                                           @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                           @Mock CtType fieldTypeMock,
                                                                                           @Mock CtMethod getterMethodMock,
                                                                                           @Mock CtPackage typePackageMock,
                                                                                           @Mock CtPackage testPackageMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeRefMockToReturnFields(typeRefMock, List.of(fieldMock));

        given(typeMock.getPackage()).willReturn(typePackageMock);

        given(testClazzMock.getPackage()).willReturn(testPackageMock);
        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.getModifiers()).willReturn(Set.of());

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.getDeclaringType()).willReturn(typeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeRefMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    void findAccessibleFieldsThrowsExceptionWhenTypeIsNull(@Mock CtClass<?> testClazzMock) {
        // given / when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFields(testClazzMock, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("The type reference of the type containing fields cannot be null!");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setUpTypeRefMockToReturnFields(CtTypeReference typeMock, Collection<CtField> fields) {
        var fieldReferences = fields.stream()
                .map(field -> {
                    var fieldReferenceMock = (CtFieldReference<?>) mock(CtFieldReference.class);
                    given(fieldReferenceMock.getFieldDeclaration()).willReturn(field);
                    return fieldReferenceMock;
                })
                .collect(Collectors.toList());

        doReturn(fieldReferences).when(typeMock).getAllFields();
    }

}
