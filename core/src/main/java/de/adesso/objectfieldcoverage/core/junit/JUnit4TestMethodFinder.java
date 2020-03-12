package de.adesso.objectfieldcoverage.core.junit;

import spoon.reflect.declaration.CtMethod;

import java.util.function.Predicate;

public class JUnit4TestMethodFinder extends AbstractJUnitTestMethodFinder {

    @Override
    protected Predicate<CtMethod<?>> testMethodPredicate() {
        return null;
    }

}
