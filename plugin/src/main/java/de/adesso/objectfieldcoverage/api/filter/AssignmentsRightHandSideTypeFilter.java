package de.adesso.objectfieldcoverage.api.filter;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * A {@link TypeFilter} which filters for {@link spoon.reflect.code.CtAssignment}s which used a specific expression
 * on the right hand side of the assignment.
 */
public class AssignmentsRightHandSideTypeFilter<T> extends TypeFilter<CtAssignment<T, ? extends T>> {

    /**
     * The {@link CtExpression} which must match the right-hand-side of an {@link CtAssignment}.
     */
    private CtExpression<?> rightHandSideExpression;

    /**
     *
     * @param rightHandSideExpression
     *          The expression
     */
    public AssignmentsRightHandSideTypeFilter(CtExpression<T> rightHandSideExpression) {
        super(CtAssignment.class);

        this.rightHandSideExpression = rightHandSideExpression;
    }

    /**
     *
     * @param assignment
     *          The {@link CtAssignment} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code assignments} right-hand-side is equal to the configured
     *          expression. {@code false} is returned otherwise.
     */
    @Override
    public boolean matches(CtAssignment<T, ? extends T> assignment) {
        if(!super.matches(assignment)) {
            return false;
        }

        return rightHandSideExpression.equals(assignment.getAssignment());
    }

}
