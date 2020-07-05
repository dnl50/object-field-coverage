package de.adesso.objectfieldcoverage.api;

/**
 * Functional interface abstraction for implementations whose central method is called one after another with the same
 * argument (e.g. a filter chain where the previous filter decides if the next filter should be called).
 *
 * @param <T>
 *          The type of the object which is passed to the central method of the chainable.
 */
@FunctionalInterface
public interface Chainable<T> {

    /**
     *
     * @param t
     *          The argument which would be passed to the next {@link Chainable}, not {@code null}.
     *
     * @return
     *          {@code true}, if the next {@link Chainable} should be called with the given {@code t}. {@code false}
     *          is returned otherwise.
     */
    boolean callNext(T t);

}
