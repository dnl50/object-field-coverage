package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.api.filter.VariableWithDefaultExpressionFilter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtVariable;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VariableUtils {

    /**
     *
     * @param variableType
     *          The class instance of the {@link CtVariable} subtype, not {@code null}.
     *
     * @param defaultExpression
     *          The default expression to find variables with matching default expression with. May be null.
     *
     * @param root
     *          The {@link CtElement} to get the child variables with a matching default expression from, not
     *          {@code null}.
     *
     * @param <T>
     *          The subtype of {@link CtVariable} returned.
     *
     * @param <E>
     *          The expressions return type.
     *
     * @param <V>
     *          The variables type.
     *
     * @return
     *          A list containing all variables with a matching default expression.
     *
     * @see VariableWithDefaultExpressionFilter
     */
    public static <T extends CtVariable<V>, E extends V, V> List<T> findVariablesWithDefaultExpression(Class<T> variableType,
                                                                                                       CtExpression<E> defaultExpression,
                                                                                                       CtElement root) {
        return root.getElements(new VariableWithDefaultExpressionFilter<T>(variableType, defaultExpression));
    }
    
}
