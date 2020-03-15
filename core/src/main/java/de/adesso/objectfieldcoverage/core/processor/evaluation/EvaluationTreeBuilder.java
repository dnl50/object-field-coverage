package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.evaluation.AssertedField;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

import java.util.List;

public class EvaluationTreeBuilder {

    private final List<AccessibilityAwareFieldFinder> fieldFinders = List.of();

    public AssertionEvaluationInformation buildEvaluationInformation(CtType<?> assertedType, CtClass<?> testClazz) {
        return null;
    }

    private AssertedField<?> buildAssertedField(CtField<?> field) {
        return null;
    }

}
