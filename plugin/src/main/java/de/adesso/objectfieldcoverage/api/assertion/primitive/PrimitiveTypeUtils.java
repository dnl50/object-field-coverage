package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.primitive.bool.BooleanTypeAssertion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.TypeFactory;

import java.util.Objects;

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

}
