package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * A functional interface abstraction whose implementations are used to identify test methods
 * in a given {@link CtClass}. All implementations must declare a <b>public no-arg</b> constructor.
 */
@FunctionalInterface
public interface TestMethodFinder {

    /**
     *
     * @param testClazz
     *          The {@link CtClass} of which the test methods should be returned, not {@code null}.
     *
     * @return
     *          A list containing the {@link CtMethod}s of the given {@code testClazz} which
     *          are <i>test methods</i>. Cannot be {@code null}.
     */
    List<CtMethod<?>> findTestMethods(CtClass<?> testClazz);

}
