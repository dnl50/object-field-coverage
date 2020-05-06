package de.adesso.objectfieldcoverage.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;

import java.util.HashSet;
import java.util.Set;

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

}
