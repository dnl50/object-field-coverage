package de.adesso.objectfieldcoverage.core.finder.pseudo.generator;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.FieldFactory;
import spoon.reflect.reference.CtTypeReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PseudoFieldGeneratorImplIntegrationTest extends AbstractSpoonIntegrationTest {

    private PseudoFieldGeneratorImpl testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new PseudoFieldGeneratorImpl();
    }

    @Test
    void generatePseudoFieldGeneratesExpectedField() {
        // given
        var model = buildModel("graph/pseudo/generator/EmptyClass.java");
        var pseudoClass = findClassWithSimpleName(model,"EmptyClass");
        var factory = pseudoClass.getFactory().Field();
        var fieldTypeRef = pseudoClass.getFactory().Type().STRING;
        var givenFieldName = "test";

        // when
        var generatedField = testSubject.generatePseudoField(factory, pseudoClass, fieldTypeRef, givenFieldName);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(generatedField.getDeclaringType()).isEqualTo(pseudoClass);
        softly.assertThat(generatedField.getSimpleName()).isEqualTo(givenFieldName);
        softly.assertThat(generatedField.getType()).isEqualTo(fieldTypeRef);

        softly.assertThat(pseudoClass.getFields()).containsOnly(generatedField);

        softly.assertAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generatePseudoFieldThrowsExceptionWhenSimpleNameIsBlank() {
        // given
        var factoryMock = mock(FieldFactory.class);
        var pseudoClassMock = mock(CtClass.class);
        var typeRefMock = mock(CtTypeReference.class);

        var givenSimpleName = "";

        // when / then
        assertThatThrownBy(() -> testSubject.generatePseudoField(factoryMock, pseudoClassMock, typeRefMock, givenSimpleName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The field name cannot be blank!");
    }

}
