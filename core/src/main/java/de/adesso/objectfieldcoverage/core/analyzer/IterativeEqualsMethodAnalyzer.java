package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

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
     * The fully qualified name of the {@link Object} class.
     */
    private static final String OBJECT_FULLY_QUALIFIED_NAME = "java.lang.Object";

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
     * @param clazz
     *          The {@link CtClass} to analyze the equals method of, not {@code null}.
     *
     * @param accessibleFields
     *          A set containing all accessible fields from which the fields which are used in the equals
     *          method of the given {@code clazz} and its superclasses should be filtered from, not {@code null}.
     *          (POV: clazz having a reference to {@code clazz} instance &rarr; {@code clazz}).
     *
     * @param accessibleFieldsInSuperTypes
     *          A map which contains an entry for for the given {@code clazz} every superclass which is part of the
     *          underlying model itself (excluding {@link Object}). The set to which the {@link CtType} is mapped
     *          must contain all fields which are <i>accessible</i> from the given {@code clazz} (POV: {@code clazz} &rarr;
     *          superclass).
     *
     * @return
     *          An <b>unmodifiable</b> set containing all accessible fields which are part of the given
     *          {@code accessibleFieldsInType} map.
     *
     * @throws IllegalArgumentException
     *          If the {@code accessibleFieldsInSuperTypes} map does not contain the required entries.
     */
    public Set<AccessibleField<?>> findAccessibleFieldsUsedInEquals(CtClass<?> clazz,
                                                                    Set<AccessibleField<?>> accessibleFields,
                                                                    Map<CtType<?>, Set<AccessibleField<?>>> accessibleFieldsInSuperTypes) {
        if(accessibleFields.isEmpty()) {
            return Set.of();
        }

        var superClassesIncludingClass = new LinkedList<>(findSuperClassesExcludingObject(clazz));
        superClassesIncludingClass.addFirst(clazz);

        if(!accessibleFieldsInSuperTypes.keySet().containsAll(superClassesIncludingClass)) {
            throw new IllegalArgumentException("At least one entry in the accessibleFieldsInSuperTypes map does not contain " +
                    "a required entry for the given clazz or a superclass!");
        }

        var accessibleFieldsComparedInEquals = new HashSet<AccessibleField<?>>();
        boolean typeOrSuperTypeOverridesEquals = false;

        for(var currentClass : superClassesIncludingClass) {
            var analyzersForOverriddenEquals = overridesEqualsAnalyzers(currentClass);

            if(!analyzersForOverriddenEquals.isEmpty()) {
                typeOrSuperTypeOverridesEquals = true;

                var comparedFields = findAccessibleFieldsComparedInEquals(analyzersForOverriddenEquals,
                        currentClass, accessibleFields, accessibleFieldsInSuperTypes.get(currentClass));
                accessibleFieldsComparedInEquals.addAll(comparedFields);

                log.info("Equals method in '{}' compares {} fields!", currentClass.getQualifiedName(),
                        comparedFields.size());

                if(!equalsCallsSuper(currentClass)) {
                    log.info("Class '{}' overrides equals without calling its superclass implementation!",
                            currentClass.getQualifiedName());
                    break;
                }

            } else {
                log.info("Class '{}' does not override equals method!", currentClass.getQualifiedName());
            }
        }

        if(!typeOrSuperTypeOverridesEquals) {
            log.info("Equals method not overridden by '{}' or any of its {} superclasses (excluding Object)!",
                    clazz.getQualifiedName(), superClassesIncludingClass.size() - 1);
        }

        log.info("Analyses of class '{}' finished! {} out of {} accessible fields are compared " +
                "in the equals method!", clazz.getQualifiedName(), accessibleFieldsComparedInEquals.size(),
                accessibleFields.size());

        return Set.copyOf(accessibleFieldsComparedInEquals);
    }

    /**
     *
     * @param type
     *          The type to get the superclasses of, not {@code null}.
     *
     * @return
     *          A <b>modifiable</b> list containing all real superclasses of the given {@code type},
     *          excluding {@link Object}.
     */
    private List<CtClass<?>> findSuperClassesExcludingObject(CtType<?> type) {
        var currentSuperClass = findSuperClass(type);
        var superClasses = new ArrayList<CtClass<?>>();

        while(currentSuperClass != null) {
            if(OBJECT_FULLY_QUALIFIED_NAME.equals(currentSuperClass.getQualifiedName())) {
                break;
            }

            superClasses.add(currentSuperClass);
            currentSuperClass = findSuperClass(currentSuperClass);
        }

        return superClasses;
    }

    /**
     *
     * @param type
     *          The type of which the superclass should be retrieved, not {@code null}.
     *
     * @return
     *          The superclass of the given {@code type} or {@code null} if there is no parent
     *          class present in the underlying model.
     */
    private CtClass<?> findSuperClass(CtType<?> type) {
        return type.getSuperclass() != null ? (CtClass<?>) type.getSuperclass().getDeclaration() : null;
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          A list containing a sublist of the registered {@link EqualsMethodAnalyzer}s whose
     *          {@link EqualsMethodAnalyzer#overridesEquals(CtClass)} returns {@code true}.
     */
    private List<EqualsMethodAnalyzer> overridesEqualsAnalyzers(CtClass<?> clazz) {
        return equalsMethodAnalyzers.stream()
                .filter(equalsMethodAnalyzer -> equalsMethodAnalyzer.overridesEquals(clazz))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} which overrides the equals method, not {@code null}.
     *
     * @return
     *          {@code true}, when a registered {@link EqualsMethodAnalyzer}'s
     *          {@link EqualsMethodAnalyzer#overridesEquals(CtClass)} returns {@code true}.
     */
    private boolean equalsCallsSuper(CtClass<?> clazz) {
        return equalsMethodAnalyzers.stream()
                .anyMatch(equalsMethodAnalyzer -> equalsMethodAnalyzer.callsSuper(clazz));
    }


    /**
     * @param analyzersForMethod
     *          The {@link EqualsMethodAnalyzer}s which can analyze the equals method in the given
     *          {@code classOverridingEquals}, not {@code null}.
     *
     * @param classOverridingEquals
     *          The {@link CtClass} to analyze the equals method of, not {@code null}.
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
                                                                         CtClass<?> classOverridingEquals,
                                                                         Set<AccessibleField<?>> accessibleFieldsInRootType,
                                                                         Set<AccessibleField<?>> accessibleFieldsInSuperTypes) {
        var fieldsComparedInEquals = analyzersForMethod.stream()
                .map(equalsMethodAnalyzer -> equalsMethodAnalyzer.findFieldsComparedInEqualsMethod(classOverridingEquals, accessibleFieldsInSuperTypes))
                .flatMap(Collection::stream)
                .map(AccessibleField::getActualField)
                .collect(Collectors.toSet());

        return accessibleFieldsInRootType.stream()
                .filter(accessibleField -> fieldsComparedInEquals.contains(accessibleField.getActualField()))
                .collect(Collectors.toSet());
    }

}
