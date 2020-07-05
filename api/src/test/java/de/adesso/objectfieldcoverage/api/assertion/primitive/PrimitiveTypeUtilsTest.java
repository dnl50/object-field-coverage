package de.adesso.objectfieldcoverage.api.assertion.primitive;

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
        var actualResult = PrimitiveTypeUtils.isCandidateForPrimitiveTypeAssertion(expressionMock);

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
        var actualResult = PrimitiveTypeUtils.isCandidateForPrimitiveTypeAssertion(expressionMock);

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
        var actualResult = PrimitiveTypeUtils.isCandidateForPrimitiveTypeAssertion(expressionMock);

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

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForBoolean() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.BOOLEAN_PRIMITIVE;
        var expectedTypeRef = typeFactory.BOOLEAN_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForBooleanWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.BOOLEAN;
        var expectedTypeRef = typeFactory.BOOLEAN_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForByte() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.BYTE_PRIMITIVE;
        var expectedTypeRef = typeFactory.BYTE_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForByteWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.BYTE;
        var expectedTypeRef = typeFactory.BYTE_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForChar() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.CHARACTER_PRIMITIVE;
        var expectedTypeRef = typeFactory.CHARACTER_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForCharWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.CHARACTER;
        var expectedTypeRef = typeFactory.CHARACTER_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForShort() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.SHORT_PRIMITIVE;
        var expectedTypeRef = typeFactory.SHORT_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForShortWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.SHORT;
        var expectedTypeRef = typeFactory.SHORT_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForInt() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.INTEGER_PRIMITIVE;
        var expectedTypeRef = typeFactory.INTEGER_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForIntWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.INTEGER;
        var expectedTypeRef = typeFactory.INTEGER_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForLong() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.LONG_PRIMITIVE;
        var expectedTypeRef = typeFactory.LONG_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForLongWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.LONG;
        var expectedTypeRef = typeFactory.LONG_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForFloat() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.FLOAT_PRIMITIVE;
        var expectedTypeRef = typeFactory.FLOAT_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForFloatWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.FLOAT;
        var expectedTypeRef = typeFactory.FLOAT_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForDouble() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.DOUBLE_PRIMITIVE;
        var expectedTypeRef = typeFactory.DOUBLE_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReturnsExpectedTypeRefForDoubleWrapper() {
        // given
        var typeFactory = new TypeFactory();
        var givenTypeRef = typeFactory.DOUBLE;
        var expectedTypeRef = typeFactory.DOUBLE_PRIMITIVE;

        // when / then
        assertGetPrimitiveType(givenTypeRef, expectedTypeRef);
    }

    @Test
    void getPrimitiveTypeReferenceThrowsExceptionWhenGivenTypeIsNotAPrimitiveType() {
        // given
        var givenTypeRef = new TypeFactory().createReference(String.class);

        // when / then
        assertThatThrownBy(() -> PrimitiveTypeUtils.getPrimitiveTypeReference(givenTypeRef))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given type is not a primitive or wrapper type!");
    }

    private void assertGetPrimitiveType(CtTypeReference<?> givenTypeRef, CtTypeReference<?> expectedResult) {
        // when
        var actualTypeRef = PrimitiveTypeUtils.getPrimitiveTypeReference(givenTypeRef);

        // then
        assertThat(actualTypeRef).isEqualTo(expectedResult);
    }

}
