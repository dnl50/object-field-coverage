package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.analyzer.method.PrimitiveTypeEqualsMethodEqualsMethodAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PrimitiveTypeEqualsMethodAnalyzerIntegrationTest extends AbstractSpoonIntegrationTest {

    private PrimitiveTypeEqualsMethodEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new PrimitiveTypeEqualsMethodEqualsMethodAnalyzer();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFieldsComparedInEqualsMethodReturnsAllPrimitiveTypeFieldsDeclaredInTypeWhenCompared() {
        // given
        var model = buildModel("analyzer/primitive/ClassComparingAllFields.java");
        var givenClazz = findClassWithSimpleName(model, "ClassComparingAllFields");

        var charField = (CtField<Character>) givenClazz.getField("charField");
        var byteField = (CtField<Byte>) givenClazz.getField("byteField");
        var shortField = (CtField<Short>) givenClazz.getField("shortField");
        var intField = (CtField<Integer>) givenClazz.getField("intField");
        var longField = (CtField<Long>) givenClazz.getField("longField");
        var booleanField = (CtField<Boolean>) givenClazz.getField("booleanField");
        var floatField = (CtField<Float>) givenClazz.getField("floatField");
        var doubleField = (CtField<Double>) givenClazz.getField("doubleField");
        var stringField = (CtField<String>) givenClazz.getField("stringField");

        var expectedAccessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(charField,  charField),
                new AccessibleField<>(byteField,  byteField),
                new AccessibleField<>(shortField,  shortField),
                new AccessibleField<>(intField,  intField),
                new AccessibleField<>(longField,  longField),
                new AccessibleField<>(booleanField,  booleanField),
                new AccessibleField<>(floatField,  floatField),
                new AccessibleField<>(doubleField,  doubleField)
        );

        var allAccessibleFields = new HashSet<>(expectedAccessibleFields);
        allAccessibleFields.add(new AccessibleField<>(stringField, stringField));

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(givenClazz.getReference(), allAccessibleFields);

        // then
        assertThat(actualResult).containsExactlyInAnyOrderElementsOf(expectedAccessibleFields);
    }

}
