package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGeneratorImpl;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGeneratorImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.TypeFactory;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveTypePseudoFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private PrimitiveTypePseudoFieldFinder testSubject;

    private CtModel model;

    private TypeFactory typeFactory;

    @BeforeEach
    void setUp() {
        this.model = buildModel();
        this.typeFactory = model.getUnnamedModule()
                .getFactory()
                .Type();

        this.testSubject = new PrimitiveTypePseudoFieldFinder(new PseudoClassGeneratorImpl(),
                new PseudoFieldGeneratorImpl());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    })
    void containsPseudoFieldsReturnsTrueForPrimitiveTypesAndWrapperTypes(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            String.class, BigDecimal.class
    })
    void containsPseudoFieldsReturnsFalseForNonPrimitiveTypesAndNonWrapperTypes(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isFalse();
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Boolean.class, boolean.class,
            Byte.class, byte.class,
            Character.class, char.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class
    })
    @SuppressWarnings("unchecked")
    void findOrCreatePseudoFieldsCreatesExpectedFieldsForPrimitiveAndWrapperTypes(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);
        var givenAccessingType = givenTypeRef.getTypeDeclaration();

        var expectedDeclaringTypeSimpleName = String.format("%s%s",
                StringUtils.capitalize(PrimitiveTypeUtils.getPrimitiveTypeReference(givenTypeRef).getSimpleName()),
                PseudoClassGenerator.PSEUDO_CLASS_SUFFIX);

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(givenAccessingType, givenTypeRef);

        // then
        var pseudoClass = findClassWithSimpleName(model, expectedDeclaringTypeSimpleName);
        var valueField = (CtField<Boolean>) pseudoClass.getField("value");

        var expectedAccessibleField = new AccessibleField<>(valueField, Set.of(), true);

        assertThat(actualAccessibleFields).containsExactly(expectedAccessibleField);
    }

}
