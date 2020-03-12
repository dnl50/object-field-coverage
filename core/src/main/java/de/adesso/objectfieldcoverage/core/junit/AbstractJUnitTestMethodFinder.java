package de.adesso.objectfieldcoverage.core.junit;

import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A abstract {@link TestMethodFinder} implementation whose {@link #findTestMethods(CtClass)} method
 * is looking for JUnit test methods, including those methods that are declared in inner classes.
 */
public abstract class AbstractJUnitTestMethodFinder implements TestMethodFinder {

    @Override
    public List<CtMethod<?>> findTestMethods(CtClass<?> testClazz) {
        if(Objects.isNull(testClazz)) {
            throw new IllegalArgumentException("testClazz cannot be null!");
        }

        //TODO: scan inner classes as well

        return testClazz.getMethods().stream()
                .filter(testMethodPredicate())
                .collect(Collectors.toList());
    }

    /**
     *
     * @return
     *          A {@link Predicate} that matches a given {@link CtMethod} in case it is
     *          a JUnit test method. Cannot be {@code null}.
     */
    protected abstract Predicate<CtMethod<?>> testMethodPredicate();

}
