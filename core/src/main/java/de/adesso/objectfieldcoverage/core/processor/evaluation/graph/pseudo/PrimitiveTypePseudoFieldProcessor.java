package de.adesso.objectfieldcoverage.core.processor.evaluation.graph.pseudo;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

public class PrimitiveTypePseudoFieldProcessor implements PseudoFieldProcessor {

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} which should be checked for pseudo fields, not {@code null}.
     *
     * @return
     *          The result of {@link PrimitiveTypeUtils#isPrimitiveOrWrapperType(CtTypeReference)}.
     */
    @Override
    public boolean containsPseudoFields(CtTypeReference<?> typeRef) {
        return PrimitiveTypeUtils.isPrimitiveOrWrapperType(typeRef);
    }

    @Override
    public Set<CtField<?>> createPseudoFields(CtTypeReference<?> typeRef) {

    }

    private CtClass<?> getPrimitiveTypePseudoClass(CtTypeReference<?> typeRef) {
        var primitiveTypeSimpleName = StringUtils.capitalize(PrimitiveTypeUtils.getPrimitiveTypeReference(typeRef)
                .getSimpleName());
        var pseudoClassName = String.format("%s%s", primitiveTypeSimpleName, PSEUDO_CLASS_SUFFIX);


    }

}
