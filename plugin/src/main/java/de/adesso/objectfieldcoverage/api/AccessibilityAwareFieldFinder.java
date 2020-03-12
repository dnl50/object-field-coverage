package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

import java.util.List;

@FunctionalInterface
public interface AccessibilityAwareFieldFinder {

    List<CtField<?>> findAccessibleFields(CtClass<?> testClazz, CtType<?> type);

}
