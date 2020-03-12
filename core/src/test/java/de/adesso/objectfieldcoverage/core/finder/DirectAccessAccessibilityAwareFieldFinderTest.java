package de.adesso.objectfieldcoverage.core.finder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DirectAccessAccessibilityAwareFieldFinderTest {

    private DirectAccessAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DirectAccessAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void findAccessibleFieldsReturnsPublicField(@Mock CtType typeMock,
                                                @Mock CtClass testClazzMock,
                                                @Mock CtField fieldMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.isPublic()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsProtectedFieldWhenTestClassIsInSamePackage(@Mock CtType typeMock,
                                                                               @Mock CtClass testClazzMock,
                                                                               @Mock CtField fieldMock,
                                                                               @Mock CtType fieldDeclaringTypeMock,
                                                                               @Mock CtPackage packageMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.isProtected()).willReturn(true);

        given(fieldDeclaringTypeMock.getPackage()).willReturn(packageMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsProtectedFieldWhenTestClassIsRealSubClass(@Mock CtType typeMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtField fieldMock,
                                                                              @Mock CtType fieldDeclaringTypeMock,
                                                                              @Mock CtPackage fieldDeclaringTypePackage,
                                                                              @Mock CtTypeReference fieldDeclaringTypeReference,
                                                                              @Mock CtPackage testClassPackageMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.isProtected()).willReturn(true);

        given(fieldDeclaringTypeMock.getPackage()).willReturn(fieldDeclaringTypePackage);

        given(testClazzMock.getPackage()).willReturn(testClassPackageMock);
        given(testClazzMock.getSuperclass()).willReturn(fieldDeclaringTypeReference);

        given(fieldDeclaringTypeReference.getTypeDeclaration()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenFieldIsProtectedAndTestClassInDifferentPackage(@Mock CtType typeMock,
                                                                                                @Mock CtClass testClazzMock,
                                                                                                @Mock CtField fieldMock,
                                                                                                @Mock CtType fieldDeclaringTypeMock,
                                                                                                @Mock CtPackage fieldDeclaringTypePackage,
                                                                                                @Mock CtPackage testClassPackageMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.isProtected()).willReturn(true);

        given(fieldDeclaringTypeMock.getPackage()).willReturn(fieldDeclaringTypePackage);

        given(testClazzMock.getPackage()).willReturn(testClassPackageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPackagePrivateFieldWhenTestClassIsInSamePackage(@Mock CtType typeMock,
                                                                                    @Mock CtClass testClazzMock,
                                                                                    @Mock CtField fieldMock,
                                                                                    @Mock CtType fieldDeclaringTypeMock,
                                                                                    @Mock CtPackage packageMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(fieldDeclaringTypeMock.getPackage()).willReturn(packageMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPackagePrivateFieldWhenTestClassIsInnerClass(@Mock CtType typeMock,
                                                                                 @Mock CtClass testClazzMock,
                                                                                 @Mock CtField fieldMock,
                                                                                 @Mock CtType fieldDeclaringTypeMock,
                                                                                 @Mock CtPackage packageMock,
                                                                                 @Mock CtPackage testClassPackageMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(fieldDeclaringTypeMock.getPackage()).willReturn(packageMock);

        given(testClazzMock.getPackage()).willReturn(testClassPackageMock);
        given(testClazzMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenTestClassInnerClass(@Mock CtType typeMock,
                                                                 @Mock CtClass testClazzMock,
                                                                 @Mock CtField fieldMock,
                                                                 @Mock CtType fieldDeclaringTypeMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.isPrivate()).willReturn(true);

        given(testClazzMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListPrivateFieldWhenTestClassIsNoInnerClass(@Mock CtType typeMock,
                                                                                     @Mock CtClass testClazzMock,
                                                                                     @Mock CtField fieldMock,
                                                                                     @Mock CtType fieldDeclaringTypeMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.isPrivate()).willReturn(true);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenTestClazzIsDeclaringClassOfField(@Mock CtType typeMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtField fieldMock) {
        // given
        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(fieldMock.getDeclaringType()).willReturn(testClazzMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(fieldMock);
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
