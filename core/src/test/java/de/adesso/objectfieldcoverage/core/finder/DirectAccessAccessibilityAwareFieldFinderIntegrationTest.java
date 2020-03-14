package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DirectAccessAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private DirectAccessAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new DirectAccessAccessibilityAwareFieldFinder();
    }

    @Test
    void findAccessibleFieldsCarTest() throws Exception {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/CarTest.java");
        var carType = findClassWithName(model, "Car");
        var testClazz = findClassWithName(model, "CarTest");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(testClazz, carType);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void findAccessibleFieldsEngineInnerClass() throws Exception {
        // given
        var model = buildModel("finder/direct-access/Car.java");
        var carType = findClassWithName(model, "Car");
        var testClazz = findClassWithName(model, "Engine");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer"),
                carType.getField("privateStaticManufacturer"),
                carType.getField("privateManufacturer")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(testClazz, carType);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void findAccessibleFieldsRetailerStaticInnerClass() throws Exception {
        // given
        var model = buildModel("finder/direct-access/Car.java");
        var carType = findClassWithName(model, "Car");
        var testClazz = findClassWithName(model, "Retailer");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer"),
                carType.getField("packagePrivateManufacturer"),
                carType.getField("privateStaticManufacturer"),
                carType.getField("privateManufacturer")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(testClazz, carType);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void findAccessibleFieldsTruckSubClass() throws Exception {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/Truck.java");
        var carType = findClassWithName(model, "Car");
        var testClazz = findClassWithName(model, "Truck");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer"),
                carType.getField("protectedManufacturer")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(testClazz, carType);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    void findAccessibleFieldsPlaneClassFromOtherPackage() throws Exception {
        // given
        var model = buildModel("finder/direct-access/Car.java", "finder/direct-access/Plane.java");
        var carType = findClassWithName(model, "Car");
        var testClazz = findClassWithName(model, "Plane");

        var expectedFields = List.of(
                carType.getField("publicStaticManufacturer"),
                carType.getField("publicManufacturer")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(testClazz, carType);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}