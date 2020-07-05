package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

@EqualsAndHashCode
@RequiredArgsConstructor
public abstract class ReferenceTypeAssertion<T> implements AbstractAssertion<T> {

    /**
     * The expression which is asserted by this assertion.
     */
    protected final CtExpression<T> assertedExpression;

    /**
     *  The {@link CtMethod} this assertion originates from.
     */
    protected final CtMethod<?> originTestMethod;

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

}
