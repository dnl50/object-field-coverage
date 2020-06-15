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

    public Set<CtField<?>> createPseudoFields(CtTypeReference<?> typeRef) {
        if(!containsPseudoFields(typeRef)) {
            throw new IllegalArgumentException(String.format("The given type '%s' does not contain any pseudo fields!",
                    typeRef.getQualifiedName()));
        }


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
    protected abstract Set<Pair<String, CtTypeReference<?>>> fieldNameAndTypes(CtTypeReference<?> typeRef);

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
     *          The {@link CtTypeReference} to get the pseudo class for, not {@code null}.
     *
     * @param packageQualifiedName
     *          The qualified name of the package the pseudo class should be declared in, not null. An empty
     *          String indicates the default package.
     *
     * @return
     *          An existing class with the same qualified name in case it exists or a newly
     *          {@link PseudoClassGenerator#generatePseudoClass(Factory, String, String) generated} {@link CtClass}
     *          when no such class is present.
     */
    protected CtClass<?> findOrCreatePseudoClass(CtTypeReference<?> typeRef, String packageQualifiedName) {
        var pseudoClassPrefix = getPseudoClassPrefix(typeRef);
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

    protected <T> CtField<T> createField(CtClass<?> pseudoClass, String simpleName, CtTypeReference<T> fieldTypeRef) {
        var pseudoFieldOptional = pseudoClass.getAllFields().stream()
                .filter(field -> simpleName.equals(field.getSimpleName()))
                .findFirst();

        if(pseudoFieldOptional.isPresent()) {
            var existingPseudoField = pseudoFieldOptional.get();

            if(!fieldTypeRef.equals(existingPseudoField.getType())) {
                log.warn("");
            }

            log.info("Pseudo field");
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

        public QualifiedNameClassTypeFilter(String qualifiedName) {
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
