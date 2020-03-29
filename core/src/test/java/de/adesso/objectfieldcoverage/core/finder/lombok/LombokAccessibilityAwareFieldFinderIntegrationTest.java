package de.adesso.objectfieldcoverage.core.finder.lombok;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LombokAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private LombokAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokAccessibilityAwareFieldFinder();
    }

    @Test
    void dataAnnotatedClass() throws Exception {
        // given
        var model = buildModel("finder/lombok/DataBox.java");
        var boxClass = findClassWithSimpleName(model, "DataBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height"),
                boxClass.getField("depth")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass);

        // then
        assertThat(accessibleFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void getterAnnotatedClass() throws Exception {
        // given
        var model = buildModel("finder/lombok/TypeGetterBox.java");
        var boxClass = findClassWithSimpleName(model, "TypeGetterBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height"),
                boxClass.getField("depth")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass);

        // then
        assertThat(accessibleFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void fieldAnnotatedClass() throws Exception {
        // given
        var model = buildModel("finder/lombok/FieldGetterBox.java");
        var boxClass = findClassWithSimpleName(model, "FieldGetterBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(boxClass, boxClass);

        // then
        assertThat(accessibleFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void fieldAnnotationTakesPrecedenceOverClassAnnotation() throws Exception {
        // given
        var model = buildModel("finder/lombok/TypeAndFieldGetterBox.java", "finder/lombok/DataBox.java");
        var boxClass = findClassWithSimpleName(model, "TypeAndFieldGetterBox");
        var testClazz = findClassWithSimpleName(model, "DataBox");

        var expectedFields = List.of(
                boxClass.getField("width"),
                boxClass.getField("height")
        );

        // when
        var accessibleFields = testSubject.findAccessibleFields(testClazz, boxClass);

        // then
        assertThat(accessibleFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}
