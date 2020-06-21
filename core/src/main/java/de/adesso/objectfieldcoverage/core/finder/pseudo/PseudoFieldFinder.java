package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.ClassFactory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract {@link AccessibilityAwareFieldFinder} implementation for finding pseudo fields in a given
 * type reference.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class PseudoFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     * Interface abstraction for generating pseudo {@link CtClass}es.
     */
    private final PseudoClassGenerator pseudoClassGenerator;

    /**
     * Interface abstraction for generating pseudo {@link CtField}s.
     */
    private final PseudoFieldGenerator pseudoFieldGenerator;

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} which should be checked for pseudo fields, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} contains pseudo fields.
     */
    public abstract boolean containsPseudoFields(CtTypeReference<?> typeRef);

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} to get the simple names and {@link CtTypeReference}s of the pseudo fields for,
     *          not {@code null}.
     *
     * @return
     *          A set containing the (simple name, type reference) pairs of the pseudo fields which are
     *          generated. The type reference <b>must</b> be a type reference of a primitive type!
     */
    protected abstract Set<Pair<String, CtTypeReference<?>>> fieldNamesAndTypes(CtTypeReference<?> typeRef);

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type to get the pseudo class prefix for, not {@code null}.
     *
     * @return
     *          The prefix of the simple name of the pseudo class.
     */
    protected abstract String getPseudoClassPrefix(CtTypeReference<?> typeRef);

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type the pseudo fields should be generated for, not {@code null}.
     *
     * @return
     *          The qualified name of the package the pseudo class is part of.
     */
    protected abstract String getPackageQualifiedName(CtTypeReference<?> typeRef);

    @Override
    protected Set<CtField<?>> findFieldsInType(CtTypeReference<?> typeRef) {
        if(containsPseudoFields(typeRef)) {
            return findOrCreatePseudoFields(typeRef);
        }

        return Set.of();
    }

    /**
     *
     * @param accessingType
     *          The type whose methods could potentially access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the field is accessible according to the normal Java access rules. {@code false}
     *          is returned otherwise.
     */
    @Override
    public boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field) {
        return isAccessibleAccordingToJls(accessingType, field);
    }

    /**
     *
     * @param field
     *          The {@link CtField field} to check, not {@code null}.
     *
     * @return
     *          {@code true}, since implementations of this abstract class only return pseudo fields.
     */
    @Override
    protected boolean isPseudoField(CtField<?> field) {
        return true;
    }

    /**
     *
     * @param accessingType
     *          The type whose methods can access the given {@code field}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code accessingType}, not {@code null}. The
     *          {@link #isFieldAccessible(CtType, CtField)} method <b>must</b> return {@code true} when being invoked
     *          with the given {@code accessingType} and {@code field}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          An empty set since pseudo fields aren't actually present on the accessed type.
     */
    @Override
    public <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field) {
        return Set.of();
    }

    /**
     *
     * @param accessingTypeTypeRefPair
     *          A pair containing the accessing type and the field declaring type, not {@code null}.
     *
     * @return
     *          {@code true}, if the given type reference does not contain any pseudo fields. {@code false}
     *          is returned otherwise.
     */
    @Override
    public boolean callNext(Pair<CtType<?>, CtTypeReference<?>> accessingTypeTypeRefPair) {
        return !containsPseudoFields(accessingTypeTypeRefPair.getRight());
    }

    /**
     *
     * @param typeRef
     *          The type reference of the type to create pseudo fields for, not {@code null}.
     *
     * @return
     *          A set containing the generated {@code public} pseudo fields. The fields are declared in a separate
     *          {@code public} pseudo class.
     *
     * @throws IllegalStateException
     *          When the {@link #containsPseudoFields(CtTypeReference)} method returns {@code false} for the given
     *          {@code typeRef}.
     */
    private Set<CtField<?>> findOrCreatePseudoFields(CtTypeReference<?> typeRef) {
        if(!containsPseudoFields(typeRef)) {
            throw new IllegalStateException(String.format("The given type '%s' does not contain any pseudo fields!",
                    typeRef.getQualifiedName()));
        }

        var pseudoClass = findOrCreatePseudoClass(typeRef);
        return fieldNamesAndTypes(typeRef).stream()
                .map(nameAndTypePair -> this.findOrCreateField(pseudoClass, nameAndTypePair.getLeft(), nameAndTypePair.getRight()))
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} to get the pseudo class for, not {@code null}.
     *
     * @return
     *          An existing class with the same qualified name in case it exists or a newly
     *          {@link PseudoClassGenerator#generatePseudoClass(ClassFactory, String, String)}  generated} {@link CtClass}
     *          when no such class is present.
     */
    private CtClass<?> findOrCreatePseudoClass(CtTypeReference<?> typeRef) {
        var pseudoClassPrefix = getPseudoClassPrefix(typeRef);
        var packageQualifiedName = getPackageQualifiedName(typeRef);
        var pseudoClassQualifiedName = String.format("%s.%s%s", packageQualifiedName, pseudoClassPrefix,
                PseudoClassGenerator.PSEUDO_CLASS_SUFFIX);
        var classesWithQualifiedName = typeRef.getFactory().getModel().
                getElements(new QualifiedNameClassTypeFilter(pseudoClassQualifiedName));

        if(classesWithQualifiedName.isEmpty()) {
            return pseudoClassGenerator.generatePseudoClass(typeRef.getFactory().Class(), pseudoClassPrefix, packageQualifiedName);
        } else {
            return classesWithQualifiedName.get(0);
        }
    }

    /**
     *
     * @param pseudoClass
     *          The {@link CtClass} the pseudo field should be generated on, not {@code null}.
     *
     * @param fieldName
     *          The simple name of the field to generate, not blank.
     *
     * @param fieldTypeRef
     *          The type of the field, not {@code null}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          A field with the same {@code fieldName} and type in case it is already present on the given
     *          {@code pseudoClass} or a generated
     *
     * @throws IllegalStateException
     *          When a field with the same {@code fieldName} exists but the type of the field differs from the
     *          given {@code fieldTypeRef}.
     */
    @SuppressWarnings("unchecked")
    private <T> CtField<T> findOrCreateField(CtClass<?> pseudoClass, String fieldName, CtTypeReference<T> fieldTypeRef) {
        var pseudoFieldOptional = pseudoClass.getAllFields().stream()
                .filter(field -> fieldName.equals(field.getSimpleName()))
                .findFirst();

        if(pseudoFieldOptional.isPresent()) {
            var existingPseudoField = (CtFieldReference<T>) pseudoFieldOptional.get();

            if(!fieldTypeRef.equals(existingPseudoField.getType())) {
                var exceptionMessage = String.format("Pseudo field '%s' exists but has different type " +
                        "(expected: '%s', actual: '%s')!", fieldName, existingPseudoField.getType().getQualifiedName(),
                        fieldTypeRef.getQualifiedName());

                log.error(exceptionMessage);

                throw new IllegalStateException(exceptionMessage);
            }

            log.debug("Pseudo field '{}' already exists!", fieldName);

            return existingPseudoField.getDeclaration();
        } else {
            log.debug("Pseudo field '{}' does not exist and needs to be generated!", fieldName);
            var fieldFactory = pseudoClass.getFactory().Field();
            return pseudoFieldGenerator.generatePseudoField(fieldFactory, pseudoClass, fieldTypeRef, fieldName);
        }
    }

    /**
     * {@link TypeFilter} implementation filtering for {@link CtClass}es with a specific qualified name.
     */
    private static class QualifiedNameClassTypeFilter extends TypeFilter<CtClass<?>> {

        /**
         * The qualified name of the {@link CtClass} to filter for.
         */
        private final String qualifiedName;

        QualifiedNameClassTypeFilter(String qualifiedName) {
            super(CtClass.class);

            this.qualifiedName = qualifiedName;
        }

        /**
         *
         * @param clazz
         *          The {@link CtClass} to check, not {@code null}.
         *
         * @return
         *          {@code true}, if {@link CtClass#getQualifiedName() qualified name} of the given {@code clazz}
         *          is equal to the expected qualified name. {@code false} is returned otherwise.
         */
        @Override
        public boolean matches(CtClass<?> clazz) {
            if(!super.matches(clazz)) {
                return false;
            }

            return qualifiedName.equals(clazz.getQualifiedName());
        }

    }

}
