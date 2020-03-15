package de.adesso.objectfieldcoverage.core.junit.assertion;

import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.core.junit.assertion.filter.JUnitAssertEqualsInvocationFilter;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.stream.Collectors;

public class JUnitAssertionFinder implements AssertionFinder {

    @Override
    public List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod) {
        return testMethod.getElements(new JUnitAssertEqualsInvocationFilter()).stream()
                .map(this::createAbstractAssertion)
                .collect(Collectors.toList());
    }

    private AbstractAssertion<?> createAbstractAssertion(CtInvocation<Void> invocation) {
        //TODO:
        // - implement for other types than boolean/Boolean
        // - take other assertEquals signatures into account
        // - take assertFalse/assertTrue/assertNull/assertNotNull into account

        var invocationArgs = invocation.getArguments();
        var assertedExpression = invocationArgs.get(1);

        if(PrimitiveTypeUtils.isCandidateForBooleanTypeAssertion(assertedExpression)) {
            return PrimitiveTypeUtils.buildBooleanTypeAssertion(assertedExpression);
        }

        throw new IllegalStateException();
    }

}
