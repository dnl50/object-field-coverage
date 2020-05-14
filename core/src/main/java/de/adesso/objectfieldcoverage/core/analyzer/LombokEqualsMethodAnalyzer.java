package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Set;

public class LombokEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    /**
     * Checks if the given {@link CtType} is annotated with Lombok's {@link Data} or
     * {@link EqualsAndHashCode} annotation.
     *
     * @param type
     *          The {@link CtType} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} is annotated with {@link Data} or
     *          {@link EqualsAndHashCode}.
     */
    @Override
    protected boolean overridesEquals(CtType<?> type) {
        return type.getAnnotation(Data.class) != null ||
                type.getAnnotation(EqualsAndHashCode.class) != null;
    }

    /**
     *
     * @param equalsMethod
     *          The equals method to check, not {@code null}.
     *
     * @return
     *         {@code true}, if the declaring type of the given {@code equalsMethod} is annotated with
     *         {@link EqualsAndHashCode} and the {@link EqualsAndHashCode#callSuper() call super} flag
     *         is set to {@code true}. {@code false} is returned otherwise.
     */
    @Override
    protected boolean callsSuper(CtMethod<Boolean> equalsMethod) {
        var equalsAndHashCodeAnnotation = equalsMethod.getDeclaringType()
                .getAnnotation(EqualsAndHashCode.class);

        return equalsAndHashCodeAnnotation != null && equalsAndHashCodeAnnotation.callSuper();
    }

    @Override
    protected Set<CtField<?>> findFieldsComparedInEqualsMethodInternal(CtClass<?> clazzOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        return null;
    }

}
