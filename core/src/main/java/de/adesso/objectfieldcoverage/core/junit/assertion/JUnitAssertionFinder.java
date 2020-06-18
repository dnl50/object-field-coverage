package de.adesso.objectfieldcoverage.core.junit.assertion;

import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

public class JUnitAssertionFinder implements AssertionFinder {

    @Override
    public List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod, List<CtMethod<?>> invokedHelperMethods) {
        return List.of();
    }

}
