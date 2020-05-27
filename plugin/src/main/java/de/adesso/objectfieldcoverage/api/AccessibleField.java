package de.adesso.objectfieldcoverage.api;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AccessibleField<T> {

    /**
     * The field which can be accessed through a typed element. Not {@code null}.
     */
    private CtField<T> actualField;

    /**
     * A set containing all typed elements which grant access to the {@link #getActualField()
     * actual field}. Neither {@code null} nor empty.
     */
    private Set<CtTypedElement<T>> accessGrantingElements;

    /**
     *
     * @param actualField The actual {@link CtField} this instance refers to, not {@code null}.
     *
     * @param accessGrantingElement The {@link CtTypedElement} which grants access to the given
     *                              {@code actualField} (e.g. a getter method), not {@code null}.
     */
    public AccessibleField(CtField<T> actualField, CtTypedElement<T> accessGrantingElement) {
        this(actualField, Set.of(accessGrantingElement));
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
     *          is {@code null} or is not equal to the actual field of {@code this} instance.
     */
    public AccessibleField<T> unite(AccessibleField<T> other) {
        var otherActualField = other.actualField;
        if(!this.actualField.equals(otherActualField)) {
            throw new IllegalArgumentException("The actual fields are not equal!");
        }

        var unitedSetInitialCapacity = this.accessGrantingElements.size() + other.accessGrantingElements.size();
        var unitedSet = new HashSet<CtTypedElement<T>>(unitedSetInitialCapacity);

        unitedSet.addAll(this.accessGrantingElements);
        unitedSet.addAll(other.accessGrantingElements);

        return new AccessibleField<>(this.actualField, unitedSet);
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
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Set<AccessibleField<?>> uniteAll(Collection<? extends AccessibleField<?>> accessibleFields) {
        var groupedByFieldMap = accessibleFields.stream()
                .collect(Collectors.groupingBy(AccessibleField::getActualField));

        var resultSet = new HashSet<AccessibleField<?>>();

        groupedByFieldMap.values().forEach(accessibleFieldsForCtField -> {
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
