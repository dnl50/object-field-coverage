package de.adesso.objectfieldcoverage.api.assertion.primitive.bool;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.fraction.Fraction;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtVariableRead;

import java.util.Optional;

@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BooleanTypeAssertion extends PrimitiveTypeAssertion<Boolean> {

    private final CtVariableRead<Boolean> assertedVariableRead;

    private final CtAbstractInvocation<Boolean> assertedInvocation;

    @Override
    public PrimitiveType getAssertedPrimitiveType() {
        return () -> "boolean";
    }

    @Override
    public Optional<CtVariableRead<Boolean>> getAssertedVariableRead() {
        return Optional.ofNullable(assertedVariableRead);
    }

    @Override
    public Optional<CtAbstractInvocation<Boolean>> getAssertedAbstractInvocation() {
        return Optional.ofNullable(assertedInvocation);
    }

    @Override
    public Fraction calculateMetricValue(AssertionEvaluationInformation evaluationInformation) {
        return Fraction.ONE;
    }

}
