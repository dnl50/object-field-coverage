package de.adesso.objectfieldcoverage.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

import java.lang.annotation.Annotation;
import java.util.Collection;

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

    /**
     * @param elements
     *          The elements to check, not {@code null}.
     *
     * @param annotation
     *          The class instance of the annotation, not {@code null}.
     *
     * @return
     *          {@code true}, if the at least one of the given {@code element}s is annotated with the given
     *          {@code annotation}. {@code false} is returned otherwise.
     */
    public static boolean isAnnotatedWith(Collection<? extends CtElement> elements, Class<? extends Annotation> annotation) {
        return elements.stream()
                .anyMatch(element -> isAnnotatedWith(element, annotation));
    }

    /**
     *
     * @param element
     *          The element to check the child element of, not {@code null}.
     *
     * @param subElementType
     *          The type of the child elements which should be checked, not {@code null}.
     *
     * @param annotation
     *          The class instance of the annotation, not {@code null}.
     *
     * @return
     *          {@code true}, if at least one child element with matching type of the given {@code element} is
     *          annotated with the given {@code annotation}. {@code false} is returned otherwise.
     */
    public static boolean childElementAnnotatedWith(CtElement element, Class<? extends CtElement> subElementType,
                                                    Class<? extends Annotation> annotation) {
        return element.getElements(new TypeFilter<>(subElementType)).stream()
                .anyMatch(childElement -> isAnnotatedWith(childElement, annotation));
    }

}
