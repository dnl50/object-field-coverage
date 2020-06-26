package de.adesso.objectfieldcoverage.core.processor.evaluation.graph.util;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph;
import de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraphNode;
import de.adesso.objectfieldcoverage.core.finder.JavaBeansAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.pseudo.CollectionPseudoFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.pseudo.PrimitiveTypePseudoFieldFinder;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGeneratorImpl;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGeneratorImpl;
import de.adesso.objectfieldcoverage.core.processor.evaluation.graph.AccessibleFieldGraphBuilder;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import static org.assertj.core.api.Assertions.assertThat;

class AccessibleFieldGraphBuilderIntegrationTest extends AbstractSpoonIntegrationTest {

    private List<AccessibilityAwareFieldFinder> fieldFinders;

    @BeforeEach
    void setUp() {
        this.fieldFinders = List.of(
                new CollectionPseudoFieldFinder(new PseudoClassGeneratorImpl(), new PseudoFieldGeneratorImpl()),
                new PrimitiveTypePseudoFieldFinder(new PseudoClassGeneratorImpl(), new PseudoFieldGeneratorImpl()),
                new JavaBeansAccessibilityAwareFieldFinder());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            boolean.class, Boolean.class,
            char.class, Character.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    })
    void buildGraphReturnsGraphWithSingleNodeForPrimitiveType(Class<?> clazz) {
        // given
        var model = buildModel("graph/City.java");
        var typeFactory = model.getUnnamedModule()
                .getFactory()
                .Type();
        var typeRef = typeFactory.createReference(clazz);
        var accessingType = findClassWithSimpleName(model, "City");

        var testSubject = new AccessibleFieldGraphBuilder(fieldFinders, accessingType);

        var pseudoClassName = StringUtils.capitalize(PrimitiveTypeUtils.getPrimitiveTypeReference(typeRef).getSimpleName())
                + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

        // when
        var actualGraph = testSubject.buildGraph(typeRef);

        // then
        var pseudoClass = findClassWithSimpleName(model, pseudoClassName);

        var pseudoField = pseudoClass.getField("value");
        var accessiblePseudoField = new AccessibleField<>(pseudoField, Set.of(), true);
        var pseudoFieldNode = AccessibleFieldGraphNode.of(accessiblePseudoField);
        var expectedGraph = new AccessibleFieldGraph(Set.of(pseudoFieldNode), typeRef, accessingType.getReference());

        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForCity() {
        // given
        var listPseudoClassName = "List" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

        var model = buildModel("graph/City.java");
        var cityClass = findClassWithSimpleName(model, "City");

        var cityNameField = cityClass.getField("name");
        var cityNameGetter = cityClass.getMethodsByName("getName").get(0);
        var cityNameAccessibleField = new AccessibleField(cityNameField, cityNameGetter);
        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        var houseNumbersField = cityClass.getField("houseNumbers");
        var houseNumbersGetter = cityClass.getMethodsByName("getHouseNumbers").get(0);
        var houseNumbersAccessibleField = new AccessibleField(houseNumbersField, houseNumbersGetter);
        var houseNumbersNode = AccessibleFieldGraphNode.of(houseNumbersAccessibleField);

        var testSubject = new AccessibleFieldGraphBuilder(fieldFinders, cityClass);

        // when
        var actualGraph = testSubject.buildGraph(cityClass.getReference());

        // then
        var listPseudoClass = findClassWithSimpleName(model, listPseudoClassName);
        var sizePseudoField = (CtField<Boolean>) listPseudoClass.getField("size");
        var elementsPseudoField = (CtField<Boolean>) listPseudoClass.getField("elements");
        var orderPseudoField = (CtField<Boolean>) listPseudoClass.getField("order");

        var childNodesOfHouseNumberNode = Set.of(
                AccessibleFieldGraphNode.of(new AccessibleField<>(sizePseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(elementsPseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(orderPseudoField, Set.of(), true))
        );
        houseNumbersNode.addChildren(childNodesOfHouseNumberNode);

        var expectedGraph = new AccessibleFieldGraph(Set.of(cityNameNode, houseNumbersNode), cityClass.getReference(), cityClass.getReference());

        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForAddress() {
        // given
        var listPseudoClassName = "List" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

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
        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        var cityHouseNumbersField = cityClass.getField("houseNumbers");
        var cityHouseNumbersGetter = cityClass.getMethodsByName("getHouseNumbers").get(0);
        var cityHouseNumbersAccessibleField = new AccessibleField(cityHouseNumbersField, cityHouseNumbersGetter);
        var cityHouseNumbersNode = AccessibleFieldGraphNode.of(cityHouseNumbersAccessibleField);

        var addressHouseNumberNode = AccessibleFieldGraphNode.of(addressHouseNumberAccessibleField);
        var addressStreetNode = AccessibleFieldGraphNode.of(addressStreetAccessibleField);
        var addressPostalCodeNode = AccessibleFieldGraphNode.of(addressPostalCodeAccessibleField);
        var addressCityNode = AccessibleFieldGraphNode.of(addressCityAccessibleField);

        var testSubject = new AccessibleFieldGraphBuilder(fieldFinders, addressClass);

        // when
        var actualGraph = testSubject.buildGraph(addressClass.getReference());

        // then
        var listPseudoClass = findClassWithSimpleName(model, listPseudoClassName);
        var sizePseudoField = (CtField<Boolean>) listPseudoClass.getField("size");
        var elementsPseudoField = (CtField<Boolean>) listPseudoClass.getField("elements");
        var orderPseudoField = (CtField<Boolean>) listPseudoClass.getField("order");

        var childNodesOfHouseNumberNode = Set.of(
                AccessibleFieldGraphNode.of(new AccessibleField<>(sizePseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(elementsPseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(orderPseudoField, Set.of(), true))
        );
        cityHouseNumbersNode.addChildren(childNodesOfHouseNumberNode);
        addressCityNode.addChildren(Set.of(cityNameNode, cityHouseNumbersNode));

        var expectedGraph = new AccessibleFieldGraph(Set.of(addressHouseNumberNode,
                addressStreetNode, addressPostalCodeNode, addressCityNode), addressClass.getReference(), addressClass.getReference());

        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForPerson() {
        // given
        var listPseudoClassName = "List" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

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
        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        var cityHouseNumbersField = cityClass.getField("houseNumbers");
        var cityHouseNumbersGetter = cityClass.getMethodsByName("getHouseNumbers").get(0);
        var cityHouseNumbersAccessibleField = new AccessibleField(cityHouseNumbersField, cityHouseNumbersGetter);
        var cityHouseNumbersNode = AccessibleFieldGraphNode.of(cityHouseNumbersAccessibleField);

        var personNameNode = AccessibleFieldGraphNode.of(personNameAccessibleField);
        var personSiblingNode = AccessibleFieldGraphNode.of(personSiblingAccessibleField);
        var personHomeAddressNode = AccessibleFieldGraphNode.of(personHomeAddressAccessibleField);
        var personFavouriteCityNode = AccessibleFieldGraphNode.of(personFavouriteCityAccessibleField);

        var addressHouseNumberNode = AccessibleFieldGraphNode.of(addressHouseNumberAccessibleField);
        var addressStreetNode = AccessibleFieldGraphNode.of(addressStreetAccessibleField);
        var addressPostalCodeNode = AccessibleFieldGraphNode.of(addressPostalCodeAccessibleField);
        var addressCityNode = AccessibleFieldGraphNode.of(addressCityAccessibleField);

        personSiblingNode.addChildren(Set.of(personNameNode, personSiblingNode, personHomeAddressNode, personFavouriteCityNode));
        personHomeAddressNode.addChildren(Set.of(addressHouseNumberNode, addressStreetNode, addressPostalCodeNode, addressCityNode));
        personFavouriteCityNode.addChildren(Set.of(cityNameNode, cityHouseNumbersNode));
        addressCityNode.addChildren(Set.of(cityNameNode, cityHouseNumbersNode));

        var testSubject = new AccessibleFieldGraphBuilder(fieldFinders, personClass);

        // when
        var actualGraph = testSubject.buildGraph(personClass.getReference());

        // then
        var listPseudoClass = findClassWithSimpleName(model, listPseudoClassName);
        var sizePseudoField = (CtField<Boolean>) listPseudoClass.getField("size");
        var elementsPseudoField = (CtField<Boolean>) listPseudoClass.getField("elements");
        var orderPseudoField = (CtField<Boolean>) listPseudoClass.getField("order");

        var childNodesOfHouseNumberNode = Set.of(
                AccessibleFieldGraphNode.of(new AccessibleField<>(sizePseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(elementsPseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(orderPseudoField, Set.of(), true))
        );
        cityHouseNumbersNode.addChildren(childNodesOfHouseNumberNode);

        var expectedGraph = new AccessibleFieldGraph(Set.of(personNameNode, personSiblingNode, personHomeAddressNode,
                personFavouriteCityNode), personClass.getReference(), personClass.getReference());

        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void buildGraphForPersonWithFilter() {
        // given
        var listPseudoClassName = "List" + PseudoClassGenerator.PSEUDO_CLASS_SUFFIX;

        var model = buildModel("graph/Person.java", "graph/Address.java", "graph/City.java");
        var personClass = findClassWithSimpleName(model, "Person");
        var cityClass = findClassWithSimpleName(model, "City");
        var listTypeRef = cityClass.getFactory().Type().LIST;

        // exclude fields from Address
        BiPredicate<AccessibleField<?>, CtTypeReference<?>> givenFilter = (accessibleField, originType) ->
                originType.equals(personClass.getReference())
                        || originType.equals(cityClass.getReference())
                        || originType.getTypeErasure().equals(listTypeRef);

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

        var cityNameField = cityClass.getField("name");
        var cityNameGetter = cityClass.getMethodsByName("getName").get(0);
        var cityNameAccessibleField = new AccessibleField(cityNameField, cityNameGetter);
        var cityNameNode = AccessibleFieldGraphNode.of(cityNameAccessibleField);

        var cityHouseNumbersField = cityClass.getField("houseNumbers");
        var cityHouseNumbersGetter = cityClass.getMethodsByName("getHouseNumbers").get(0);
        var cityHouseNumbersAccessibleField = new AccessibleField(cityHouseNumbersField, cityHouseNumbersGetter);
        var cityHouseNumbersNode = AccessibleFieldGraphNode.of(cityHouseNumbersAccessibleField);

        var personNameNode = AccessibleFieldGraphNode.of(personNameAccessibleField);
        var personSiblingNode = AccessibleFieldGraphNode.of(personSiblingAccessibleField);
        var personHomeAddressNode = AccessibleFieldGraphNode.of(personHomeAddressAccessibleField);
        var personFavouriteCityNode = AccessibleFieldGraphNode.of(personFavouriteCityAccessibleField);

        personSiblingNode.addChildren(Set.of(personNameNode, personSiblingNode, personHomeAddressNode, personFavouriteCityNode));
        personFavouriteCityNode.addChildren(Set.of(cityNameNode, cityHouseNumbersNode));

        var testSubject = new AccessibleFieldGraphBuilder(fieldFinders, personClass);

        // when
        var actualGraph = testSubject.buildGraph(personClass.getReference(), givenFilter);

        // then
        var listPseudoClass = findClassWithSimpleName(model, listPseudoClassName);
        var sizePseudoField = (CtField<Boolean>) listPseudoClass.getField("size");
        var elementsPseudoField = (CtField<Boolean>) listPseudoClass.getField("elements");
        var orderPseudoField = (CtField<Boolean>) listPseudoClass.getField("order");

        var childNodesOfHouseNumberNode = Set.of(
                AccessibleFieldGraphNode.of(new AccessibleField<>(sizePseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(elementsPseudoField, Set.of(), true)),
                AccessibleFieldGraphNode.of(new AccessibleField<>(orderPseudoField, Set.of(), true))
        );
        cityHouseNumbersNode.addChildren(childNodesOfHouseNumberNode);

        var expectedGraph = new AccessibleFieldGraph(Set.of(personNameNode, personSiblingNode, personHomeAddressNode,
                personFavouriteCityNode), personClass.getReference(), personClass.getReference());

        assertThat(actualGraph).isEqualTo(expectedGraph);
    }

}
