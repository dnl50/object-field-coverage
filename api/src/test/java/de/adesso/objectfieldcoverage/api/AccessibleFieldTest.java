package de.adesso.objectfieldcoverage.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccessibleFieldTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteReturnsExpectedInstance(@Mock CtField fieldMock, @Mock CtMethod firstMethodMock, @Mock CtMethod secondMethodMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, firstMethodMock, true);
        var other = new AccessibleField<>(fieldMock, secondMethodMock, true);

        var expectedAccessibleField = new AccessibleField<>(fieldMock, Set.of(firstMethodMock, secondMethodMock), true);

        // when
        var actualAccessibleField = testSubject.unite(other);

        // then
        assertThat(actualAccessibleField).isEqualTo(expectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteThrowsExceptionWhenOtherActualFieldIsNotEqual(@Mock CtField fieldMock, @Mock CtField otherFieldMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of());
        var other = new AccessibleField<>(otherFieldMock, Set.of());

        // when / then
        assertThatThrownBy(() -> testSubject.unite(other))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The actual fields are not equal!");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteThrowsExceptionWhenPseudoFlagsNotEqual(@Mock CtField fieldMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of(), true);
        var other = new AccessibleField<>(fieldMock, Set.of(), false);

        // when / then
        assertThatThrownBy(() -> testSubject.unite(other))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The pseudo flag of the other AccessibleField instance is not the same!");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isDirectlyAccessibleReturnsTrueWhenAccessGrantingElementsContainsField(@Mock CtField fieldMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, fieldMock);

        // when
        var actualResult = testSubject.isDirectlyAccessible();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isDirectlyAccessibleReturnsFalseWhenAccessGrantingElementsDoesNotContainField(@Mock CtField fieldMock, @Mock CtMethod methodMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, methodMock);

        // when
        var actualResult = testSubject.isDirectlyAccessible();

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isAccessibleThroughMethodReturnsTrueWhenMethodInAccessGrantingElements(@Mock CtField fieldMock, @Mock CtMethod methodMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, methodMock);

        // when
        var actualResult = testSubject.isAccessibleThroughMethod();

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isAccessibleThroughMethodReturnsFalseWhenMethodNotInAccessGrantingElements(@Mock CtField fieldMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, fieldMock);

        // when
        var actualResult = testSubject.isAccessibleThroughMethod();

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isAccessibleThroughElementReturnsTrueWhenElementIsContained(@Mock CtField<String> fieldMock,
                                                                     @Mock CtTypedElement<String> typedElementMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, typedElementMock);

        // when
        var actualResult = testSubject.isAccessibleThroughElement(typedElementMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isAccessibleThroughElementReturnsFalseWhenElementIsNotContained(@Mock CtField<String> fieldMock,
                                                                         @Mock CtTypedElement<String> typedElementMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of());

        // when
        var actualResult = testSubject.isAccessibleThroughElement(typedElementMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void isAccessedThroughInvocationReturnsFalseWhenInvocationIsNull(@Mock CtField<String> fieldMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of());

        // when
        var actualResult = testSubject.isAccessedThroughInvocation(null);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isAccessedThroughInvocationReturnsFalseWhenInvocationTypeDoesNotMatch(@Mock CtField fieldMock,
                                                                               @Mock CtInvocation invocationMock,
                                                                               @Mock CtTypeReference fieldTypeRef,
                                                                               @Mock CtTypeReference invocationTypeRef) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of());

        given(fieldMock.getType()).willReturn(fieldTypeRef);
        given(invocationMock.getType()).willReturn(invocationTypeRef);

        // when
        var actualResult = testSubject.isAccessedThroughInvocation(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isAccessedThroughInvocationReturnsFalseWhenExecutableNotInAccessGrantingSet(@Mock CtField fieldMock,
                                                                                     @Mock CtInvocation invocationMock,
                                                                                     @Mock CtTypeReference typeRef,
                                                                                     @Mock CtExecutableReference execRefMock,
                                                                                     @Mock CtExecutable executableMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of());

        given(fieldMock.getType()).willReturn(typeRef);
        given(invocationMock.getType()).willReturn(typeRef);
        given(invocationMock.getExecutable()).willReturn(execRefMock);
        given(execRefMock.getDeclaration()).willReturn(executableMock);

        // when
        var actualResult = testSubject.isAccessedThroughInvocation(invocationMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void isAccessedThroughInvocationReturnsTrueWhenExecutableInAccessGrantingSet(@Mock CtField fieldMock,
                                                                                 @Mock CtInvocation invocationMock,
                                                                                 @Mock CtTypeReference typeRef,
                                                                                 @Mock CtExecutableReference execRefMock,
                                                                                 @Mock CtExecutable executableMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, Set.of(executableMock));

        given(fieldMock.getType()).willReturn(typeRef);
        given(invocationMock.getType()).willReturn(typeRef);
        given(invocationMock.getExecutable()).willReturn(execRefMock);
        given(execRefMock.getDeclaration()).willReturn(executableMock);

        // when
        var actualResult = testSubject.isAccessedThroughInvocation(invocationMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteAllUnitsAccessGrantingElements(@Mock CtField firstFieldMock,
                                             @Mock CtField secondFieldMock,
                                             @Mock CtTypedElement firstFieldAccessGrantingElementMock,
                                             @Mock CtTypedElement secondFieldAccessGrantingElementMock) {
        // given
        var firstGivenAccessibleField = new AccessibleField<>(firstFieldMock, firstFieldMock);
        var secondGivenAccessibleField = new AccessibleField<>(firstFieldMock, firstFieldAccessGrantingElementMock);
        var thirdGivenAccessibleField = new AccessibleField<>(secondFieldMock, secondFieldMock);
        var fourthGivenAccessibleField = new AccessibleField<>(secondFieldMock, secondFieldAccessGrantingElementMock);

        var firstExpectedAccessibleField = new AccessibleField<>(firstFieldMock, Set.of(firstFieldMock, firstFieldAccessGrantingElementMock), false);
        var secondExpectedAccessibleField = new AccessibleField<>(secondFieldMock, Set.of(secondFieldMock, secondFieldAccessGrantingElementMock), false);

        // when
        var actualResult = AccessibleField.uniteAll(Set.<AccessibleField<?>>of(firstGivenAccessibleField, secondGivenAccessibleField,
                thirdGivenAccessibleField, fourthGivenAccessibleField));

        // then
        assertThat(actualResult).containsExactlyInAnyOrder(firstExpectedAccessibleField, secondExpectedAccessibleField);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteAllThrowsExceptionWhenPseudoFlagNotConsistent(@Mock CtField fieldMock) {
        // given
        var fieldName = "fieldMock";

        var accessibleField = new AccessibleField<>(fieldMock, fieldMock);
        var otherAccessibleField = new AccessibleField<>(fieldMock, fieldMock, true);

        given(fieldMock.getSimpleName()).willReturn(fieldName);

        // when / then
        assertThatThrownBy(() -> AccessibleField.uniteAll(Set.<AccessibleField<?>>of(accessibleField, otherAccessibleField)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The pseudo field flag for field '%s' is not consistent!", fieldName);
    }

}
