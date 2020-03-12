package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

import java.util.List;

@FunctionalInterface
public interface AccessibilityAwareFieldFinder {

    // static final fields can be omitted as they get filtered out afterwards anyway
    List<CtField<?>> findAccessibleFields(CtClass<?> testClazz, CtType<?> type);

}
