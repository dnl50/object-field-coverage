package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtVariableRead;

import java.util.List;
import java.util.Optional;

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
    public Optional<CtVariableRead<S>> getAssertedVariableRead() {
        // TODO implement
        return Optional.empty();
    }

    @Override
    public Optional<CtAbstractInvocation<S>> getAssertedAbstractInvocation() {
        // TODO implement
        return Optional.empty();
    }

}
