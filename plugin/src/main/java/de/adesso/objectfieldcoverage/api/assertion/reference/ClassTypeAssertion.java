package de.adesso.objectfieldcoverage.api.assertion.reference;

import spoon.reflect.code.CtExpression;

//TODO: JavaDoc
public abstract class ClassTypeAssertion<T> extends ReferenceTypeAssertion<T> {

    public ClassTypeAssertion(CtExpression<T> assertedExpression) {
        super(assertedExpression);
    }

}
