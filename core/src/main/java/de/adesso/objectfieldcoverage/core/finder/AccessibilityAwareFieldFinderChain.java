package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Chain using multiple {@link AccessibilityAwareFieldFinder} implementations internally, aggregating their results.
 * The order of the internal {@link AccessibilityAwareFieldFinder}s is important since the aggregation is done iteratively
 */
@Slf4j
public class AccessibilityAwareFieldFinderChain {

    /**
     * The {@link AccessibilityAwareFieldFinder}s used internally.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     *
     * @param fieldFinders
     *          The {@link AccessibilityAwareFieldFinder}s which should be used internally, not {@code null}.
     */
    public AccessibilityAwareFieldFinderChain(List<? extends AccessibilityAwareFieldFinder> fieldFinders) {
        this.fieldFinders = List.copyOf(fieldFinders);
    }

    /**
     *
     * @param accessingType
     *          The type whose methods could potentially access the given {@code typeRef}'s
     *          fields, not {@code null}.
     *
     * @param typeRef
     *          The reference of the type to get the accessible fields of, not {@code null}.
     *
     * @return
     *          A set of all fields which are accessible from the given {@code accessingType} combined with the
     *          typed element which grants access to the field. Includes fields which are directly declared in
     *          the given {@code typeRef} or in any super-type.
     */
    public Set<AccessibleField<?>> findAccessibleFields(CtType<?> accessingType, CtTypeReference<?> typeRef) {
        Objects.requireNonNull(accessingType, "The accessing type cannot be null!");
        Objects.requireNonNull(typeRef, "The type reference of the type containing fields cannot be null!");

        if(fieldFinders.isEmpty()) {
            return Set.of();
        }

        var accessibleFields = new HashSet<AccessibleField<?>>();

        for (var currentFieldFinder : fieldFinders) {
            accessibleFields.addAll(currentFieldFinder.findAccessibleFields(accessingType, typeRef));

            if (!currentFieldFinder.callNext(Pair.of(accessingType, typeRef))) {
                log.debug("Not calling next AccessibleFieldFinder in chain since '{}#callNext' returned false!",
                        currentFieldFinder.getClass().getName());
                break;
            }
        }

        return AccessibleField.uniteAll(accessibleFields);
    }

}
