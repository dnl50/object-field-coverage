package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGeneratorImpl;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGeneratorImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * {@link PseudoFieldFinder} implementation for primitive type and primitive wrapper type types.
 */
public class PrimitiveTypePseudoFieldFinder extends PseudoFieldFinder {

    /**
     * The fully qualified name of the {@code java.lang} package. This is where the pseudo classes for
     * primitive/wrapper types are generated.
     */
    private static final String JAVA_LANG_PACKAGE = "java.lang";

    /**
     * The name of the pseudo field of a primitive/wrapper type.
     */
    private static final String PSEUDO_FIELD_NAME = "value";

    public PrimitiveTypePseudoFieldFinder(PseudoClassGenerator pseudoClassGenerator, PseudoFieldGenerator pseudoFieldGenerator) {
        super(pseudoClassGenerator, pseudoFieldGenerator);
    }

    /**
     * No-arg constructor initializing the {@link PseudoClassGenerator} and {@link PseudoFieldGenerator} fields
     * of the super-class with the default implementations.
     *
     * @see PrimitiveTypePseudoFieldFinder#PrimitiveTypePseudoFieldFinder(PseudoClassGenerator, PseudoFieldGenerator)
     */
    @SuppressWarnings("unused")
    public PrimitiveTypePseudoFieldFinder() {
        this(new PseudoClassGeneratorImpl(), new PseudoFieldGeneratorImpl());
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} which should be checked for pseudo fields, not {@code null}.
     *
     * @return
     *          The result of {@link PrimitiveTypeUtils#isPrimitiveOrWrapperType(CtTypeReference)}.
     */
    @Override
    public boolean containsPseudoFields(CtTypeReference<?> typeRef) {
        return PrimitiveTypeUtils.isPrimitiveOrWrapperType(typeRef);
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} to get the simple names and {@link CtTypeReference}s of the pseudo fields for,
     *          not {@code null}.
     *
     * @return
     *          An <b>unmodifiable</b> set containing a single pair. The name of the field is set to
     *          {@value #PSEUDO_FIELD_NAME} and the type reference is that of the corresponding primitive type.
     */
    @Override
    protected Set<Pair<String, CtTypeReference<?>>> fieldNamesAndTypes(CtTypeReference<?> typeRef) {
        var fieldType = PrimitiveTypeUtils.getPrimitiveTypeReference(typeRef);
        return Set.of(Pair.of(PSEUDO_FIELD_NAME, fieldType));
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type to get the pseudo class prefix for, not {@code null}.
     *
     * @return
     *          The simple name of the primitive type which is represented by the given {@code typeRef} with its
     *          first letter capitalized.
     */
    @Override
    protected String getPseudoClassPrefix(CtTypeReference<?> typeRef) {
        var primitiveTypeSimpleName = PrimitiveTypeUtils.getPrimitiveTypeReference(typeRef)
                .getSimpleName();
        return StringUtils.capitalize(primitiveTypeSimpleName);
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type the pseudo fields should be generated for, not {@code null}.
     *
     * @return
     *          {@value #JAVA_LANG_PACKAGE}
     */
    @Override
    protected String getPackageQualifiedName(CtTypeReference<?> typeRef) {
        return JAVA_LANG_PACKAGE;
    }

}
