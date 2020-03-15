package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PrimitiveTypeUtilsTest {

    @Test
    @SuppressWarnings("rawtypes")
    void isCandidateForBooleanTypeAssertionReturnsTrueWhenExpressionTypeIsPrimitiveBoolean(@Mock CtExpression expressionMock) {
        // given
        var booleanPrimitive = new TypeFactory().BOOLEAN_PRIMITIVE;
        given(expressionMock.getType()).willReturn(booleanPrimitive);

        // when
        var actualResult = PrimitiveTypeUtils.isCandidateForBooleanTypeAssertion(expressionMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void isCandidateForBooleanTypeAssertionReturnsTrueWhenExpressionTypeIsWrapperBoolean(@Mock CtExpression expressionMock) {
        // given
        var booleanWrapper = new TypeFactory().BOOLEAN;
        given(expressionMock.getType()).willReturn(booleanWrapper);

        // when
        var actualResult = PrimitiveTypeUtils.isCandidateForBooleanTypeAssertion(expressionMock);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void isCandidateForBooleanTypeAssertionReturnsFalseWhenExpressionTypeDoesNotMatch(@Mock CtExpression expressionMock,
                                                                                      @Mock CtTypeReference typeRefMock) {
        // given
        given(expressionMock.getType()).willReturn(typeRefMock);

        // when
        var actualResult = PrimitiveTypeUtils.isCandidateForBooleanTypeAssertion(expressionMock);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void buildBooleanTypeAssertionReturnsBooleanTypeAssertionWhenExpressionTypeIsPrimitiveBoolean(@Mock CtExpression expressionMock,
                                                                                                  @Mock CtTypeReference typeRefMock) {
        // given
        var booleanWrapper = new TypeFactory().BOOLEAN_PRIMITIVE;
        given(expressionMock.getType()).willReturn(booleanWrapper);

        var expectedAssertion = new BooleanTypeAssertion(expressionMock);

        // when
        var actualAssertion = PrimitiveTypeUtils.buildBooleanTypeAssertion(expressionMock);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void buildBooleanTypeAssertionReturnsBooleanTypeAssertionWhenExpressionTypeIsWrapperBoolean(@Mock CtExpression expressionMock,
                                                                                                @Mock CtTypeReference typeRefMock) {
        // given
        var booleanWrapper = new TypeFactory().BOOLEAN;
        given(expressionMock.getType()).willReturn(booleanWrapper);

        var expectedAssertion = new BooleanTypeAssertion(expressionMock);

        // when
        var actualAssertion = PrimitiveTypeUtils.buildBooleanTypeAssertion(expressionMock);

        // then
        assertThat(actualAssertion).isEqualTo(expectedAssertion);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void buildBooleanTypeAssertionThrowsExceptionWhenExpressionTypeDoesNotMatch(@Mock CtExpression expressionMock,
                                                                                @Mock CtTypeReference typeRefMock) {
        // given
        given(expressionMock.getType()).willReturn(typeRefMock);

        // when / then
        assertThatThrownBy(() -> PrimitiveTypeUtils.buildBooleanTypeAssertion(expressionMock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given expression's return type is not compatible!");
    }

}
