package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Set;

/**
 * Functional interface abstraction for finding the target {@link CtExecutable}s of a given test method.
 * Multiple executables might be returned since a single test method might target multiple executables.
 */
@FunctionalInterface
public interface TargetExecutableFinder {

    /**
     *
     * @param testMethod
     *          The test method for which the target executables should be found, not {@code null}.
     *
     * @param helperMethods
     *          All helper methods which are invoked inside the given {@code testMethod}, not {@code null}.
     *
     * @return
     *          A set containing all executables which are targeted by the given {@code testMethod}. The
     *          executables do not have to be invoked.
     */
    Set<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods);

}
