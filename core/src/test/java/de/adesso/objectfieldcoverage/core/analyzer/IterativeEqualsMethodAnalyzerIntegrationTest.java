package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.analyzer.lombok.LombokEqualsMethodAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IterativeEqualsMethodAnalyzerIntegrationTest extends AbstractSpoonIntegrationTest {

    private IterativeEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        var lombokEqualsMethodAnalyzer = new LombokEqualsMethodAnalyzer();

        this.testSubject = new IterativeEqualsMethodAnalyzer(List.of(lombokEqualsMethodAnalyzer));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAccessibleFieldsUsedInEqualsReturnsExpectedFields() {
        // given
        var model = buildModel("analyzer/lombok/DataClass.java", "analyzer/lombok/DataClassExtendingDataClass.java",
                "analyzer/lombok/EqualsAndHashCodeClassCallingSuper.java");
        var dataClazz = findClassWithSimpleName(model, "DataClass");
        var extendingClazz = findClassWithSimpleName(model, "DataClassExtendingDataClass");
        var equalsAndHashCodeClazz = findClassWithSimpleName(model, "EqualsAndHashCodeClassCallingSuper");

        // fields in DataClass.java
        var protectedIntField = (CtField<Integer>) dataClazz.getField("protectedInt");
        var protectedStringField = (CtField<Integer>) dataClazz.getField("protectedString");

        // fields in DataClassExtendingDataClass.java
        var existsField = (CtField<Boolean>) extendingClazz.getField("exists");
        var lengthField = (CtField<Short>) extendingClazz.getField("length");
        var excludedInt = (CtField<Short>) extendingClazz.getField("excludedInt");

        // fields in EqualsAndHashCodeClassCallingSuper.java
        var explicitlyIncluded = (CtField<Object>) equalsAndHashCodeClazz.getField("explicitlyIncluded");
        var notExplicitlyIncluded = (CtField<Object>) equalsAndHashCodeClazz.getField("notExplicitlyIncluded");

        var allAccessibleFields = Set.of(
                new AccessibleField<>(protectedIntField, protectedIntField),
                new AccessibleField<>(protectedStringField, protectedStringField),
                new AccessibleField<>(existsField, existsField),
                new AccessibleField<>(lengthField, lengthField),
                new AccessibleField<>(excludedInt, excludedInt),
                new AccessibleField<>(explicitlyIncluded, explicitlyIncluded),
                new AccessibleField<>(notExplicitlyIncluded, notExplicitlyIncluded)
        );

        var expectedFields = Set.of(
                new AccessibleField<>(existsField, existsField),
                new AccessibleField<>(lengthField, lengthField),
                new AccessibleField<>(explicitlyIncluded, explicitlyIncluded)
        );

        var accessibleSuperFieldsDataClazz = Set.<AccessibleField<?>>of(
                new AccessibleField<>(protectedIntField, protectedIntField),
                new AccessibleField<>(protectedStringField, protectedStringField)
        );

        var accessibleSuperFieldsExtendingClazz = Set.<AccessibleField<?>>of(
                new AccessibleField<>(protectedIntField, protectedIntField),
                new AccessibleField<>(protectedStringField, protectedStringField),
                new AccessibleField<>(existsField, existsField),
                new AccessibleField<>(lengthField, lengthField),
                new AccessibleField<>(excludedInt, excludedInt)
        );

        var accessibleFieldsInSuperTypeMap = Map.of(
                dataClazz.getReference(), accessibleSuperFieldsDataClazz,
                extendingClazz.getReference(), accessibleSuperFieldsExtendingClazz,
                equalsAndHashCodeClazz.getReference(), allAccessibleFields
        );

        // when
        var actualFields = testSubject.findAccessibleFieldsUsedInEquals(equalsAndHashCodeClazz.getReference(), allAccessibleFields,
                accessibleFieldsInSuperTypeMap);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}
