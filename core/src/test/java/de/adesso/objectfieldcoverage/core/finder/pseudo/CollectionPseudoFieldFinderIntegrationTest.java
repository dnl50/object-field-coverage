package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGeneratorImpl;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGeneratorImpl;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtField;
import spoon.reflect.factory.TypeFactory;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionPseudoFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private CollectionPseudoFieldFinder testSubject;

    private TypeFactory typeFactory;

    private CtModel model;

    @BeforeEach
    void setUp() {
        this.model = buildModel();
        this.typeFactory = model.getUnnamedModule()
                .getFactory()
                .Type();

        this.testSubject = new CollectionPseudoFieldFinder(new PseudoClassGeneratorImpl(),
                new PseudoFieldGeneratorImpl());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            List.class, ArrayList.class, LinkedList.class, Queue.class,
            SortedSet.class, LinkedHashSet.class, TreeSet.class
    })
    @SuppressWarnings("unchecked")
    void findOrCreatePseudoFieldsCreatesExpectedFieldsForOrderedCollections(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);
        var givenAccessingType = givenTypeRef.getTypeDeclaration();

        var expectedDeclaringTypeSimpleName = String.format("%s%s",
                StringUtils.capitalize(givenTypeRef.getSimpleName()),
                PseudoClassGenerator.PSEUDO_CLASS_SUFFIX);

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(givenAccessingType, givenTypeRef);

        // then
        var pseudoClass = findClassWithSimpleName(model, expectedDeclaringTypeSimpleName);
        var orderField = (CtField<Boolean>) pseudoClass.getField("order");
        var elementsField = (CtField<Boolean>) pseudoClass.getField("elements");
        var sizeField = (CtField<Boolean>) pseudoClass.getField("size");

        var expectedAccessibleFields = Set.of(
                new AccessibleField<>(orderField, Set.of(), true),
                new AccessibleField<>(elementsField, Set.of(), true),
                new AccessibleField<>(sizeField, Set.of(), true)
        );

       assertThat(actualAccessibleFields).containsExactlyInAnyOrderElementsOf(expectedAccessibleFields);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Map.class, HashMap.class, Collection.class, Set.class, HashSet.class
    })
    @SuppressWarnings("unchecked")
    void findOrCreatePseudoFieldsCreatesExpectedFieldsForUnorderedCollections(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);
        var givenAccessingType = givenTypeRef.getTypeDeclaration();

        var expectedDeclaringTypeSimpleName = String.format("%s%s",
                StringUtils.capitalize(givenTypeRef.getSimpleName()),
                PseudoClassGenerator.PSEUDO_CLASS_SUFFIX);

        // when
        var actualAccessibleFields = testSubject.findAccessibleFields(givenAccessingType, givenTypeRef);

        // then
        var pseudoClass = findClassWithSimpleName(model, expectedDeclaringTypeSimpleName);
        var elementsField = (CtField<Boolean>) pseudoClass.getField("elements");
        var sizeField = (CtField<Boolean>) pseudoClass.getField("size");

        var expectedAccessibleFields = Set.of(
                new AccessibleField<>(elementsField, Set.of(), true),
                new AccessibleField<>(sizeField, Set.of(), true)
        );

        assertThat(actualAccessibleFields).containsExactlyInAnyOrderElementsOf(expectedAccessibleFields);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Map.class, HashMap.class, Collection.class, Set.class, List.class, ArrayList.class, LinkedList.class,
            Queue.class, SortedSet.class, LinkedHashSet.class, TreeSet.class
    })
    void containsPseudoFieldsReturnsTrueForOrderedAndUnorderedCollections(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Object.class})
    void containsPseudoFieldsReturnsFalseWhenTypeIsNoCollection(Class<?> clazz) {
        // given
        var givenTypeRef = typeFactory.createReference(clazz);

        // when
        var actualResult = testSubject.containsPseudoFields(givenTypeRef);

        // then
        assertThat(actualResult).isFalse();
    }

}
