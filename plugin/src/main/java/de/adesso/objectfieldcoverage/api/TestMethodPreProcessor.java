package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtMethod;

/**
 * Functional interface abstraction for a preprocessor for {@link CtMethod}s which are identified as
 * <i>test methods</i>.
 */
@FunctionalInterface
public interface TestMethodPreProcessor {

    /**
     *
     * @param testMethod
     *          The {@link CtMethod} to process, not {@code null}.
     *
     * @param <T>
     *          The return type of the given {@code testMethod}.
     *
     * @return
     *          The processed {@link CtMethod}.
     */
    <T> CtMethod<T> processTestMethod(CtMethod<T> testMethod);

}
