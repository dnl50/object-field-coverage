package de.adesso.objectfieldcoverage.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AccessibleFieldTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void uniteReturnsExpectedInstance(@Mock CtField fieldMock, @Mock CtMethod firstMethodMock, @Mock CtMethod secondMethodMock) {
        // given
        var testSubject = new AccessibleField<>(fieldMock, firstMethodMock);
        var other = new AccessibleField<>(fieldMock, secondMethodMock);

        var expectedAccessibleField = new AccessibleField<>(fieldMock, Set.of(firstMethodMock, secondMethodMock));

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

}
