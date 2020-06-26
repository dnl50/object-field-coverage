package de.adesso.objectfieldcoverage.api.filter;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * A {@link TypeFilter} which filters for {@link spoon.reflect.code.CtAssignment}s which used a specific expression
 * on the right hand side of the assignment.
 */
public class AssignmentsRightHandSideTypeFilter<T> extends TypeFilter<CtAssignment<T, ? extends T>> {

    private CtExpression<?> rightHandSideExpression;

    /**
     *
     * @param rightHandSideExpression
     *          The expression
     */
    public AssignmentsRightHandSideTypeFilter(CtExpression<?> rightHandSideExpression) {
        super(CtAssignment.class);

        this.rightHandSideExpression = rightHandSideExpression;
    }

}
