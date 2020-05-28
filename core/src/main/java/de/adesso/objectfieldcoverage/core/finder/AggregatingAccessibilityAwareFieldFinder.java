package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link AccessibilityAwareFieldFinder} implementation using multiple other {@link AccessibilityAwareFieldFinder}
 * implementations internally, aggregating their results.
 */
public class AggregatingAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     * The {@link AccessibilityAwareFieldFinder}s used internally.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     *
     * @param fieldFinders
     *          The {@link AccessibilityAwareFieldFinder}s which should be used internally, not {@code null}.
     */
    public AggregatingAccessibilityAwareFieldFinder(Collection<? extends AccessibilityAwareFieldFinder> fieldFinders) {
        this.fieldFinders = List.copyOf(fieldFinders);
    }

    /**
     *
     * @param accessingType
     *          The class whose methods could potentially access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if at least one of the registered {@link AccessibilityAwareFieldFinder}'s
     *          {@link AccessibilityAwareFieldFinder#isFieldAccessible(CtType, CtField)} method returns {@code true}
     *          for the given {@code accessingType} and {@code field}. {@code false} is returned otherwise.
     */
    @Override
    public boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field) {
        return fieldFinders.stream()
                .anyMatch(finder -> finder.isFieldAccessible(accessingType, field));
    }

    /**
     *
     * @param accessingType
     *          The class whose methods can access the given {@code field}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code accessingType}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param <T>
     *          The type of the given {@code field}.
     *
     * @return
     *          A set containing the flat mapped result of each {@link AccessibilityAwareFieldFinder#findAccessGrantingElements(CtType, CtField)}
     *          invocation of the registered {@link AccessibilityAwareFieldFinder}s.
     */
    @Override
    public <T> Set<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field) {
        return fieldFinders.stream()
                .filter(fieldFinder -> fieldFinder.isFieldAccessible(accessingType, field))
                .map(fieldFinder -> fieldFinder.findAccessGrantingElements(accessingType, field))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}
