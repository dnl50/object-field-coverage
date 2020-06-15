package de.adesso.objectfieldcoverage.core.processor.evaluation.graph.pseudo;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

/**
 * A functional interface abstraction for generating pseudo fields in a pseudo class.
 */
@FunctionalInterface
public interface PseudoFieldGenerator {

    /**
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
     * @return
     *          The generated {@link CtField}.
     */
    CtField<?> generatePseudoField(CtClass<?> pseudoClass, CtTypeReference<?> fieldTypeRef, String fieldName);

}
