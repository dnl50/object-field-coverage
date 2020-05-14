package de.adesso.objectfieldcoverage.core.graph.util;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.core.graph.AccessibleFieldGraphNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessibleFieldGraphBuilderIntegrationTest extends AbstractSpoonIntegrationTest {

    private AccessibilityAwareFieldFinder fieldFinder;

    @BeforeEach
    void setUp() {
        this.fieldFinder = new JavaBeansAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForCity() {
        // given
        var model = buildModel("graph/City.java");
        var cityClass = findClassWithSimpleName(model, "City");

        var cityNameField = cityClass.getField("name");
        var cityNameGetter = cityClass.getMethodsByName("getName").get(0);

        var cityNameAccessibleField = new AccessibleField(cityNameField, cityNameGetter);

        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        var expectedGraph = new AccessibleFieldGraph(Set.of(cityNameNode));

        // when
        var actualGraph = AccessibleFieldGraphBuilder.buildGraph(List.of(fieldFinder), cityClass, cityClass);

        // then
        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForAddress() {
        // given
        var model = buildModel("graph/Address.java", "graph/City.java");
        var addressClass = findClassWithSimpleName(model, "Address");
        var cityClass = findClassWithSimpleName(model, "City");

        var addressHouseNumberField = addressClass.getField("houseNumber");
        var addressHouseNumberGetter = addressClass.getMethodsByName("getHouseNumber").get(0);
        var addressStreetField = addressClass.getField("street");
        var addressStreetGetter = addressClass.getMethodsByName("getStreet").get(0);
        var addressPostalCodeField = addressClass.getField("postalCode");
        var addressPostalCodeGetter = addressClass.getMethodsByName("getPostalCode").get(0);
        var addressCityField = addressClass.getField("city");
        var addressCityGetter = addressClass.getMethodsByName("getCity").get(0);

        var addressHouseNumberAccessibleField = new AccessibleField(addressHouseNumberField, addressHouseNumberGetter);
        var addressStreetAccessibleField = new AccessibleField(addressStreetField, addressStreetGetter);
        var addressPostalCodeAccessibleField = new AccessibleField(addressPostalCodeField, addressPostalCodeGetter);
        var addressCityAccessibleField = new AccessibleField(addressCityField, addressCityGetter);

        var cityNameField = cityClass.getField("name");
        var cityNameGetter = cityClass.getMethodsByName("getName").get(0);

        var cityNameAccessibleField = new AccessibleField(cityNameField, cityNameGetter);

        var addressHouseNumberNode = AccessibleFieldGraphNode.of(addressHouseNumberAccessibleField);
        var addressStreetNode = AccessibleFieldGraphNode.of(addressStreetAccessibleField);
        var addressPostalCodeNode = AccessibleFieldGraphNode.of(addressPostalCodeAccessibleField);
        var addressCityNode = AccessibleFieldGraphNode.of(addressCityAccessibleField);

        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        addressCityNode.addChildren(Set.of(cityNameNode));

        var expectedGraph = new AccessibleFieldGraph(Set.of(addressHouseNumberNode,
                addressStreetNode, addressPostalCodeNode, addressCityNode));

        // when
        var actualGraph = AccessibleFieldGraphBuilder.buildGraph(List.of(fieldFinder), addressClass, addressClass);

        // then
        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForPerson() {
        // given
        var model = buildModel("graph/Person.java", "graph/Address.java", "graph/City.java");
        var personClass = findClassWithSimpleName(model, "Person");
        var addressClass = findClassWithSimpleName(model, "Address");
        var cityClass = findClassWithSimpleName(model, "City");

        var personNameField = personClass.getField("name");
        var personNameGetter = personClass.getMethodsByName("getName").get(0);
        var personSiblingField = personClass.getField("sibling");
        var personSiblingGetter = personClass.getMethodsByName("getSibling").get(0);
        var personHomeAddressField = personClass.getField("homeAddress");
        var personHomeAddressGetter = personClass.getMethodsByName("getHomeAddress").get(0);
        var personFavouriteCityField = personClass.getField("favouriteCity");
        var personFavouriteCityGetter = personClass.getMethodsByName("getFavouriteCity").get(0);

        var personNameAccessibleField = new AccessibleField(personNameField, personNameGetter);
        var personSiblingAccessibleField = new AccessibleField(personSiblingField, personSiblingGetter);
        var personHomeAddressAccessibleField = new AccessibleField(personHomeAddressField, personHomeAddressGetter);
        var personFavouriteCityAccessibleField = new AccessibleField(personFavouriteCityField, personFavouriteCityGetter);

        var addressHouseNumberField = addressClass.getField("houseNumber");
        var addressHouseNumberGetter = addressClass.getMethodsByName("getHouseNumber").get(0);
        var addressStreetField = addressClass.getField("street");
        var addressStreetGetter = addressClass.getMethodsByName("getStreet").get(0);
        var addressPostalCodeField = addressClass.getField("postalCode");
        var addressPostalCodeGetter = addressClass.getMethodsByName("getPostalCode").get(0);
        var addressCityField = addressClass.getField("city");
        var addressCityGetter = addressClass.getMethodsByName("getCity").get(0);

        var addressHouseNumberAccessibleField = new AccessibleField(addressHouseNumberField, addressHouseNumberGetter);
        var addressStreetAccessibleField = new AccessibleField(addressStreetField, addressStreetGetter);
        var addressPostalCodeAccessibleField = new AccessibleField(addressPostalCodeField, addressPostalCodeGetter);
        var addressCityAccessibleField = new AccessibleField(addressCityField, addressCityGetter);

        var cityNameField = cityClass.getField("name");
        var cityNameGetter = cityClass.getMethodsByName("getName").get(0);

        var cityNameAccessibleField = new AccessibleField(cityNameField, cityNameGetter);

        var personNameNode = AccessibleFieldGraphNode.of(personNameAccessibleField);
        var personSiblingNode = AccessibleFieldGraphNode.of(personSiblingAccessibleField);
        var personHomeAddressNode = AccessibleFieldGraphNode.of(personHomeAddressAccessibleField);
        var personFavouriteCityNode = AccessibleFieldGraphNode.of(personFavouriteCityAccessibleField);

        var addressHouseNumberNode = AccessibleFieldGraphNode.of(addressHouseNumberAccessibleField);
        var addressStreetNode = AccessibleFieldGraphNode.of(addressStreetAccessibleField);
        var addressPostalCodeNode = AccessibleFieldGraphNode.of(addressPostalCodeAccessibleField);
        var addressCityNode = AccessibleFieldGraphNode.of(addressCityAccessibleField);

        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        personSiblingNode.addChildren(Set.of(personNameNode, personSiblingNode, personHomeAddressNode, personFavouriteCityNode));
        personHomeAddressNode.addChildren(Set.of(addressHouseNumberNode, addressStreetNode, addressPostalCodeNode, addressCityNode));
        personFavouriteCityNode.addChildren(Set.of(cityNameNode));
        addressCityNode.addChildren(Set.of(cityNameNode));

        var expectedGraph = new AccessibleFieldGraph(Set.of(personNameNode, personSiblingNode, personHomeAddressNode,
                personFavouriteCityNode));

        // when
        var actualGraph = AccessibleFieldGraphBuilder.buildGraph(List.of(fieldFinder), personClass, personClass);

        // then
        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

}
