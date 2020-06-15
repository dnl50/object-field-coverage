package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import spoon.reflect.code.CtExpression;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PrimitiveTypeAssertion<T> implements AbstractAssertion<T> {

    private final CtExpression<T> assertedExpression;

    @Getter
    private final PrimitiveType primitiveType;

    @Override
    public CtExpression<T> getAssertedExpression() {
        return assertedExpression;
    }

}
