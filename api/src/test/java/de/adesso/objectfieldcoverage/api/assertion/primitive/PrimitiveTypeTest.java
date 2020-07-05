package de.adesso.objectfieldcoverage.api.assertion.primitive;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimitiveTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "boolean", "byte", "char", "short", "int", "long", "float", "double"
    })
    void ofReturnsExpectedEnumConstant(String simpleName) {
        // given
        var expectedResult = PrimitiveType.valueOf(simpleName.toUpperCase());

        // when
        var actualResult = PrimitiveType.of(simpleName);

        // then
        assertThat(actualResult).isSameAs(expectedResult);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test", "string"
    })
    void ofThrowsExceptionForUnknownSimpleNames(String simpleName) {
        // given

        // when / then
        assertThatThrownBy(() -> PrimitiveType.of(simpleName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No PrimitiveType constant with name '%s' found!", simpleName);
    }

}
