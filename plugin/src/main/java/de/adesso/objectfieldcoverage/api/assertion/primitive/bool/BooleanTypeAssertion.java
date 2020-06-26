package de.adesso.objectfieldcoverage.api.assertion.primitive.bool;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false)
public class BooleanTypeAssertion extends PrimitiveTypeAssertion<Boolean> {

    /**
     * A boolean flag indicating whether the asserted expression is compared with null.
     */
    private final boolean nullComparison;

    public BooleanTypeAssertion(CtExpression<Boolean> assertedExpression, CtMethod<?> originTestMethod, boolean nullComparison) {
        super(assertedExpression, originTestMethod, PrimitiveType.BOOLEAN);

        this.nullComparison = nullComparison;
    }

    /**
     * Sets the {@link #isNullComparison() null comparison} flag to {@code false}.
     *
     * @param assertedExpression
     *          The asserted boolean expression, not {@code null}.
     *
     * @param originTestMethod
     *          The test method the assertion originates from, not {@code null}.
     */
    public BooleanTypeAssertion(CtExpression<Boolean> assertedExpression, CtMethod<?> originTestMethod) {
        this(assertedExpression, originTestMethod, false);
    }

    /**
     *
     * @param evaluationInformation
     *          The {@link AssertionEvaluationInformation} that contains additional information
     *          about the type of the {@link #getAssertedExpression() asserted expression}.
     *
     * @return
     *          All paths inside the {@link AssertionEvaluationInformation#getAccessibleFieldsGraph() accessible
     *          fields graph} or an empty set in case the asserted expression is a primitive type and it is compared
     *          with {@code null}.
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        if(getAssertedExpression().getType().isPrimitive()) {
            log.warn("Asserted primitive boolean expression '{}' originating from test method '{}' compared with " +
                    "null", getAssertedExpression(), getOriginTestMethod().getSignature());
            return Set.of();
        }

        return evaluationInformation.getAccessibleFieldsGraph()
                .getTransitiveReachabilityPaths();
    }

}
