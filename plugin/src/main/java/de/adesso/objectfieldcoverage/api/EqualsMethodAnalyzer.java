package de.adesso.objectfieldcoverage.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.*;

/**
 * Abstract base class for equals method analysers to find out which fields in a given
 * class are compared in the equals method.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class EqualsMethodAnalyzer {

    /**
     * The fully qualified class name of the {@link Object} class.
     */
    private static final String OBJECT_FULLY_QUALIFIED_NAME = "java.lang.Object";

    /**
     * The given {@code fieldsAccessibleFromType} must include <b>all</b> accessible fields. This
     * includes all transitively accessible fields. An entry must be present for the given {@code clazz}
     * itself and all its super-classes, excluding {@link Object}.
     * <p>
     * Example:
     *
     * Let's say the given {@link CtClass} is this {@code Person} class with a private
     * {@code Address} field.
     * <pre>
     *     public class Person {
     *
     *         private Address address;
     *
     *     }
     * </pre>
     * The {@code Address} field has a public {@code String} field, which is accessible by
     * direct field access (disregarding Modules added in Java 9).
     * <pre>
     *     public class Address {
     *
     *         public String street;
     *
     *     }
     * </pre>
     * The set for the {@code Person} class therefore contains the {@code Address} field (from
     * the {@code Person} class itself) and the {@code String} field (from the {@code Address}
     * class).
     *
     * @param clazz
     *          The type to find the {@link CtField fields} in which are compared in the
     *          types {@link Object#equals(Object)} method
     *
     * @param fieldsAccessibleFromType
     *          A map containing an entry for the given {@code clazz} itself and all super-classes. Each entry
     *          maps the {@link CtType} to a set of all {@link AccessibleField}s which can be accessed from the
     *          type. <b>Must</b> include transitively accessible fields as described above.
     *
     * @return
     *          A set containing all fields of the given {@code clazz} which are compared
     *          in the type's {@link Object#equals(Object)} method.
     */
    public Set<CtField<?>> findFieldsComparedInEqualsMethod(CtClass<?> clazz, Map<CtType<?>, Set<AccessibleField<?>>> fieldsAccessibleFromType) {
        Objects.requireNonNull(clazz, "type cannot be null!");
        Objects.requireNonNull(fieldsAccessibleFromType, "fieldsAccessibleFromType cannot be null!");

        if(!thisOrSuperClassOverridesEquals(clazz)) {
            return Set.of();
        }

        var clazzOverridingEquals = findClassOverridingEquals(clazz);
        var superClasses = getSuperClassesExcludingObject(clazzOverridingEquals);

        if(!fieldsAccessibleFromType.keySet().containsAll(superClasses)) {
            throw new IllegalArgumentException("The given map does not contain all entries for the " +
                    "class and every super-class!");
        }

        if(log.isDebugEnabled() && !clazz.equals(clazzOverridingEquals)) {
            log.debug("Class '{}' does not override equals itself, but its parent class '{}' does!",
                    clazz.getQualifiedName(), clazzOverridingEquals.getQualifiedName());
        }

        return Set.of();
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code clazz} or any super-class (excluding {@link Object})
     *          overrides the {@link Object#equals(Object) equals} method defined in {@link Object}. {@code false}
     *          is returned otherwise.
     */
    private boolean thisOrSuperClassOverridesEquals(CtClass<?> clazz) {
        CtType<?> currentClazz = clazz;

        while(currentClazz != null && !OBJECT_FULLY_QUALIFIED_NAME.equals(currentClazz.getQualifiedName())) {
            if(overridesEquals(currentClazz)) {
                return true;
            }

            currentClazz = currentClazz.getSuperclass().getDeclaration();
        }

        return false;
    }

    /**
     *
     * @param clazz
     *          The class to get the super-classes of, not {@code null}.
     *
     * @return
     *          A list containing the class itself and all super-classes of the given {@code clazz}.
     *          The first element is the given {@code clazz} itself, the second its parent class,
     *          the third. Does not include the {@link CtClass} representation of {@link Object}.
     */
    private List<CtClass<?>> getSuperClassesExcludingObject(CtClass<?> clazz) {
        CtType<?> currentClazz = clazz;
        var superClasses = new ArrayList<CtClass<?>>();

        while(currentClazz != null && !OBJECT_FULLY_QUALIFIED_NAME.equals(currentClazz.getQualifiedName())) {
            superClasses.add((CtClass<?>) currentClazz);

            currentClazz = currentClazz.getSuperclass().getDeclaration();
        }

        return superClasses;
    }

    /**
     *
     * @param clazz
     *          The class to check, not {@code null}.
     *
     * @return
     *          The {@link CtClass} which overrides the {@link Object#equals(Object)} method defined
     *          in {@link Object}. May be the given {@code clazz} or any super-class of the given
     *          {@code clazz}.
     *
     * @throws IllegalStateException
     *          When neither the given {@code clazz} nor any super-class (excluding {@link Object})
     *          overrides the {@link Object#equals(Object)} method defined in {@link Object}.
     */
    private CtClass<?> findClassOverridingEquals(CtClass<?> clazz) {
        CtType<?> currentClazz = clazz;

        while(currentClazz != null && !OBJECT_FULLY_QUALIFIED_NAME.equals(currentClazz.getQualifiedName())) {
            if(overridesEquals(currentClazz)) {
                return (CtClass<?>) currentClazz;
            }

            currentClazz = currentClazz.getSuperclass().getDeclaration();
        }

        throw new IllegalStateException("The clazz nor any super-class overrides Object#equals(Object)!");
    }

    /**
     * Abstract method to check if a given {@link CtType} overrides the {@link Object#equals(Object) equals}
     * method defined {@link Object}. Does <b>not</b> check if a super-class overrides that method.
     *
     * @param type
     *          The {@link CtType} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} overrides the {@link Object#equals(Object) equals}
     *          method defined in {@link Object}. {@code false} is returned otherwise.
     */
    protected abstract boolean overridesEquals(CtType<?> type);

    /**
     *
     * @param equalsMethod
     *          The equals method to check, not {@code null}.
     *
     * @return
     *          {@code true} if the {@link Object#equals(Object) equals} method of the super-class
     *          is called inside the given {@code equalsMethod}. {@code false} is returned
     *          otherwise.
     */
    protected abstract boolean callsSuper(CtMethod<Boolean> equalsMethod);

    /**
     *
     * @param clazzOverridingEquals
     *          The {@link CtClass} which overrides the {@link Object#equals(Object) equals} method
     *          defined in {@link Object}, not {@code null}.
     *
     * @param accessibleFields
     *          The fields of the declared in the class itself and in all super-classes which can be accessed
     *          in the equals method of the given {@code clazzOverridingEquals}, not {@code null}.
     *
     * @return
     *          A set containing all fields which are compared inside the given {@code clazzOverridingEquals}
     *          equals method.
     */
    protected abstract Set<CtField<?>> findFieldsComparedInEqualsMethodInternal(CtClass<?> clazzOverridingEquals,
                                                                                Set<AccessibleField<?>> accessibleFields);

}
