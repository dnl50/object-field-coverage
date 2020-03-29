package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtExpression;

//TODO: JavaDoc
@RequiredArgsConstructor
public abstract class ReferenceTypeAssertion<T> implements AbstractAssertion<T> {

    protected final CtExpression<T> assertedExpression;

    @Override
    public CtExpression<T> getAssertedExpression() {
        return assertedExpression;
    }

}
