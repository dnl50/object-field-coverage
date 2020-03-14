package de.adesso.objectfieldcoverage.core.junit;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.params.ParameterizedTest;
import spoon.reflect.declaration.CtMethod;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * {@link AbstractJUnitTestMethodFinder} whose {@link #testMethodPredicate()} method returns
 * a predicate that matches non-private {@link CtMethod}s that are annotated with JUnit Jupiter's
 * {@link Test}, {@link RepeatedTest}, {@link ParameterizedTest}, {@link TestFactory} or
 * {@link TestTemplate} annotation.
 */
public class JUnitJupiterTestMethodFinder extends AbstractJUnitTestMethodFinder {

    /**
     * A set containing all JUnit Jupiter annotations that make a method a <i>test method</i>. See section
     * 2.2 of the JUnit Jupiter user guide for more information.
     */
    private static final Set<Class<? extends Annotation>> JUNIT_JUPITER_TEST_METHOD_ANNOTATIONS = Set.of(Test.class,
            RepeatedTest.class, ParameterizedTest.class, TestFactory.class, TestTemplate.class);

    /**
     *
     * @return
     *          A {@link Predicate} that matches a given {@link CtMethod} in case
     *          it is annotated with JUnit Jupiter's {@link Test} annotation and the method
     *          is <b>not</b> declared <i>private</i>.
     */
    @Override
    protected Predicate<CtMethod<?>> testMethodPredicate() {
        return ctMethod -> isAnnotatedWithTestMethodAnnotation(ctMethod) && !ctMethod.isPrivate();
    }

    /**
     *
     * @param method
     *          The method which should be checked, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code method} is annotated with an annotation contained
     *          in the {@link #JUNIT_JUPITER_TEST_METHOD_ANNOTATIONS} set. {@code false} is returned
     *          otherwise.
     */
    private boolean isAnnotatedWithTestMethodAnnotation(CtMethod<?> method) {
        return JUNIT_JUPITER_TEST_METHOD_ANNOTATIONS.stream()
                .anyMatch(annotation -> Objects.nonNull(method.getAnnotation(annotation)));
    }

}
