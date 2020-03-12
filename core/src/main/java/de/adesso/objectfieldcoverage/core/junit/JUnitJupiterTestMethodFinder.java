package de.adesso.objectfieldcoverage.core.junit;

import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link AbstractJUnitTestMethodFinder} whose {@link #testMethodPredicate()} method returns
 * a predicate that matches {@link CtMethod}s that are annotated with JUnit Jupiter's
 * {@link Test} annotation.
 */
public class JUnitJupiterTestMethodFinder extends AbstractJUnitTestMethodFinder {

    /**
     *
     * @return
     *          A {@link Predicate} that matches a given {@link CtMethod} in case
     *          it is annotated with JUnit Jupiter's {@link Test} annotation.
     */
    @Override
    protected Predicate<CtMethod<?>> testMethodPredicate() {
        return ctMethod -> Objects.nonNull(ctMethod.getAnnotation(Test.class));
    }

}
