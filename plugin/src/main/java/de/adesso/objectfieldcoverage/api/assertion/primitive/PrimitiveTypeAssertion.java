package de.adesso.objectfieldcoverage.api.assertion.primitive;

import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;

public abstract class PrimitiveTypeAssertion<T> implements AbstractAssertion<T> {

    public abstract PrimitiveType getAssertedPrimitiveType();

}
