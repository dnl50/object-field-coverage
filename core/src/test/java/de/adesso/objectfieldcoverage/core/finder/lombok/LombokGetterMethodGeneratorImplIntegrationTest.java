package de.adesso.objectfieldcoverage.core.finder.lombok;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import lombok.AccessLevel;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LombokGetterMethodGeneratorImplIntegrationTest extends AbstractSpoonIntegrationTest {

    private LombokGetterMethodGeneratorImpl testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokGetterMethodGeneratorImpl();
    }

    @Test
    void generateGetterMethodReturnsExistingMethodWhenPresent() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var expectedExistingMethod = dataBoxClass.getMethod("getWidth");
        var field = dataBoxClass.getField("width");

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.PUBLIC);

        // then
        assertThat(actualMethod).isEqualTo(expectedExistingMethod);
    }

    @Test
    void generateGetterMethodGeneratesNewMethodWithMatchingAccessLevelWhenNotPresent() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var field = dataBoxClass.getField("height");

        var expectedName = "getHeight";

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.PROTECTED);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualMethod.getSimpleName()).isEqualTo(expectedName);
        softly.assertThat(actualMethod.isProtected()).isTrue();
        softly.assertThat(actualMethod.getParameters()).isEmpty();

        softly.assertAll();
    }

    @Test
    void generateGetterMethodGeneratesNewMethodWithMatchingAccessLevelWhenNotPresent_BooleanPrimitive() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var field = dataBoxClass.getField("empty");

        var expectedName = "isEmpty";

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.PUBLIC);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualMethod.getSimpleName()).isEqualTo(expectedName);
        softly.assertThat(actualMethod.isPublic()).isTrue();
        softly.assertThat(actualMethod.getParameters()).isEmpty();

        softly.assertAll();
    }

    @Test
    void generateGetterMethodGeneratesNewMethodWithMatchingAccessLevelWhenNotPresent_BooleanWrapper() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var field = dataBoxClass.getField("full");

        var expectedName = "isFull";

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.PACKAGE);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualMethod.getSimpleName()).isEqualTo(expectedName);
        softly.assertThat(actualMethod.isPublic()).isFalse();
        softly.assertThat(actualMethod.isProtected()).isFalse();
        softly.assertThat(actualMethod.isPrivate()).isFalse();
        softly.assertThat(actualMethod.getParameters()).isEmpty();

        softly.assertAll();
    }

    @Test
    void generateGetterMethodGeneratesNewMethodWithMatchingAccessLevelWhenNotPresent_Private() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var field = dataBoxClass.getField("full");

        var expectedName = "isFull";

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.PRIVATE);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualMethod.getSimpleName()).isEqualTo(expectedName);
        softly.assertThat(actualMethod.isPrivate()).isTrue();
        softly.assertThat(actualMethod.getParameters()).isEmpty();

        softly.assertAll();
    }

    @Test
    void generateGetterMethodGeneratesNewMethodWithMatchingAccessLevelWhenNotPresent_Module() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var dataBoxClass = findClassWithSimpleName(model, "DataBox");
        var field = dataBoxClass.getField("full");

        var expectedName = "isFull";

        // when
        var actualMethod = testSubject.generateGetterMethod(field, AccessLevel.MODULE);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualMethod.getSimpleName()).isEqualTo(expectedName);
        softly.assertThat(actualMethod.isPublic()).isFalse();
        softly.assertThat(actualMethod.isProtected()).isFalse();
        softly.assertThat(actualMethod.isPrivate()).isFalse();
        softly.assertThat(actualMethod.getParameters()).isEmpty();

        softly.assertAll();
    }

}
