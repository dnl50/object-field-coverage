package de.adesso.objectfieldcoverage.core.analyzer.lombok;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link EqualsMethodAnalyzer} for Lombok annotated classes. Supports Lombok's {@link lombok.Data}
 * and {@link EqualsAndHashCode} annotations.
 */
@Slf4j
public class LombokEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}. Must ba a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, if the given {@code type} is annotated with {@link Data} or
     *          {@link EqualsAndHashCode}.
     */
    @Override
    public boolean overridesEquals(CtClass<?> clazz) {
        return clazz.getAnnotation(Data.class) != null ||
                clazz.getAnnotation(EqualsAndHashCode.class) != null;
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code clazz} is annotated with Lombok's {@link EqualsAndHashCode}
     *          annotation and the {@link EqualsAndHashCode#callSuper() call super} flag is set. {@code false}
     *          is returned otherwise.
     */
    @Override
    protected boolean callsSuperInternal(CtClass<?> clazz) {
        var equalsAndHashCodeAnnotation = clazz.getAnnotation(EqualsAndHashCode.class);
        return equalsAndHashCodeAnnotation != null && equalsAndHashCodeAnnotation.callSuper();
    }

    /**
     *
     * @param clazzOverridingEquals
     *          The {@link CtClass} which overrides the equals method declared in {@link Object#equals(Object)},
     *          not {@code null}. The {@link #overridesEquals(CtClass)} method must return {@code true}.
     *
     * @param accessibleFields
     *          The fields of the declared in the class itself and in all super-classes which can be accessed
     *          in the equals method of the given {@code clazzOverridingEquals}, not {@code null}.
     *
     * @return
     *          A <b>unmodifiable</b> set containing all fields which are used in Lombok's generated
     *          getter method.
     */
    @Override
    public Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtClass<?> clazzOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        var fieldsDeclaredInDeclaringType = clazzOverridingEquals.getFields();
        var accessibleFieldsDeclaredInType = accessibleFields.stream()
                .filter(accessibleField -> fieldsDeclaredInDeclaringType.contains(accessibleField.getActualField()))
                .collect(Collectors.toSet());
        var equalsAndHashCodeAnnotation = clazzOverridingEquals.getAnnotation(EqualsAndHashCode.class);
        var onlyIncludeExplicit = equalsAndHashCodeAnnotation != null && equalsAndHashCodeAnnotation.onlyExplicitlyIncluded();
        var excludedNames = equalsAndHashCodeAnnotation != null ? Arrays.asList(equalsAndHashCodeAnnotation.exclude()) : List.of();

        if(onlyIncludeExplicit) {
            log.info("Declaring type '{}' only includes explicitly annotated fields in its generated " +
                    "equals method!", clazzOverridingEquals.getQualifiedName());

            var includedFields = fieldsDeclaredInDeclaringType.stream()
                .filter(field -> field.getAnnotation(EqualsAndHashCode.Include.class) != null)
                .collect(Collectors.toSet());

            return accessibleFieldsDeclaredInType.stream()
                .filter(accessibleField -> includedFields.contains(accessibleField.getActualField()))
                .collect(Collectors.toSet());
        }

        var excludedFields = fieldsDeclaredInDeclaringType.stream()
                .filter(field -> excludedNames.contains(field.getSimpleName()) || field.getAnnotation(EqualsAndHashCode.Exclude.class) != null)
                .collect(Collectors.toSet());

        log.info("Declaring type '{}' excludes the following fields from its generated equals " +
                "method: {}", clazzOverridingEquals.getQualifiedName(), excludedFields);

        return accessibleFieldsDeclaredInType.stream()
                .filter(accessibleField -> !excludedFields.contains(accessibleField.getActualField()))
                .collect(Collectors.toSet());
    }

}
