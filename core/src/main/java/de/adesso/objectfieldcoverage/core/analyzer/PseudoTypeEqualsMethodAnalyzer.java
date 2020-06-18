package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import spoon.reflect.declaration.CtClass;

import java.util.Set;

public class PseudoTypeEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    @Override
    public boolean overridesEquals(CtClass<?> clazz) {
        return PrimitiveTypeUtils.isPrimitiveOrWrapperType(clazz.getReference());
    }

    @Override
    protected boolean callsSuperInternal(CtClass<?> clazz) {
        return false;
    }

    @Override
    protected Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtClass<?> clazzOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        return null;
    }

}
