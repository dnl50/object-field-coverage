package de.adesso.objectfieldcoverage.api;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import lombok.Data;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @param <T>
 *          The type of the field.
 *
 * @see AccessibilityAwareFieldFinder
 */
@Data
public class AccessibleField<T> {

    /**
     * The field which can be accessed through a typed element. Not {@code null}.
     */
    private CtField<T> actualField;

    /**
     * A set containing all typed elements which grant access to the {@link #getActualField()
     * actual field}. Neither {@code null} nor empty. Only contains {@link CtMethod} and {@link CtField}
     * instances.
     */
    private Set<CtTypedElement<T>> accessGrantingElements;

    /**
     * A boolean flag indication whether this field is a pseudo field.
     */
    private boolean pseudo;

    public AccessibleField(CtField<T> actualField, Set<CtTypedElement<T>> accessGrantingElements, boolean pseudo) {
        this.actualField = actualField;
        this.accessGrantingElements = accessGrantingElements;
        this.pseudo = pseudo;
    }

    /**
     * The {@link #isPseudo() pseudo} flag is set to {@code false}.
     *
     * @param actualField
     *          The actual {@link CtField} this instance refers to, not {@code null}.
     *
     * @param accessGrantingElement
     *          The {@link CtTypedElement} which grants access to the given
     *          {@code actualField} (e.g. a getter method), not {@code null}.
     */
    public AccessibleField(CtField<T> actualField, CtTypedElement<T> accessGrantingElement) {
        this(actualField, accessGrantingElement, false);
    }

    /**
     *
     * @param actualField
     *          The actual {@link CtField} this instance refers to, not {@code null}.
     *
     * @param accessGrantingElement
     *          The {@link CtTypedElement} which grants access to the given {@code actualField}
     *          (e.g. a getter method), not {@code null}.
     *
     * @param pseudo
     *          The flag specifying whether the accessible field is a pseudo field.
     */
    public AccessibleField(CtField<T> actualField, CtTypedElement<T> accessGrantingElement, boolean pseudo) {
        this(actualField, Set.of(accessGrantingElement), pseudo);
    }

    /**
     * The {@link #isPseudo() pseudo} flag is set to {@code false}.
     *
     * @param actualField
     *          The actual {@link CtField} this instance refers to, not {@code null}.
     *
     * @param accessGrantingElements
     *          The {@link CtTypedElement}s which grant access to the given {@code actualField} (e.g. getter
     *          methods, direct field access), not {@code null}.
     */
    public AccessibleField(CtField<T> actualField, Collection<CtTypedElement<T>> accessGrantingElements) {
        this(actualField, Set.copyOf(accessGrantingElements), false);
    }

    /**
     *
     * @return
     *          {@code true}, if the contained {@link CtField}#s type is a primitive type or
     *          a wrapper class reference type. {@code false} is returned otherwise.
     *
     * @see PrimitiveTypeUtils#isPrimitiveTypeField(CtField)
     */
    public boolean isPrimitiveTypeField() {
        return PrimitiveTypeUtils.isPrimitiveTypeField(actualField);
    }

    /**
     *
     * @return
     *          {@code true}, if the contained {@link CtField}#s type is a reference type and not one of the
     *          primitive types wrapper classes. {@code false} is returned otherwise.
     *
     * @see PrimitiveTypeUtils#isPrimitiveTypeField(CtField)
     */
    public boolean isReferenceTypeField() {
        return !PrimitiveTypeUtils.isPrimitiveTypeField(actualField);
    }

    /**
     *
     * @param other
     *          The other accessible field instance {@code this} instance should be united with,
     *          not {@code null}.
     *
     * @return
     *          A new {@link AccessibleField} instance with their {@link #getAccessGrantingElements() access
     *          granting elements} set combined into a single set.
     *
     * @throws IllegalArgumentException
     *          When the {@link #getActualField() actual field} of the given {@code other} instance
     *          is {@code null} or is not equal to the actual field of {@code this} instance or the
     *          {@link #isPseudo() pseudo} flags are not equal.
     */
    public AccessibleField<T> unite(AccessibleField<T> other) {
        var otherActualField = other.actualField;
        if(!this.actualField.equals(otherActualField)) {
            throw new IllegalArgumentException("The actual fields are not equal!");
        } else if(this.pseudo != other.pseudo) {
            throw new IllegalArgumentException("The pseudo flag of the other AccessibleField instance is not the same!");
        }

        var unitedSetInitialCapacity = this.accessGrantingElements.size() + other.accessGrantingElements.size();
        var unitedSet = new HashSet<CtTypedElement<T>>(unitedSetInitialCapacity);

        unitedSet.addAll(this.accessGrantingElements);
        unitedSet.addAll(other.accessGrantingElements);

        return new AccessibleField<>(this.actualField, unitedSet, this.pseudo);
    }

    /**
     *
     * @return
     *          {@code true}, when the {@link #getAccessGrantingElements() access granting elements} set
     *          contains the {@link #getActualField() actual field} itself. {@code false} is returned
     *          otherwise.
     */
    public boolean isDirectlyAccessible() {
        return accessGrantingElements.contains(actualField);
    }

    /**
     *
     * @return
     *          {@code true}, when the {@link #getAccessGrantingElements() access granting elements} set
     *          contains a {@link spoon.reflect.declaration.CtMethod} instance. {@code false} is returned
     *          otherwise.
     */
    public boolean isAccessibleThroughMethod() {
        return accessGrantingElements.stream()
                .anyMatch(element -> element instanceof CtMethod);
    }

    /**
     *
     * @param typedElement
     *          The element to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typedElement} is contained in the
     *          {@link AccessibleField#getAccessGrantingElements() access granting elements}.
     */
    public boolean isAccessibleThroughElement(CtTypedElement<T> typedElement) {
        return accessGrantingElements.contains(typedElement);
    }

    /**
     *
     * @param invocation
     *          The {@link CtInvocation} which should be checked.
     *
     * @return
     *          {@code true}, if the given {@code invocation}'s return type matches
     *          the {@link #getActualField() actual field}'s type and the underlying
     *          {@link spoon.reflect.declaration.CtExecutable} is contained in the {@link #getAccessGrantingElements()
     *          access granting elements} set. {@code false} is returned otherwise.
     */
    public boolean isAccessedThroughInvocation(CtInvocation<T> invocation) {
        if(invocation == null || !actualField.getType().equals(invocation.getType())) {
            return false;
        }

        return accessGrantingElements.contains(invocation.getExecutable().getDeclaration());
    }

    /**
     * Static utility method which unites {@link AccessibleField} instances which have the same
     * {@link AccessibleField#getActualField() actual field} into a single {@link AccessibleField}
     * instance. Uses the {@link AccessibleField#unite(AccessibleField)} method internally.
     *
     * @param accessibleFields
     *          A collection containing multiple {@link AccessibleField} instances for different
     *          {@link CtField}s, not {@code null}.
     *
     * @return
     *          A set containing a single {@link AccessibleField} instance for each {@link CtField}.
     *
     * @throws IllegalArgumentException
     *          When two {@link AccessibleField} instances with the same internal {@link CtField} are present,
     *          but their {@link #isPseudo() pseudo} flag status differs.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Set<AccessibleField<?>> uniteAll(Collection<? extends AccessibleField<?>> accessibleFields) {
        var groupedByFieldMap = accessibleFields.stream()
                .collect(Collectors.groupingBy(AccessibleField::getActualField));

        var resultSet = new HashSet<AccessibleField<?>>();

        groupedByFieldMap.forEach((actualField, accessibleFieldsForCtField) -> {
            var isPseudoAndNonPseudo = accessibleFieldsForCtField.stream()
                    .map(AccessibleField::isPseudo)
                    .collect(Collectors.toSet())
                    .size() == 2;

            if(isPseudoAndNonPseudo) {
                throw new IllegalArgumentException(String.format("The pseudo field flag for field '%s' is not consistent!",
                        actualField.getSimpleName()));
            }

            AccessibleField unitedAccessibleField = null;

            for(var accessibleField : accessibleFieldsForCtField) {
                if(unitedAccessibleField == null) {
                    unitedAccessibleField = accessibleField;
                } else {
                    unitedAccessibleField = unitedAccessibleField.unite(accessibleField);
                }
            }

            resultSet.add(unitedAccessibleField);
        });

        return resultSet;
    }

}
