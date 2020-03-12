package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.fraction.Fraction;

import java.util.List;

@RequiredArgsConstructor
public abstract class NumericTypeAssertion<S extends Number> extends PrimitiveTypeAssertion<S> {

    protected final List<Interval<S>> excludedIntervals;

    @Override
    public Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation) {
        return calculateExclusionProportion();
    }

    protected abstract Fraction calculateExclusionProportion();

}
