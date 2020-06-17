package de.adesso.objectfieldcoverage.core.finder.pseudo.generator;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.FieldFactory;
import spoon.reflect.reference.CtTypeReference;

/**
 * A functional interface abstraction for generating pseudo fields in a pseudo class.
 *
 * @see PseudoClassGenerator
 */
@FunctionalInterface
public interface PseudoFieldGenerator {

    /**
     * The suffix of pseudo field created by an implementation.
     */
    String PSEUDO_FIELD_SUFFIX = "___pseudo";

    /**
     *
     * @param fieldFactory
     *          The {@link FieldFactory} to create the {@link CtField} with, not {@code null}.
     *
     * @param pseudoClass
     *          The pseudo {@link CtClass} the fields are declared in, not {@code null}.
     *
     * @param fieldTypeRef
     *          The {@link CtTypeReference} of the type of the field, not {@code null}.
     *
     * @param fieldName
     *          The simple name of the field, not {@code blank}.
     *
     * @param <T>
     *          The type of the field to generate.
     *
     * @return
     *          The generated {@link CtField}.
     */
    <T> CtField<T> generatePseudoField(FieldFactory fieldFactory, CtClass<?> pseudoClass, CtTypeReference<T> fieldTypeRef, String fieldName);

}
