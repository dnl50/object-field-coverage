package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link EqualsMethodAnalyzer} implementation regarding all pseudo fields, which are part of a given type,
 * as compared in the equals method of that type.
 */
@Slf4j
@NoArgsConstructor
public class PseudoFieldEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    /**
     *
     * @param clazzRef
     *          The type reference to check, not {@code null}. Must be a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, since a type reference containing pseudo fields compares them in its equals
     *          method by definition.
     */
    @Override
    public boolean overridesEquals(CtTypeReference<?> clazzRef) {
        return true;
    }

    /**
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to check, not {@code null}. The {@link #overridesEquals(CtTypeReference)}
     *          method must return {@code true} for the given {@code clazz}.
     *
     * @return
     *          {@code false}, since a type reference which containing pseudo fields does not explicitly extend
     *          any class.
     */
    @Override
    protected boolean callsSuperInternal(CtTypeReference<?> clazzRef) {
        return false;
    }

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
     *          A set containing all {@link AccessibleField}s whose {@link AccessibleField#isPseudo() pseudo} flag
     *          is set.
     */
    @Override
    protected Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtTypeReference<?> clazzRefOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        var accessiblePseudoFields = accessibleFields.stream()
                .filter(AccessibleField::isPseudo)
                .collect(Collectors.toSet());

        if(accessiblePseudoFields.size() > 0 && accessibleFields.size() != accessiblePseudoFields.size()) {
            log.warn("");
        }

        return accessiblePseudoFields;
    }

}
