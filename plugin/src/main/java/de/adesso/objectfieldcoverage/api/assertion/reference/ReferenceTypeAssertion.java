package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

@RequiredArgsConstructor
public abstract class ReferenceTypeAssertion<T> implements AbstractAssertion<T> {

    protected final CtExpression<T> assertedExpression;

    protected final CtMethod<?> originTestMethod;

    @Override
    public CtExpression<T> getAssertedExpression() {
        return assertedExpression;
    }

    @Override
    public CtMethod<?> getOriginTestMethod() {
        return originTestMethod;
    }

}
