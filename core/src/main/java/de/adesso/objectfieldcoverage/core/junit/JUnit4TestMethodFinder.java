package de.adesso.objectfieldcoverage.core.junit;

import org.junit.Test;
import spoon.reflect.declaration.CtMethod;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * {@link AbstractJUnitTestMethodFinder} whose {@link #testMethodPredicate()} method returns
 * a predicate that matches public {@link CtMethod}s that are annotated with JUnit 4's
 * {@link org.junit.Test} annotation.
 */
public class JUnit4TestMethodFinder extends AbstractJUnitTestMethodFinder {

    /**
     *
     * @return
     *          A {@link Predicate} that matches a given {@link CtMethod} in case
     *          it is annotated with JUnit 4's {@link org.junit.Test} annotation and the
     *          method is declared <i>public</i>.
     */
    @Override
    protected Predicate<CtMethod<?>> testMethodPredicate() {
        return (ctMethod) -> Objects.nonNull(ctMethod.getAnnotation(Test.class)) && ctMethod.isPublic();
    }

}
