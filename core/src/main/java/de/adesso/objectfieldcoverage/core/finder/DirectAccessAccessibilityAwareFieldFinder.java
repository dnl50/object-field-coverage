package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;

import java.util.Collection;
import java.util.Set;

/**
 * {@link AccessibilityAwareFieldFinder} implementation searching for fields which are accessible
 * directly through field access.
 * <p/>
 * <b>Note:</b> Java modules added in Java 9 are not taken into account.
 */
@Slf4j
public class DirectAccessAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     * Checks if the field is accessible through direct field access according to the Java
     * Language Specification §6.6.
     *
     * @param accessingType
     *          The class whose methods could potentially access the given {@code type}'s
     *          fields, not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if {@link #isAccessibleAccordingToJls(CtType, CtTypeMember)} returns {@code true}.
     *          {@code false} is returned otherwise.
     */
    @Override
    public boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field) {
        return super.isAccessibleAccordingToJls(accessingType, field);
    }

    /**
     *
     * @param accessingType
     *          The class whose methods can access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code accessingType},
     *          not {@code null}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          A set containing the given {@code field} as its only element.
     */
    @Override
    public <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field) {
        return Set.of(field);
    }

    /**
     *
     * @param accessingTypeTypeRefPair
     *          A pair containing the accessing type and the field declaring type, not {@code null}.
     *
     * @return
     *          {@code true}, since a type can make a field accessible through several options.
     */
    @Override
    public boolean callNext(Pair<CtType<?>, CtTypeReference<?>> accessingTypeTypeRefPair) {
        return true;
    }

}
