package de.adesso.objectfieldcoverage.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;

import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for equals method analysers to find out which fields in a given
 * class are compared in the equals method. Only takes fields into account which are declared
 * in the class itself or any superclass.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class EqualsMethodAnalyzer {

    /**
     * The simple name of the {@link Object#equals(Object)} method.
     */
    private static final String EQUALS_METHOD_SIMPLE_NAME = "equals";

    /**
     * Analyzes the equals method of a single {@link CtType} and checks whether an <i>accessible</i>
     * {@link CtField} declared in the type itself or any super-type is compared in the equals
     * method.
     *
     * @param clazz
     *          The type to find the {@link CtField fields} in which are compared in the
     *          types {@link Object#equals(Object)} method, not {@code null}.
     *
     * @param accessibleFieldsOfType
     *          A set containing the <i>accessible</i> fields which are declared in the {@code clazz} itself
     *          and all superclasses of the {@code clazz}, not {@code null}. The fields are <i>accessible</i>
     *          from the given {@code clazz}. Useful when the equals method uses getters or other ways
     *          of accessing fields internally.
     *
     * @return
     *          An <b>unmodifiable</b> set containing all fields of the given {@code clazz} which are compared
     *          in the type's {@link Object#equals(Object)} method. An empty set will be returned when the
     *          given {@code accessibleFieldsOfType} set is empty or the {@link #overridesEquals(CtClass)} method
     *          returns {@code false}.
     */
    public Set<AccessibleField<?>> findFieldsComparedInEqualsMethod(CtClass<?> clazz, Set<AccessibleField<?>> accessibleFieldsOfType) {
        if(accessibleFieldsOfType.isEmpty()) {
            return Set.of();
        }

        if(!overridesEquals(clazz)) {
            log.info("Equals method not overridden by '{}'!", clazz.getQualifiedName());
            return Set.of();
        }

        return findFieldsComparedInEqualsMethodInternal(clazz, accessibleFieldsOfType);
    }

    /**
     * This method can only return {@code true} if the {@link #overridesEquals(CtClass)} method
     * returns {@code true}.
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true} if the {@link Object#equals(Object) equals} method of the super-class
     *          is called inside the given {@code clazz}' equals method. {@code false} is returned
     *          otherwise.
     */
    public boolean callsSuper(CtClass<?> clazz) {
        if(!overridesEquals(clazz)) {
            return false;
        }

        return callsSuperInternal(clazz);
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to find the overridden {@link Object#equals(Object)} method
     *          in, not {@code null}.
     *
     * @return
     *          An optional containing the overridden equals method. An empty optional will be returned
     *          in case the equals method is not overridden in the given {@code clazz}.
     */
    protected Optional<CtMethod<Boolean>> findOverriddenEqualsMethodInClass(CtClass<?> clazz) {
        var typeFactory = new TypeFactory();
        var equalsMethodReturnTypeRef = typeFactory.BOOLEAN_PRIMITIVE;
        var equalsMethodArgTypeRef = typeFactory.OBJECT;

        var equalsMethod = clazz.getMethod(equalsMethodReturnTypeRef, EQUALS_METHOD_SIMPLE_NAME, equalsMethodArgTypeRef);

        if(equalsMethod != null && clazz.equals(equalsMethod.getDeclaringType())) {
            return Optional.of(equalsMethod);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Abstract method to check if a given {@link CtType} overrides the {@link Object#equals(Object) equals}
     * method defined {@link Object}. Does <b>not</b> check if a super-class overrides that method.
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}. Must ba a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, if the given {@code clazz} overrides the {@link Object#equals(Object) equals}
     *          method declared in {@link Object}. {@code false} is returned otherwise.
     */
    public abstract boolean overridesEquals(CtClass<?> clazz);

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}. The {@link #overridesEquals(CtClass)} method
     *          must return {@code true} for the given {@code clazz}.
     *
     * @return
     *          {@code true} if the {@link Object#equals(Object) equals} method of the super-class
     *          is called inside the given {@code clazz}' equals method. {@code false} is returned
     *          otherwise. Implementations might throw an exceptions when the {@link #overridesEquals(CtClass)}
     *          method returns false.
     */
    protected abstract boolean callsSuperInternal(CtClass<?> clazz);

    /**
     *
     * @param clazzOverridingEquals
     *          The {@link CtClass} which overrides the equals method declared in {@link Object#equals(Object)},
     *          not {@code null}. The {@link #overridesEquals(CtClass)} method must return {@code true}.
     *
     * @param accessibleFields
     *          A set containing the <i>accessible</i> fields which are declared in the {@code clazz} itself
     *          and all superclasses of the {@code clazz}, not {@code null}. The fields are <i>accessible</i>
     *          from the given {@code clazz}.
     *
     * @return
     *          A set containing all fields which are compared inside the given {@code clazzOverridingEquals}
     *          equals method.
     */
    protected abstract Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtClass<?> clazzOverridingEquals,
                                                                                        Set<AccessibleField<?>> accessibleFields);

}
