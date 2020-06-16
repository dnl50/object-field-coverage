package de.adesso.objectfieldcoverage.core.processor.evaluation.graph.pseudo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.javadoc.internal.Pair;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Set;

//TODO: JavaDoc
@Slf4j
@RequiredArgsConstructor
public abstract class PseudoFieldProcessor {

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
     * @return
     */
    public Set<CtField<?>> findOrCreatePseudoFields(CtTypeReference<?> typeRef) {
        if(!containsPseudoFields(typeRef)) {
            throw new IllegalArgumentException(String.format("The given type '%s' does not contain any pseudo fields!",
                    typeRef.getQualifiedName()));
        }

        var pseudoClass = findOrCreatePseudoClass(typeRef);
        fieldNamesAndTypes()
    }

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
     *          A set containing the (simple name, type reference) pairs for the fields
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

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} to get the pseudo class for, not {@code null}.
     *
     * @return
     *          An existing class with the same qualified name in case it exists or a newly
     *          {@link PseudoClassGenerator#generatePseudoClass(Factory, String, String) generated} {@link CtClass}
     *          when no such class is present.
     */
    protected CtClass<?> findOrCreatePseudoClass(CtTypeReference<?> typeRef) {
        var pseudoClassPrefix = getPseudoClassPrefix(typeRef);
        var packageQualifiedName = getPackageQualifiedName(typeRef);
        var pseudoClassQualifiedName = String.format("%s.%s%s", packageQualifiedName, pseudoClassPrefix,
                PseudoClassGenerator.PSEUDO_CLASS_SUFFIX);
        var classesWithQualifiedName = typeRef.getFactory().getModel().
                getElements(new QualifiedNameClassTypeFilter(pseudoClassQualifiedName));

        if(classesWithQualifiedName.isEmpty()) {
            return pseudoClassGenerator.generatePseudoClass(typeRef.getFactory(), pseudoClassPrefix, packageQualifiedName);
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
    protected <T> CtField<T> createField(CtClass<?> pseudoClass, String fieldName, CtTypeReference<T> fieldTypeRef) {
        var pseudoFieldOptional = pseudoClass.getAllFields().stream()
                .filter(field -> fieldName.equals(field.getSimpleName()))
                .findFirst();

        if(pseudoFieldOptional.isPresent()) {
            var existingPseudoField = pseudoFieldOptional.get();

            if(!fieldTypeRef.equals(existingPseudoField.getType())) {
                var exceptionMessage = String.format("Pseudo field '%s' exists but has different type " +
                        "(expected: '%s', actual: '%s')!", fieldName, existingPseudoField.getType().getQualifiedName(),
                        fieldTypeRef.getQualifiedName());

                log.error(exceptionMessage);

                throw new IllegalStateException(exceptionMessage);
            }

            log.debug("Pseudo field '{}' already exists!", fieldName);

            return (CtField<T>) pseudoFieldOptional.get();
        } else {
            log.debug("Pseudo field '{}' does not exist and needs to be generated!", fieldName);
            return pseudoFieldGenerator.generatePseudoField(pseudoClass, fieldTypeRef, fieldName);
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
