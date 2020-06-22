package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combines multiple {@link EqualsMethodAnalyzer}s to walk up the super-class hierarchy of a given {@link CtClass}
 * to find out which {@link AccessibleField}s are compared in the equals methods.
 */
@Slf4j
@RequiredArgsConstructor
public class IterativeEqualsMethodAnalyzer {

    /**
     * A list of equals method analyzers which will be to analyze the equals methods
     * while walking up the super-class hierarchy of a given {@link CtClass}.
     */
    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    /**
     * Walks up the parent class hierarchy until either the {@link CtClass} representation of {@link Object}
     * is reached or the parent class is not part of the underlying model. Analysis is stopped abruptly in
     * case a {@code equals} method in the hierarchy does not call its superclass implementation.
     *
     * @param classRefToAnalyze
     *          The {@link CtTypeReference} of the class to analyze the equals method of, not {@code null}.
     *
     * @param accessibleFields
     *          A set containing all accessible fields from which the fields which are used in the equals
     *          method of the given {@code classToAnalyze} and its superclasses should be filtered from, not {@code null}.
     *          (POV: class having a reference to {@code classToAnalyze} instance &rarr; {@code classToAnalyze}).
     *
     * @param accessibleFieldsInSuperTypes
     *          A map which contains an entry for the given {@code classRefToAnalyze} and every superclass (excluding {@link Object}).
     *          The set to which the {@link CtType} is mapped must contain all fields which are <i>accessible</i> from
     *          the given {@code classRefToAnalyze} (POV: {@code classRefToAnalyze} &rarr; superclass).
     *
     * @return
     *          An <b>unmodifiable</b> set containing all accessible fields which are part of the given
     *          {@code accessibleFieldsInType} map.
     *
     * @throws IllegalArgumentException
     *          If the {@code accessibleFieldsInSuperTypes} map does not contain the required entries.
     */
    public Set<AccessibleField<?>> findAccessibleFieldsUsedInEquals(CtTypeReference<?> classRefToAnalyze,
                                                                    Set<AccessibleField<?>> accessibleFields,
                                                                    Map<CtTypeReference<?>, Set<AccessibleField<?>>> accessibleFieldsInSuperTypes) {
        if(accessibleFields.isEmpty()) {
            return Set.of();
        }

        var superClassRefsIncludingClass = TypeUtils.findExplicitSuperClassesIncludingClass(classRefToAnalyze);

        if(!accessibleFieldsInSuperTypes.keySet().containsAll(superClassRefsIncludingClass)) {
            throw new IllegalArgumentException("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                    "a required entry for the given class or a superclass!");
        }

        var accessibleFieldsComparedInEquals = new HashSet<AccessibleField<?>>();
        boolean typeOrSuperTypeOverridesEquals = false;

        for(var currentClassRef : superClassRefsIncludingClass) {
            var analyzersForOverriddenEquals = analyzersSayingTypeOverridesEquals(currentClassRef);

            if(!analyzersForOverriddenEquals.isEmpty()) {
                typeOrSuperTypeOverridesEquals = true;

                var comparedFields = findAccessibleFieldsComparedInEquals(analyzersForOverriddenEquals,
                        currentClassRef, accessibleFields, accessibleFieldsInSuperTypes.get(currentClassRef));
                accessibleFieldsComparedInEquals.addAll(comparedFields);

                log.info("Equals method in '{}' compares {} fields!", currentClassRef.getQualifiedName(),
                        comparedFields.size());

                if(!equalsCallsSuper(currentClassRef)) {
                    log.info("Class '{}' overrides equals without calling its superclass implementation!",
                            currentClassRef.getQualifiedName());
                    break;
                }

            } else {
                log.info("Class '{}' does not override equals method!", currentClassRef.getQualifiedName());
            }
        }

        if(!typeOrSuperTypeOverridesEquals) {
            log.info("Equals method not overridden by '{}' or any of its {} superclasses (excluding Object)!",
                    classRefToAnalyze.getQualifiedName(), superClassRefsIncludingClass.size() - 1);
        }

        log.info("Analyses of class '{}' finished! {} out of {} accessible fields are compared " +
                "in the equals method!", classRefToAnalyze.getQualifiedName(), accessibleFieldsComparedInEquals.size(),
                accessibleFields.size());

        return Set.copyOf(accessibleFieldsComparedInEquals);
    }

    /**
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to check, not {@code null}.
     *
     * @return
     *          A list containing a sublist of the registered {@link EqualsMethodAnalyzer}s whose
     *          {@link EqualsMethodAnalyzer#overridesEquals(CtTypeReference)} returns {@code true}.
     */
    private List<EqualsMethodAnalyzer> analyzersSayingTypeOverridesEquals(CtTypeReference<?> clazzRef) {
        return equalsMethodAnalyzers.stream()
                .filter(equalsMethodAnalyzer -> equalsMethodAnalyzer.overridesEquals(clazzRef))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param clazzRef
     *          The {@link CtClass} which overrides the equals method, not {@code null}.
     *
     * @return
     *          {@code true}, when a registered {@link EqualsMethodAnalyzer}'s
     *          {@link EqualsMethodAnalyzer#overridesEquals(CtTypeReference)} returns {@code true}.
     */
    private boolean equalsCallsSuper(CtTypeReference<?> clazzRef) {
        return equalsMethodAnalyzers.stream()
                .anyMatch(equalsMethodAnalyzer -> equalsMethodAnalyzer.callsSuper(clazzRef));
    }


    /**
     * @param analyzersForMethod
     *          The {@link EqualsMethodAnalyzer}s which can analyze the equals method in the given
     *          {@code classOverridingEquals}, not {@code null}.
     *
     * @param classRefOverridingEquals
     *          The {@link CtTypeReference} to analyze the equals method of, not {@code null}.
     *
     * @param accessibleFieldsInRootType
     *          A set containing all accessible fields from which the field which are used in the equals
     *          method of the given {@code classOverridingEquals} should be filtered from, not {@code null}.
     *
     * @param accessibleFieldsInSuperTypes
     *          A map which contains an entry for for the given {@code clazz} every superclass which is part of the
     *          underlying model itself (excluding {@link Object}). The set to which the {@link CtType} is mapped
     *          must contain all fields which are <i>accessible</i> from the given {@code clazz}.
     *
     * @return
     *          An <b>unmodifiable</b> set containing all accessible fields which are part of the given
     *          {@code accessibleFieldsInType} map.
     *
     * @throws IllegalArgumentException
     *          If the {@code accessibleFieldsInSuperTypes} map does not contain the required entries.
     */
    private Set<AccessibleField<?>> findAccessibleFieldsComparedInEquals(List<EqualsMethodAnalyzer> analyzersForMethod,
                                                                         CtTypeReference<?> classRefOverridingEquals,
                                                                         Set<AccessibleField<?>> accessibleFieldsInRootType,
                                                                         Set<AccessibleField<?>> accessibleFieldsInSuperTypes) {
        var fieldsComparedInEquals = analyzersForMethod.stream()
                .map(equalsMethodAnalyzer -> equalsMethodAnalyzer.findFieldsComparedInEqualsMethod(classRefOverridingEquals, accessibleFieldsInSuperTypes))
                .flatMap(Collection::stream)
                .map(AccessibleField::getActualField)
                .collect(Collectors.toSet());

        return accessibleFieldsInRootType.stream()
                .filter(accessibleField -> fieldsComparedInEquals.contains(accessibleField.getActualField()))
                .collect(Collectors.toSet());
    }

}
