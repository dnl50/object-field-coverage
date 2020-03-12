package de.adesso.objectfieldcoverage.core.annotation;

import java.lang.annotation.*;

/**
 * Method-level annotation used to mark test methods for which the coverage metric should
 * be calculated.
 */
@Documented
@Repeatable(TestTargets.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface TestTarget {

    /**
     * Specifies the tested method the annotated test method focuses on. Uses
     * the JavaDoc notation. Must be a non-void method iff the {@link #exceptionExpected()}
     * flag is set to {@code false}. All parameter types not in the <em>java.lang</em> package
     * must by fully qualified.
     *
     * <p/>
     *
     * Let's say the tested method {@code max} in a class named {@code Test} in the <em>de.adesso</em> package
     * is defined as follows:
     *
     * <pre>
     *      public int max(int i1, int i2) {
     *          // omitted
     *      }
     * </pre>
     *
     * The corresponding target method identifier would be {@code de.adesso.Test#max(int, int)}.
     *
     * @return
     *          The target method identifier in a JavaDoc notation, can not be empty.
     */
    String value();

    /**
     * Specifies whether an exception is expected from the target method
     * invocation. Default is set to {@code false}.
     *
     * @return
     *          {@code true}, iff an exception is expected from the target method
     *          invocation.
     */
    boolean exceptionExpected() default false;

}
