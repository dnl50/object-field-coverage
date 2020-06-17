package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DirectAccessAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private DirectAccessAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DirectAccessAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsCarTest() {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/CarTest.java");
        var carType = findClassWithSimpleName(model, "Car");
        var testClazz = findClassWithSimpleName(model, "CarTest");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer")
        );

        var expectedAccessibleFields = expectedFields.stream()
                .map(accessibleField -> new AccessibleField(accessibleField, accessibleField))
                .collect(Collectors.toList());

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(testClazz, carType.getReference());

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualAccessibleFields).hasSize(4);
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(0));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(1));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(2));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(3));

        softly.assertAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsEngineInnerClass() {
        // given
        var model = buildModel("finder/direct-access/Car.java");
        var carType = findClassWithSimpleName(model, "Car");
        var testClazz = findClassWithSimpleName(model, "Engine");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer"),
                carType.getField("privateStaticManufacturer"),
                carType.getField("privateManufacturer")
        );

        var expectedAccessibleFields = expectedFields.stream()
                .map(accessibleField -> new AccessibleField(accessibleField, accessibleField))
                .collect(Collectors.toList());

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(testClazz, carType.getReference());

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualAccessibleFields).hasSize(6);
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(0));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(1));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(2));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(3));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(4));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(5));

        softly.assertAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsRetailerStaticInnerClass() {
        // given
        var model = buildModel("finder/direct-access/Car.java");
        var carType = findClassWithSimpleName(model, "Car");
        var testClazz = findClassWithSimpleName(model, "Retailer");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer"),
                carType.getField("privateStaticManufacturer"),
                carType.getField("privateManufacturer")
        );

        var expectedAccessibleFields = expectedFields.stream()
                .map(accessibleField -> new AccessibleField(accessibleField, accessibleField))
                .collect(Collectors.toList());

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(testClazz, carType.getReference());

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualAccessibleFields).hasSize(6);
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(0));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(1));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(2));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(3));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(4));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(5));

        softly.assertAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsTruckSubClass() {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/Truck.java");
        var carType = findClassWithSimpleName(model, "Car");
        var testClazz = findClassWithSimpleName(model, "Truck");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer")
        );

        var expectedAccessibleFields = expectedFields.stream()
                .map(accessibleField -> new AccessibleField(accessibleField, accessibleField))
                .collect(Collectors.toList());

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(testClazz, carType.getReference());

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualAccessibleFields).hasSize(3);
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(0));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(1));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(2));

        softly.assertAll();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsPlaneClassFromOtherPackage() {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/Plane.java");
        var carType = findClassWithSimpleName(model, "Car");
        var testClazz = findClassWithSimpleName(model, "Plane");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer")
        );

        var expectedAccessibleFields = expectedFields.stream()
                .map(accessibleField -> new AccessibleField(accessibleField, accessibleField))
                .collect(Collectors.toList());

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(testClazz, carType.getReference());

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualAccessibleFields).hasSize(2);
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(0));
        softly.assertThat(actualAccessibleFields).contains(expectedAccessibleFields.get(1));

        softly.assertAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAccessibleFieldsPlaneDeeplyNestedClassWithPlaneAsAccessingType() {
        // given
        var model = buildModel("finder/direct-access/Plane.java");
        var planeClass = findClassWithSimpleName(model, "Plane");
        var rodInnerClass = findClassWithSimpleName(model, "Rod");

        var lengthField = (CtField<Integer>) rodInnerClass.getField("length");

        var expectedField = new AccessibleField<>(lengthField, lengthField);

        // when
        var actualFields = testSubject.findAccessibleFields(planeClass, rodInnerClass.getReference());

        // then
        assertThat(actualFields).containsExactly(expectedField);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAccessibleFieldsPlaneDeeplyNestedClassWithCarAsAccessingType() {
        // given
        var model = buildModel("finder/direct-access/Plane.java", "finder/direct-access/Car.java");
        var carClass = findClassWithSimpleName(model, "Car");
        var rodInnerClass = findClassWithSimpleName(model, "Rod");

        // when
        var actualFields = testSubject.findAccessibleFields(carClass, rodInnerClass.getReference());

        // then
        assertThat(actualFields).isEmpty();
    }

}
