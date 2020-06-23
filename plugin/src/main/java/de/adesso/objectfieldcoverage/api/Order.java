package de.adesso.objectfieldcoverage.api;

import java.lang.annotation.*;

/**
 * Type level annotation for annotating interface / abstract class implementations for basic ordering of implementations
 * when requesting a list of all available implementations of the interface / abstract class.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {

    /**
     * An int value indicating that the implementation should be positioned at the end
     * of the list.
     */
    int LOWEST = Integer.MIN_VALUE;

    /**
     * An int value indicating that the implementation should be in the middle of the list.
     */
    int DEFAULT = 0;

    /**
     * An int value indicating that the implementation should be positioned at the beginning
     * of the list.
     */
    int HIGHEST = Integer.MAX_VALUE;

    /**
     *
     * @return
     *          An integer indicating the order of the implementation. Higher values indicate a higher
     *          priority.
     */
    int value() default DEFAULT;

}
