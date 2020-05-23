package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtField;
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
    void isPrimitiveTypeFieldReturnsTrueWhenFieldTypeIsIntegerPrimitive(@Mock CtField<Integer> field) {
        // given
        var intPrimitiveTypeRef = new TypeFactory().INTEGER_PRIMITIVE;

        given(field.getType()).willReturn(intPrimitiveTypeRef);

        // when
        var actualResult = PrimitiveTypeUtils.isPrimitiveTypeField(field);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPrimitiveTypeFieldReturnsTrueWhenFieldTypeIsIntegerWrapper(@Mock CtField<Integer> field) {
        // given
        var intPrimitiveTypeRef = new TypeFactory().INTEGER;

        given(field.getType()).willReturn(intPrimitiveTypeRef);

        // when
        var actualResult = PrimitiveTypeUtils.isPrimitiveTypeField(field);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void isPrimitiveTypeFieldReturnsFalseWhenFieldTypeIsOtherReferenceType(@Mock CtField<String> field) {
        // given
        var intPrimitiveTypeRef = new TypeFactory().createReference(String.class);

        given(field.getType()).willReturn(intPrimitiveTypeRef);

        // when
        var actualResult = PrimitiveTypeUtils.isPrimitiveTypeField(field);

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

    @Test
    void getPrimitiveTypeReferenceReturnsBooleanPrimitiveForBoolean() {
        // given
        var givenPrimitiveTypeName = "boolean";
        var expectedTypeRef = new TypeFactory().BOOLEAN_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsBytePrimitiveForByte() {
        // given
        var givenPrimitiveTypeName = "byte";
        var expectedTypeRef = new TypeFactory().BYTE_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsShortPrimitiveForShort() {
        // given
        var givenPrimitiveTypeName = "short";
        var expectedTypeRef = new TypeFactory().SHORT_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsIntPrimitiveForInt() {
        // given
        var givenPrimitiveTypeName = "int";
        var expectedTypeRef = new TypeFactory().INTEGER_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsLongPrimitiveForLong() {
        // given
        var givenPrimitiveTypeName = "long";
        var expectedTypeRef = new TypeFactory().LONG_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsCharPrimitiveForChar() {
        // given
        var givenPrimitiveTypeName = "char";
        var expectedTypeRef = new TypeFactory().CHARACTER_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsFloatPrimitiveForFloat() {
        // given
        var givenPrimitiveTypeName = "float";
        var expectedTypeRef = new TypeFactory().FLOAT_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceReturnsDoublePrimitiveForDouble() {
        // given
        var givenPrimitiveTypeName = "double";
        var expectedTypeRef = new TypeFactory().DOUBLE_PRIMITIVE;

        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceThrowsExceptionForOtherString() {
        // given
        var givenPrimitiveTypeName = "unknown";

        // when / then
        assertThatThrownBy(() -> PrimitiveTypeUtils.getPrimitiveTypeReference(givenPrimitiveTypeName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("'%s' is not a primitive type!", givenPrimitiveTypeName);
    }

}
