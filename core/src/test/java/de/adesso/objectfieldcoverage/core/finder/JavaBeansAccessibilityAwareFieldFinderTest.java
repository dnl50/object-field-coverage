package de.adesso.objectfieldcoverage.core.finder;

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
    void findAccessibleFieldsReturnsPrimitiveBooleanFieldWithJavaBeansGetterPresent(@Mock CtType typeMock,
                                                                                    @Mock CtClass testClazzMock,
                                                                                    @Mock CtField fieldMock,
                                                                                    @Mock CtTypeReference fieldReferenceTypeMock,
                                                                                    @Mock CtType fieldDeclaringTypeMock,
                                                                                    @Mock CtType fieldTypeMock,
                                                                                    @Mock CtMethod getterMethodMock) {
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
                .willReturn(getterMethodMock);

        given(getterMethodMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    void findAccessibleFieldsThrowsExceptionWhenTestClazzIsNull(@Mock CtType<?> typeMock) {
        // given / when / then
        assertThatThrownBy(() -> testSubject.findAccessibleFields( null, typeMock))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("testClazz cannot be null!");
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
