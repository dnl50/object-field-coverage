package de.adesso.objectfieldcoverage.core.finder.pseudo.generator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.FieldFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * {@link PseudoFieldGenerator} implementation using spoons {@link FieldFactory} for creating {@link CtField}s.
 */
@Slf4j
public class PseudoFieldGeneratorImpl implements PseudoFieldGenerator {

    /**
     * <b>Note:</b> This implementation does not verify that a field with the same name already exists on the
     * given {@code pseudoClass}.
     *
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException
     *          When the given {@code fieldName} is blank.
     */
    @Override
    public <T> CtField<T> generatePseudoField(FieldFactory fieldFactory, CtClass<?> pseudoClass, CtTypeReference<T> fieldTypeRef, String fieldName) {
        if(StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("The field name cannot be blank!");
        }

        return fieldFactory.create(pseudoClass, Set.of(ModifierKind.PUBLIC), fieldTypeRef, fieldName);
    }

}
