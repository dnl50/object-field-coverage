package de.adesso.objectfieldcoverage.api.filter;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * {@link TypeFilter} extension which filters for {@link CtVariable}s which have a specific default expression.
 *
 * @param <T>
 *          The variable type.
 */
public class VariableWithDefaultExpressionFilter<T extends CtVariable<?>> extends TypeFilter<T> {

    /**
     * The {@link CtExpression} to compare the default expression of a variable with, not {@code null}.
     */
    private final CtExpression<?> defaultExpression;

    /**
     *
     * @param type
     *          The class reference of the variable type, not {@code null}.
     *
     * @param defaultExpression
     *          The {@link CtExpression} to which the default expression of a given {@link CtVariable} must
     *          be equal. Passing {@code null} means that variables with no default expression are
     *          filtered.
     */
    public VariableWithDefaultExpressionFilter(Class<T> type, CtExpression<?> defaultExpression) {
        super(type);

        this.defaultExpression = defaultExpression;
    }

    /**
     *
     * @param variable
     *          The {@link CtVariable} to check the default expression of, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@link CtVariable}s default expression is equal to the configured
     *          default expression. {@code false} is returned otherwise.
     */
    @Override
    public boolean matches(T variable) {
        if(!super.matches(variable)) {
            return false;
        }

        var variableDefaultExpression = variable.getDefaultExpression();

        if(defaultExpression == null) {
            return variableDefaultExpression == null;
        } else {
            return defaultExpression.equals(variableDefaultExpression);
        }
    }

}
