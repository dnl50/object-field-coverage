package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.NoArgsConstructor;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * {@link EqualsMethodAnalyzer} implementation for
 */
@NoArgsConstructor
public class JavaPackageEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    /**
     * A regex matching the {@code java} package and any sub package. It is assumed that the package name which
     * this regex matches is a valid package name.
     */
    private static final String JAVA_PACKAGE_REGEX = "^java(\\..*)?$";

    /**
     *
     * @param clazzRef
     *          The type reference to check, not {@code null}. Must be a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, if the given {@code clazzRef} is declared in the {@code java} package
     *          or any sub package. {@code false} is returned otherwise.
     */
    @Override
    public boolean overridesEquals(CtTypeReference<?> clazzRef) {
        return clazzRef.getPackage().getQualifiedName().matches(JAVA_PACKAGE_REGEX);
    }

    /**
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to check, not {@code null}. The {@link #overridesEquals(CtTypeReference)}
     *          method must return {@code true} for the given {@code clazz}.
     *
     * @return
     *          {@code true}
     */
    @Override
    protected boolean callsSuperInternal(CtTypeReference<?> clazzRef) {
        return true;
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
     *          The given {@code accessibleFields}.
     */
    @Override
    protected Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtTypeReference<?> clazzRefOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        return accessibleFields;
    }

}
