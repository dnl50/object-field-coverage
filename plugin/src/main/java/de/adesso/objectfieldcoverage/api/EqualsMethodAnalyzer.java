package de.adesso.objectfieldcoverage.api;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Optional;
import java.util.Set;

/**
 * Abstract base class for equals analysers to find out which fields in a given class are compared in the equals
 * method. Provides methods for analyzing an actual methods, but other analysis may also be performed.
 * <b/>
 * All implementations must provide a public no-arg default constructor.
 */
@Slf4j
@NoArgsConstructor
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
     * @param clazzRef
     *          The type reference to find the {@link AccessibleField fields} in which are compared in the
     *          types {@link Object#equals(Object)} method, not {@code null}. Must be a class or enum
     *          type reference.
     *
     * @param accessibleFieldsOfType
     *          A set containing the <i>accessible</i> fields which are declared in the {@code clazz} itself
     *          and all superclasses of the {@code clazz}, not {@code null}. May include pseudo fields as well.
     *          The fields are <i>accessible</i> from the given {@code clazz}. Useful when the equals method uses
     *          getters or other ways of accessing fields internally.
     *
     * @return
     *          An <b>unmodifiable</b> set containing all fields of the given {@code clazzRef} which are compared
     *          in the type's {@link Object#equals(Object)} method. An empty set will be returned when the
     *          given {@code accessibleFieldsOfType} set is empty or the {@link #overridesEquals(CtTypeReference)} method
     *          returns {@code false}.
     *
     * @throws IllegalArgumentException
     *          When the given {@code clazzRef} is not a class or enum type reference.
     */
    public Set<AccessibleField<?>> findFieldsComparedInEqualsMethod(CtTypeReference<?> clazzRef, Set<AccessibleField<?>> accessibleFieldsOfType) {
        if(!clazzRef.isClass() && !clazzRef.isEnum()) {
            throw new IllegalArgumentException("The given type reference must be a class or enum reference!");
        }

        if(accessibleFieldsOfType.isEmpty()) {
            return Set.of();
        }

        if(!overridesEquals(clazzRef)) {
            log.info("Equals method not overridden by '{}'!", clazzRef.getQualifiedName());
            return Set.of();
        }

        return findFieldsComparedInEqualsMethodInternal(clazzRef, accessibleFieldsOfType);
    }

    /**
     * This method can only return {@code true} if the {@link #overridesEquals(CtTypeReference)} method
     * returns {@code true}.
     *
     * @param clazzRef
     *          The reference of the type to check, not {@code null}.
     *
     * @return
     *          {@code true} if the {@link Object#equals(Object) equals} method of the super-class
     *          is called inside the given {@code clazz}' equals method. {@code false} is returned
     *          otherwise.
     */
    public boolean callsSuper(CtTypeReference<?> clazzRef) {
        if(!overridesEquals(clazzRef)) {
            return false;
        }

        return callsSuperInternal(clazzRef);
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
     * @param clazzRef
     *          The type reference to check, not {@code null}. Must be a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, if the given {@code clazzRef} overrides the {@link Object#equals(Object) equals}
     *          method declared in {@link Object}. {@code false} is returned otherwise.
     */
    public abstract boolean overridesEquals(CtTypeReference<?> clazzRef);

    /**
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to check, not {@code null}. The {@link #overridesEquals(CtTypeReference)}
     *          method must return {@code true} for the given {@code clazz}.
     *
     * @return
     *          {@code true} if the {@link Object#equals(Object) equals} method of the super-class
     *          is called inside the given {@code clazzRef}'s equals method. {@code false} is returned
     *          otherwise. Implementations might throw an exceptions when the {@link #overridesEquals(CtTypeReference)}
     *          method returns false.
     */
    protected abstract boolean callsSuperInternal(CtTypeReference<?> clazzRef);

    /**
     *
     * @param clazzRefOverridingEquals
     *          The reference of the type which overrides the equals method declared in {@link Object#equals(Object)},
     *          not {@code null}. The {@link #overridesEquals(CtTypeReference)} method must return {@code true} for the
     *          this type reference.
     *
     * @param accessibleFields
     *          A set containing the <i>accessible</i> fields which are declared in the {@code clazzRefOverridingEquals} itself
     *          and all superclasses of the {@code clazz}, not {@code null}. The fields are <i>accessible</i>
     *          from the given {@code clazz}.
     *
     * @return
     *          A set containing all fields which are compared inside the given {@code clazzOverridingEquals}'
     *          equals method. Must be a subset of the given {@code accessibleFields} set.
     */
    protected abstract Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtTypeReference<?> clazzRefOverridingEquals,
                                                                                        Set<AccessibleField<?>> accessibleFields);

}
