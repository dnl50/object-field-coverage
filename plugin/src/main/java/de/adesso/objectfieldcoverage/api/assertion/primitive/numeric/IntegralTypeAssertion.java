package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtExpression;

import java.util.List;

public class IntegralTypeAssertion<S extends Number> extends NumericTypeAssertion<S> {

    private final IntegralTypeAssertionType type;

    public IntegralTypeAssertion(List<Interval<S>> excludedIntervals, IntegralTypeAssertionType type) {
        super(excludedIntervals);

        this.type = type;
    }

    @Override
    protected Fraction calculateExclusionProportion() {
        // TODO implement
        return Fraction.ONE;
    }

    @Override
    public PrimitiveType getAssertedPrimitiveType() {
        return type;
    }


    @Override
    public CtExpression<S> getAssertedExpression() {
        //TODO: implement
        return null;
    }
}
