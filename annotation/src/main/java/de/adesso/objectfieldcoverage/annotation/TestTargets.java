package de.adesso.objectfieldcoverage.annotation;

import java.lang.annotation.*;

/**
 * Wrapper annotation for multiple {@link TestTarget} annotations.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface TestTargets {

    /**
     *
     * @return
     *          The test target annotations to specify the targeted executable (methods and constructors).
     */
    TestTarget[] value();

}
