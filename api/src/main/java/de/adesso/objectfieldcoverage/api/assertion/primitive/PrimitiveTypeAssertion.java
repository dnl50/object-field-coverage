package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

//TODO: Test

/**
 * {@link AbstractAssertion} implementation used for primitive types and their wrapper types.
 *
 * @param <T>
 *          The return type of the asserted expression
 */
@Getter
@EqualsAndHashCode
public class PrimitiveTypeAssertion<T> implements AbstractAssertion<T> {

    /**
     * The expression which is asserted by this assertion.
     */
    private final CtExpression<T> assertedExpression;

    /**
     *  The {@link CtMethod} this assertion originates from.
     */
    private final CtMethod<?> originTestMethod;

    /**
     * A boolean flag indicating whether the primitive wrapper types where
     * expression result was compared with the == operator.
     */
    private boolean equalityAsserted;

    /**
     * The {@link PrimitiveType} of this assertion.
     */
    private final PrimitiveType primitiveType;

    /**
     *
     * @param assertedExpression
     *          The asserted expression, must return a primitive or wrapper type, not {@code null}.
     *
     * @param originTestMethod
     *          The {@link CtMethod} this assertion originates from, not {@code null}.
     *
     * @param equalityAsserted
     *          A boolean flag indicating whether the expression was compared using the == operator.
     */
    public PrimitiveTypeAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod, boolean equalityAsserted) {
        this.assertedExpression = assertedExpression;
        this.originTestMethod = originTestMethod;
        this.equalityAsserted = equalityAsserted;

        var primitiveTypeSimpleName = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedExpression.getType())
                .getSimpleName();
        this.primitiveType = PrimitiveType.of(primitiveTypeSimpleName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CtExpression<T> getAssertedExpression() {
        return assertedExpression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CtMethod<?> getOriginTestMethod() {
        return originTestMethod;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          A set containing all paths in the given node graph or an empty set when the
     *          expression is asserted using reference
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        var isPrimitiveTypeAsserted = assertedExpression.getType()
                .isPrimitive();

        if(!equalityAsserted || isPrimitiveTypeAsserted || primitiveType.isReferenceEqualitySupported()) {
            return evaluationInformation.getAccessibleFieldsGraph()
                    .getTransitiveReachabilityPaths();
        }

        return Set.of();
    }

}
