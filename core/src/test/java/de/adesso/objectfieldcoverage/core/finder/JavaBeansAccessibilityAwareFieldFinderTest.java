package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.List;
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
                                                                           @Mock CtClass testClazzMock,
                                                                           @Mock CtField fieldMock,
                                                                           @Mock CtTypeReference fieldReferenceTypeMock,
                                                                           @Mock CtType fieldTypeMock,
                                                                           @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(true);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsBooleanReferenceTypeFieldWithGetterPresent(@Mock CtType typeMock,
                                                                               @Mock CtClass testClazzMock,
                                                                               @Mock CtField fieldMock,
                                                                               @Mock CtTypeReference fieldReferenceTypeMock,
                                                                               @Mock CtType fieldTypeMock,
                                                                               @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterForPrimitiveBooleanFieldPresentButNotPublic(@Mock CtType typeMock,
                                                                                                   @Mock CtClass testClazzMock,
                                                                                                   @Mock CtField fieldMock,
                                                                                                   @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                                   @Mock CtType fieldTypeMock,
                                                                                                   @Mock CtMethod getterMethodMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(false);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterForPrimitiveBooleanFieldPresentButStatic(@Mock CtType typeMock,
                                                                                                @Mock CtClass testClazzMock,
                                                                                                @Mock CtField fieldMock,
                                                                                                @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                                @Mock CtType fieldTypeMock,
                                                                                                @Mock CtMethod getterMethodMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.Boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);
        given(getterMethodMock.isStatic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenNoGetterPresentForPrimitiveBooleanField(@Mock CtType typeMock,
                                                                                         @Mock CtClass testClazzMock,
                                                                                         @Mock CtField fieldMock,
                                                                                         @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                         @Mock CtType fieldTypeMock) {
        // given
        var booleanFieldSimpleName = "activated";
        var expectedJavaBeansGetterMethodName = "isActivated";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(booleanFieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(true);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("boolean");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsReferenceTypeFieldWithGetterPresent(@Mock CtType typeMock,
                                                                        @Mock CtClass testClazzMock,
                                                                        @Mock CtField fieldMock,
                                                                        @Mock CtTypeReference fieldReferenceTypeMock,
                                                                        @Mock CtType fieldTypeMock,
                                                                        @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsStaticReferenceTypeFieldWithStaticGetterPresent(@Mock CtType typeMock,
                                                                                    @Mock CtClass testClazzMock,
                                                                                    @Mock CtField fieldMock,
                                                                                    @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                    @Mock CtType fieldTypeMock,
                                                                                    @Mock CtMethod getterMethodMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, getterMethodMock);

        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);
        given(fieldMock.isStatic()).willReturn(true);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenNoGetterPresentForReferenceTypeField(@Mock CtType typeMock,
                                                                                      @Mock CtClass testClazzMock,
                                                                                      @Mock CtField fieldMock,
                                                                                      @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                      @Mock CtType fieldTypeMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterPresentButStatic(@Mock CtType typeMock,
                                                                        @Mock CtClass testClazzMock,
                                                                        @Mock CtField fieldMock,
                                                                        @Mock CtTypeReference fieldReferenceTypeMock,
                                                                        @Mock CtType fieldTypeMock,
                                                                        @Mock CtMethod getterMethodMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);
        given(getterMethodMock.isStatic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenGetterPresentButNotPublic(@Mock CtType typeMock,
                                                                           @Mock CtClass testClazzMock,
                                                                           @Mock CtField fieldMock,
                                                                           @Mock CtTypeReference fieldReferenceTypeMock,
                                                                           @Mock CtType fieldTypeMock,
                                                                           @Mock CtMethod getterMethodMock) {
        // given
        var fieldSimpleName = "name";
        var expectedJavaBeansGetterMethodName = "getName";

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getSimpleName()).willReturn(fieldSimpleName);
        given(fieldMock.getType()).willReturn(fieldReferenceTypeMock);
        given(fieldMock.getDeclaringType()).willReturn(fieldTypeMock);

        given(fieldReferenceTypeMock.isPrimitive()).willReturn(false);
        given(fieldReferenceTypeMock.getQualifiedName()).willReturn("java.lang.String");

        given(fieldTypeMock.getMethod(fieldReferenceTypeMock, expectedJavaBeansGetterMethodName))
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(false);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    void findAccessibleFieldsReturnsEmptyListWhenTypeIsAnInterface(@Mock CtClass<?> testClazzMock,
                                                                   @Mock CtType<?> typeMock) {
        // given
        given(typeMock.isInterface()).willReturn(true);

        // when
        var actualFields =testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    void findAccessibleFieldsThrowsExceptionWhenTypeIsNull(@Mock CtClass<?> testClazzMock) {
        // given / when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFields(testClazzMock, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("type cannot be null!");
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

}
