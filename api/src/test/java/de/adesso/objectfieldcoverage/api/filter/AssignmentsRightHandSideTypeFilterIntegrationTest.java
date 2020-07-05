package de.adesso.objectfieldcoverage.api.filter;

import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.visitor.filter.TypeFilter;

import static org.assertj.core.api.Assertions.assertThat;

class AssignmentsRightHandSideTypeFilterIntegrationTest extends AbstractSpoonIntegrationTest {

    @Test
    void matchesReturnsTrueWhenLocalVariableIsAssignedExpressionResult() {
        // given
        var model = buildModel("filter/AssignmentFilterTest.java");
        var testMethod = findClassWithSimpleName(model, "AssignmentFilterTest")
                .getMethod("localVariableAssignments");
        var integerMaxExpression = testMethod.getElements(new TypeFilter<CtExpression<Integer>>(CtExpression.class))
                .get(2);
        var assignmentOfX = testMethod.getElements(new TypeFilter<CtAssignment<Integer, Integer>>(CtAssignment.class))
                .get(0);

        var testSubject = new AssignmentsRightHandSideTypeFilter<>(integerMaxExpression);

        // when
        var actualResult = testSubject.matches(assignmentOfX);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void matchesReturnsFalseWhenLocalVariableIsNotAssignedExpressionResult() {
        // given
        var model = buildModel("filter/AssignmentFilterTest.java");
        var testMethod = findClassWithSimpleName(model, "AssignmentFilterTest")
                .getMethod("localVariableAssignments");
        var integerMinExpression = testMethod.getElements(new TypeFilter<CtExpression<Integer>>(CtExpression.class))
                .get(8);
        var assignmentOfX = testMethod.getElements(new TypeFilter<CtAssignment<Integer, Integer>>(CtAssignment.class))
                .get(0);

        var testSubject = new AssignmentsRightHandSideTypeFilter<>(integerMinExpression);

        // when
        var actualResult = testSubject.matches(assignmentOfX);

        // then
        assertThat(actualResult).isFalse();
    }

}
