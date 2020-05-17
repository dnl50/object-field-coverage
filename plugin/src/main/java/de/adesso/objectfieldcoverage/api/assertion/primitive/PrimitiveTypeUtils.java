package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Objects;
import java.util.Set;

/**
 * Utility class providing some basic util methods related to the construction of {@link PrimitiveTypeAssertion}s.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrimitiveTypeUtils {

    /**
     * The type factory used to build the Spoon representation for primitive types
     * and their wrapper classes.
     */
    private static final TypeFactory TYPE_FACTORY = new TypeFactory();

    /**
     * A set containing all type references of all 8 primitive types and their corresponding
     * wrapper classes.
     */
    private static final Set<CtTypeReference<?>> PRIMITIVE_TYPE_REFERENCES;

    /*
     * static initializer block for the PRIMITIVE_TYPE_REFERENCES set.
     */
    static {
        PRIMITIVE_TYPE_REFERENCES = Set.of(
                TYPE_FACTORY.BOOLEAN_PRIMITIVE, TYPE_FACTORY.BOOLEAN,
                TYPE_FACTORY.CHARACTER_PRIMITIVE, TYPE_FACTORY.CHARACTER,
                TYPE_FACTORY.BYTE_PRIMITIVE, TYPE_FACTORY.BYTE,
                TYPE_FACTORY.SHORT_PRIMITIVE, TYPE_FACTORY.SHORT,
                TYPE_FACTORY.INTEGER_PRIMITIVE, TYPE_FACTORY.INTEGER,
                TYPE_FACTORY.LONG_PRIMITIVE, TYPE_FACTORY.LONG,
                TYPE_FACTORY.FLOAT_PRIMITIVE, TYPE_FACTORY.FLOAT,
                TYPE_FACTORY.DOUBLE_PRIMITIVE, TYPE_FACTORY.DOUBLE
        );
    }

    /**
     * Static utility method to evaluate whether a given {@link CtExpression} can be used
     * to build a {@link BooleanTypeAssertion}.
     *
     * @param expression
     *          The expression to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code expression}'s type is equal to either
     *          {@link TypeFactory#BOOLEAN_PRIMITIVE} or {@link TypeFactory#BOOLEAN}.
     */
    public static boolean isCandidateForBooleanTypeAssertion(CtExpression<?> expression) {
        Objects.requireNonNull(expression, "expression cannot be null!");

        var expressionType = expression.getType();

        return TYPE_FACTORY.BOOLEAN_PRIMITIVE.equals(expressionType) ||
                TYPE_FACTORY.BOOLEAN.equals(expressionType);
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field}'s {@link CtField#getType() type} is
     *          a {@link CtTypeReference#isPrimitive() primitive type} or a primitive types
     *          wrapper classes. {@code false} is returned otherwise.
     */
    public static boolean isPrimitiveTypeField(CtField<?> field) {
        Objects.requireNonNull(field, "The given field cannot be null!");

        return PRIMITIVE_TYPE_REFERENCES.contains(field.getType());
    }

    /**
     * Static utility method used to build a {@link BooleanTypeAssertion} from a given
     * {@link CtExpression}. The expression must be {@link #isCandidateForBooleanTypeAssertion(CtExpression)
     * compatible}.
     *
     * @param expression
     *          The expression to build a {@link BooleanTypeAssertion} from, not {@code null}.
     *          {@link #isCandidateForBooleanTypeAssertion(CtExpression)} must return {@code true}
     *          for the given {@code expression}, otherwise a exception will be thrown.
     *
     * @return
     *          The boolean type assertion.
     *
     * @throws IllegalArgumentException
     *          In case {@link #isCandidateForBooleanTypeAssertion(CtExpression)} returns {@code false}
     *          for the given {@code expression}.
     */
    @SuppressWarnings("unchecked")
    public static BooleanTypeAssertion buildBooleanTypeAssertion(CtExpression<?> expression) throws IllegalArgumentException {
        if(!isCandidateForBooleanTypeAssertion(expression)) {
            throw new IllegalArgumentException("The given expression's return type is not compatible!");
        }

        return new BooleanTypeAssertion((CtExpression<Boolean>) expression);
    }

    /**
     *
     * @param primitiveTypeName
     *          The name primitive of the primitive type, not {@code null}. Must be one of <i>boolean, byte, short,
     *          int, long, char, float</i> or <i>double</i> without any leading or trailing whitespace.
     *
     * @return
     *          The type reference for the primitive type identified by its simple name.
     */
    public static CtTypeReference<?> getPrimitiveTypeReference(String primitiveTypeName) {
        switch (primitiveTypeName) {
            case "boolean":
                return TYPE_FACTORY.BOOLEAN_PRIMITIVE;
            case "byte":
                return TYPE_FACTORY.BYTE_PRIMITIVE;
            case "short":
                return TYPE_FACTORY.SHORT_PRIMITIVE;
            case "int":
                return TYPE_FACTORY.INTEGER_PRIMITIVE;
            case "long":
                return TYPE_FACTORY.LONG_PRIMITIVE;
            case "char":
                return TYPE_FACTORY.CHARACTER_PRIMITIVE;
            case "float":
                return TYPE_FACTORY.FLOAT_PRIMITIVE;
            case "double":
                return TYPE_FACTORY.DOUBLE_PRIMITIVE;
            default:
                throw new IllegalArgumentException(String.format("'%s' is not a primitive type!", primitiveTypeName));
        }
    }

}
