package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findAccessibleFieldsReturnsPublicField(@Mock CtType typeMock,
                                                @Mock CtClass testClazzMock,
                                                @Mock CtField fieldMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);
        given(fieldMock.isPublic()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(typeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsProtectedFieldWhenTestClassIsInSamePackage(@Mock CtType typeMock,
                                                                               @Mock CtClass testClazzMock,
                                                                               @Mock CtField fieldMock,
                                                                               @Mock CtPackage packageMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(typeMock, List.of(fieldMock));

        given(typeMock.isPublic()).willReturn(true);
        given(fieldMock.isProtected()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(typeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);
        given(typeMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, typeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsProtectedFieldWhenTestClassIsRealSubClass(@Mock CtType fieldDeclaringTypeMock,
                                                                              @Mock CtTypeReference fieldDeclaringTypeRefMock,
                                                                              @Mock CtClass testClazzMock,
                                                                              @Mock CtField fieldMock,
                                                                              @Mock CtPackage packageMock,
                                                                              @Mock CtPackage otherPackageMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(fieldDeclaringTypeMock.isPublic()).willReturn(true);
        given(fieldMock.isProtected()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);
        given(fieldDeclaringTypeMock.getPackage()).willReturn(otherPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(fieldDeclaringTypeRefMock);
        given(fieldDeclaringTypeRefMock.getTypeDeclaration()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListWhenFieldIsProtectedAndTestClassInDifferentPackage(@Mock CtType fieldDeclaringTypeMock,
                                                                                                @Mock CtClass testClazzMock,
                                                                                                @Mock CtField fieldMock,
                                                                                                @Mock CtPackage packageMock,
                                                                                                @Mock CtPackage otherPackageMock) {
        // given
        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(fieldDeclaringTypeMock.isPublic()).willReturn(true);
        given(fieldMock.isProtected()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);
        given(fieldDeclaringTypeMock.getPackage()).willReturn(otherPackageMock);

        given(testClazzMock.getSuperclass()).willReturn(null);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPackagePrivateFieldWhenTestClassIsInSamePackage(@Mock CtType fieldDeclaringTypeMock,
                                                                                    @Mock CtClass testClazzMock,
                                                                                    @Mock CtField fieldMock,
                                                                                    @Mock CtPackage packageMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(fieldDeclaringTypeMock.isPublic()).willReturn(true);
        given(fieldMock.isPublic()).willReturn(false);
        given(fieldMock.isProtected()).willReturn(false);
        given(fieldMock.isPrivate()).willReturn(false);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);

        given(testClazzMock.getPackage()).willReturn(packageMock);
        given(fieldDeclaringTypeMock.getPackage()).willReturn(packageMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }


    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPackagePrivateFieldWhenTestClassIsInnerClass(@Mock CtType fieldDeclaringTypeMock,
                                                                                 @Mock CtClass testClazzMock,
                                                                                 @Mock CtField fieldMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(testClazzMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsPrivateFieldWhenTestClassInnerClass(@Mock CtType fieldDeclaringTypeMock,
                                                                        @Mock CtClass testClazzMock,
                                                                        @Mock CtField fieldMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(testClazzMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsEmptyListPrivateFieldWhenTestClassIsNoInnerClass(@Mock CtType fieldDeclaringTypeMock,
                                                                                     @Mock CtClass testClazzMock,
                                                                                     @Mock CtField fieldMock) {
        // given
        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(fieldDeclaringTypeMock.isPublic()).willReturn(true);

        given(fieldMock.isPrivate()).willReturn(true);
        given(fieldMock.getDeclaringType()).willReturn(fieldDeclaringTypeMock);

        given(testClazzMock.getTopLevelType()).willReturn(testClazzMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(testClazzMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsFieldWhenTestClazzIsDeclaringClassOfField(@Mock CtType fieldDeclaringTypeMock,
                                                                              @Mock CtField fieldMock) {
        // given
        var expectedAccessibleField = new AccessibleField<>(fieldMock, fieldMock);

        setUpTypeMockToReturnFields(fieldDeclaringTypeMock, List.of(fieldMock));

        given(fieldDeclaringTypeMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);
        given(fieldMock.getTopLevelType()).willReturn(fieldDeclaringTypeMock);

        // when
        var actualFields = testSubject.findAccessibleFields(fieldDeclaringTypeMock, fieldDeclaringTypeMock);

        // then
        assertThat(actualFields).containsExactly(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessGrantingElementsReturnsSetContainingFieldItself(@Mock CtType typeMock,
                                                                   @Mock CtField fieldMock) {
        // given

        // when
        var actualAccessGrantingElements = testSubject.findAccessGrantingElements(typeMock, fieldMock);

        // then
        assertThat(actualAccessGrantingElements).containsExactly(fieldMock);
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
