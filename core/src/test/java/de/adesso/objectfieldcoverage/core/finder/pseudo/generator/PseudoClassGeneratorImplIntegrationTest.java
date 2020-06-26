package de.adesso.objectfieldcoverage.core.finder.pseudo.generator;

import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.factory.ClassFactory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PseudoClassGeneratorImplIntegrationTest extends AbstractSpoonIntegrationTest {

    private PseudoClassGeneratorImpl testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new PseudoClassGeneratorImpl();
    }

    @Test
    void generatePseudoClassUsesDefaultPackageWhenPackageQualifiedNameIsBlank() {
        // given
        var factory = buildModel().getUnnamedModule().getFactory().Class();
        var givenSimpleName = "Test";
        var givenPackageName = "";

        var expectedFullyQualifiedName = "Test" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

        // when
        var actualGeneratedClass = testSubject.generatePseudoClass(factory, givenSimpleName, givenPackageName);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualGeneratedClass.getQualifiedName()).isEqualTo(expectedFullyQualifiedName);
        softly.assertThat(actualGeneratedClass.isPublic()).isTrue();

        softly.assertAll();
    }

    @Test
    void generatePseudoClassUsesGivenPackageWhenPackageQualifiedNameIsNotBlank() {
        // given
        var factory = buildModel().getUnnamedModule().getFactory().Class();
        var givenSimpleName = "Test";
        var givenPackageName = "de.test";

        var expectedFullyQualifiedName = "de.test.Test" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

        // when
        var actualGeneratedClass = testSubject.generatePseudoClass(factory, givenSimpleName, givenPackageName);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualGeneratedClass.getQualifiedName()).isEqualTo(expectedFullyQualifiedName);
        softly.assertThat(actualGeneratedClass.isPublic()).isTrue();

        softly.assertAll();
    }

    @Test
    void generatePseudoClassThrowsExceptionWhenSimpleNameIsBlank() {
        // given
        var factoryMock = mock(ClassFactory.class);
        var givenSimpleName = "";

        // when / then
        assertThatThrownBy(() -> testSubject.generatePseudoClass(factoryMock, givenSimpleName, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The simple class name cannot be blank!");
    }

}
