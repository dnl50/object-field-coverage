package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveType;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeAssertion;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

public class NumericTypeAssertion<T extends Number> extends PrimitiveTypeAssertion<T> {

    protected NumericTypeAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod, PrimitiveType primitiveType) {
        super(assertedExpression, originTestMethod, primitiveType);
    }

    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        return null;
    }

}
