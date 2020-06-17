package de.adesso.objectfieldcoverage.core.finder.lombok;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.stream.Collectors;

class LombokAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private LombokAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokAccessibilityAwareFieldFinder();
    }

    @Test
    void dataAnnotatedClass() {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var boxClass = findClassWithSimpleName(model, "DataBox");

        var expectedFields = List.of(
                boxClass.getField("height"),
                boxClass.getField("depth"),
                boxClass.getField("empty"),
                boxClass.getField("full")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass.getReference());

        // then
        var accessibleCtFields = accessibleFields.stream()
                .map(AccessibleField::getActualField)
                .map(CtField.class::cast)
                .collect(Collectors.toSet());

        var softly = new SoftAssertions();

        softly.assertThat(accessibleCtFields).containsExactlyInAnyOrderElementsOf(expectedFields);
        softly.assertThat(boxClass.getMethod("getHeight")).isNotNull();
        softly.assertThat(boxClass.getMethod("getDepth")).isNotNull();
        softly.assertThat(boxClass.getMethod("isEmpty")).isNotNull();
        softly.assertThat(boxClass.getMethod("isFull")).isNotNull();

        softly.assertAll();
    }

    @Test
    void getterAnnotatedClass() {
        // given
        var model = buildModel("finder/lombok/TypeGetterBox.java");
        var boxClass = findClassWithSimpleName(model, "TypeGetterBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height"),
                boxClass.getField("depth")
        );

               // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass.getReference());

        // then
        var accessibleCtFields = accessibleFields.stream()
                .map(AccessibleField::getActualField)
                .map(CtField.class::cast)
                .collect(Collectors.toSet());

        var softly = new SoftAssertions();

        softly.assertThat(accessibleCtFields).containsExactlyInAnyOrderElementsOf(expectedFields);
        softly.assertThat(boxClass.getMethod("getWidth")).isNotNull();
        softly.assertThat(boxClass.getMethod("getHeight")).isNotNull();
        softly.assertThat(boxClass.getMethod("getDepth")).isNotNull();

        softly.assertAll();
    }

    @Test
    void fieldAnnotatedClass() {
        // given
        var model = buildModel("finder/lombok/FieldGetterBox.java");
        var boxClass = findClassWithSimpleName(model, "FieldGetterBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass.getReference());

        // then
        var accessibleCtFields = accessibleFields.stream()
                .map(AccessibleField::getActualField)
                .map(CtField.class::cast)
                .collect(Collectors.toSet());

        var softly = new SoftAssertions();

        softly.assertThat(accessibleCtFields).containsExactlyInAnyOrderElementsOf(expectedFields);
        softly.assertThat(boxClass.getMethod("getWidth")).isNotNull();
        softly.assertThat(boxClass.getMethod("getHeight")).isNotNull();

        softly.assertAll();
    }

    @Test
    void fieldAnnotationTakesPrecedenceOverClassAnnotation() {
        // given
        var model = buildModel("finder/lombok/TypeAndFieldGetterBox.java", "finder/lombok/DataBox.java");
        var boxClass = findClassWithSimpleName(model, "TypeAndFieldGetterBox");
        var testClass = findClassWithSimpleName(model, "DataBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(testClass, boxClass.getReference());

        // then
        var accessibleCtFields = accessibleFields.stream()
                .map(AccessibleField::getActualField)
                .map(CtField.class::cast)
                .collect(Collectors.toSet());

        var softly = new SoftAssertions();

        softly.assertThat(accessibleCtFields).containsExactlyInAnyOrderElementsOf(expectedFields);
        softly.assertThat(boxClass.getMethod("getWidth")).isNotNull();
        softly.assertThat(boxClass.getMethod("getHeight")).isNotNull();

        softly.assertAll();
    }

}
