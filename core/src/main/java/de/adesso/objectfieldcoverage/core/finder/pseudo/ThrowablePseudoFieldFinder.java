package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.Order;
import de.adesso.objectfieldcoverage.api.assertion.reference.ThrowableAssertion;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGeneratorImpl;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGeneratorImpl;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;

/**
 * {@link PseudoFieldFinder} implementation generating pseudo fields for {@link Throwable}s. The {@link Order} is set
 * to a higher value than the one specified in the parent class annotation since {@link Throwable}s are regarded as a
 * separate class.
 */
@Order(1100)
public class ThrowablePseudoFieldFinder extends PseudoFieldFinder {

    /**
     * The {@link TypeFactory} to create type references with.
     */
    private static final TypeFactory TYPE_FACTORY = new TypeFactory();

    /**
     * The type reference of the {@link Throwable} class.
     */
    private static final CtTypeReference<Throwable> THROWABLE_TYPE_REFERENCE = TYPE_FACTORY.createReference(Throwable.class);

    /**
     * The qualified name of the {@code java.lang} package.
     */
    private static final String JAVA_LANG_PACKAGE_QUALIFIED_NAME = "java.lang";

    public ThrowablePseudoFieldFinder(PseudoClassGenerator pseudoClassGenerator, PseudoFieldGenerator pseudoFieldGenerator) {
        super(pseudoClassGenerator, pseudoFieldGenerator);
    }

    /**
     * No-arg constructor initializing the {@link PseudoClassGenerator} and {@link PseudoFieldGenerator} with their
     * corresponding default implementations.
     */
    @SuppressWarnings("unused")
    public ThrowablePseudoFieldFinder() {
        super(new PseudoClassGeneratorImpl(), new PseudoFieldGeneratorImpl());
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@code true}, if the given {@code typeRef} is a type reference of a {@link Throwable}. {@code false}
     *          is returned otherwise.
     */
    @Override
    public boolean containsPseudoFields(CtTypeReference<?> typeRef) {
        return TypeUtils.findExplicitSuperClassesIncludingClass(typeRef)
                .contains(THROWABLE_TYPE_REFERENCE);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          A set containing three pairs.
     */
    @Override
    protected Set<Pair<String, CtTypeReference<?>>> fieldNamesAndTypes(CtTypeReference<?> typeRef) {
        return Set.of(
                Pair.of(ThrowableAssertion.CAUSE_PSEUDO_FIELD_NAME, TYPE_FACTORY.BOOLEAN_PRIMITIVE),
                Pair.of(ThrowableAssertion.MESSAGE_PSEUDO_FIELD_NAME, TYPE_FACTORY.BOOLEAN_PRIMITIVE),
                Pair.of(ThrowableAssertion.TYPE_PSEUDO_FIELD_NAME, TYPE_FACTORY.BOOLEAN_PRIMITIVE)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          The simple name of the given {@code typeRef}.
     */
    @Override
    protected String getPseudoClassPrefix(CtTypeReference<?> typeRef) {
        return typeRef.getSimpleName();
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          {@value JAVA_LANG_PACKAGE_QUALIFIED_NAME}
     */
    @Override
    protected String getPackageQualifiedName(CtTypeReference<?> typeRef) {
        return JAVA_LANG_PACKAGE_QUALIFIED_NAME;
    }

}
