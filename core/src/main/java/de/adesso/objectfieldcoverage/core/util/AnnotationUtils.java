package de.adesso.objectfieldcoverage.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.declaration.CtElement;

import java.lang.annotation.Annotation;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationUtils {

    /**
     * @param element
     *          The element to check, not {@code null}.
     *
     * @param annotation
     *          The class instance of the annotation, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code element} is annotated with the given
     *          {@code annotation}. {@code false} is returned otherwise.
     */
    public static boolean isAnnotatedWith(CtElement element, Class<? extends Annotation> annotation) {
        return element.getAnnotation(annotation) != null;
    }

}
