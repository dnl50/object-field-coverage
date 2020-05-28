package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.analyzer.lombok.LombokEqualsMethodAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LombokEqualsMethodAnalyzerIntegrationTest extends AbstractSpoonIntegrationTest {

    private LombokEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new LombokEqualsMethodAnalyzer();
    }

    @Test
    @SuppressWarnings("unchecked")
    void dataClassReturnsAllFields() {
        // given
        var model = buildModel("analyzer/lombok/DataClass.java");
        var clazz = findClassWithSimpleName(model, "DataClass");
        var protectedIntField = (CtField<Integer>) clazz.getField("protectedInt");
        var protectedStringField = (CtField<Integer>) clazz.getField("protectedString");

        var accessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(protectedIntField, protectedIntField),
                new AccessibleField<>(protectedStringField, protectedStringField)
        );

        // when
        var fieldComparedInEquals = testSubject.findFieldsComparedInEqualsMethod(clazz, accessibleFields);

        // then
        assertThat(fieldComparedInEquals).containsExactlyInAnyOrderElementsOf(accessibleFields);
    }

    @Test
    @SuppressWarnings("unchecked")
    void dataClassExtendingDataClassReturnsAllDeclaredInClass() {
        // given
        var model = buildModel("analyzer/lombok/DataClass.java", "analyzer/lombok/DataClassExtendingDataClass.java");
        var clazz = findClassWithSimpleName(model, "DataClass");
        var extendingClazz = findClassWithSimpleName(model, "DataClassExtendingDataClass");
        var protectedIntField = (CtField<Integer>) clazz.getField("protectedInt");
        var protectedStringField = (CtField<Integer>) clazz.getField("protectedString");
        var existsField = (CtField<Boolean>) extendingClazz.getField("exists");
        var lengthField = (CtField<Short>) extendingClazz.getField("length");
        var excludedInt = (CtField<Short>) extendingClazz.getField("excludedInt");

        var accessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(protectedIntField, protectedIntField),
                new AccessibleField<>(protectedStringField, protectedStringField),
                new AccessibleField<>(existsField, existsField),
                new AccessibleField<>(lengthField, lengthField),
                new AccessibleField<>(excludedInt, excludedInt)
        );

        var expectedFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(existsField, existsField),
                new AccessibleField<>(lengthField, lengthField)
        );

        // when
        var fieldComparedInEquals = testSubject.findFieldsComparedInEqualsMethod(extendingClazz, accessibleFields);

        // then
        assertThat(fieldComparedInEquals).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}
