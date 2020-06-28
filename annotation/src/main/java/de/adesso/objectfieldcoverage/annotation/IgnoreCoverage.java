package de.adesso.objectfieldcoverage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method and type level annotation used to annotate test methods for which the object field coverage should not
 * be calculated. Annotating a type is equivalent to annotating every method in that type.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface IgnoreCoverage {

}
