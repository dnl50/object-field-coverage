package de.adesso.objectfieldcoverage.core.analyzer;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectsEqualsMethodAnalyzerIntegrationTest extends AbstractSpoonIntegrationTest {

    private ObjectsEqualsMethodAnalyzer testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new ObjectsEqualsMethodAnalyzer();
    }

    @Test
    void overridesEqualsFalseWhenEqualsMethodNotOverriddenInClass() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassNotOverridingEquals.java");
        var givenClazz = findClassWithSimpleName(model, "ClassNotOverridingEquals");

        // when
        var actualResult = testSubject.overridesEquals(givenClazz);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void overridesEqualsTrueWhenEqualsMethodOverriddenInClass() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEquals.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOverridingEquals");

        // when
        var actualResult = testSubject.overridesEquals(givenClazz);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void overridesEqualsFalseWhenEqualsMethodOverriddenInSuperClass() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEquals.java",
                "analyzer/objects/simple/ClassWithOverriddenEqualsInSuperClass.java");
        var givenClazz = findClassWithSimpleName(model, "ClassWithOverriddenEqualsInSuperClass");

        // when
        var actualResult = testSubject.overridesEquals(givenClazz);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void callsSuperReturnsFalseWhenSuperMethodNotInvoked() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEquals.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOverridingEquals");

        // when
        var actualResult = testSubject.callsSuper(givenClazz);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void callsSuperReturnsFalseWhenSuperMethodInvokedButResultIsIgnored() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEqualsWithIgnoringResultOfSuperInvocation.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOverridingEqualsWithIgnoringResultOfSuperInvocation");

        // when
        var actualResult = testSubject.callsSuper(givenClazz);

        // then
        assertThat(actualResult).isFalse();
    }

    @Test
    void callsSuperReturnsTrueWhenSuperMethodInvokedAndResultIsReturned() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEqualsWithCallingSuper.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOverridingEqualsWithCallingSuper");

        // when
        var actualResult = testSubject.callsSuper(givenClazz);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    void callsSuperReturnsTrueWhenSuperMethodInvokedAndResultIsAssignedToLocalVariable() {
        // given
        var model = buildModel("analyzer/objects/simple/ClassOverridingEqualsWithCallingSuperAssigningToVariable.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOverridingEqualsWithCallingSuperAssigningToVariable");

        // when
        var actualResult = testSubject.callsSuper(givenClazz);

        // then
        assertThat(actualResult).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFieldsComparedInEqualsMethodReturnsAllFieldsDeclaredInClassWhenAllCompared() {
        // given
        var model = buildModel("analyzer/objects/complex/ClassComparingAllFields.java");
        var givenClazz = findClassWithSimpleName(model, "ClassComparingAllFields");

        var firstField = (CtField<String>) givenClazz.getField("first");
        var firstFieldGetter = givenClazz.<String>getMethod("getFirst");
        var secondField = (CtField<String>) givenClazz.getField("second");
        var secondFieldGetter = givenClazz.<String>getMethod("getSecond");

        var accessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(firstField,  Set.of(firstField, firstFieldGetter)),
                new AccessibleField<>(secondField, Set.of(secondField, secondFieldGetter))
        );

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(givenClazz, accessibleFields);

        // then
        assertThat(actualResult).containsExactlyInAnyOrderElementsOf(accessibleFields);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFieldsComparedInEqualsMethodReturnsAllFieldsDeclaredInClassAndSuperClassWhenAllCompared() {
        // given
        var model = buildModel("analyzer/objects/complex/ClassComparingAllFields.java",
                "analyzer/objects/complex/ExtendedClassComparingAllFields.java");
        var superClazz = findClassWithSimpleName(model, "ClassComparingAllFields");
        var givenClazz = findClassWithSimpleName(model, "ExtendedClassComparingAllFields");

        var firstField = (CtField<String>) superClazz.getField("first");
        var firstFieldGetter = superClazz.<String>getMethod("getFirst");
        var secondField = (CtField<String>) superClazz.getField("second");
        var secondFieldGetter = superClazz.<String>getMethod("getSecond");
        var thirdField = (CtField<String>) givenClazz.getField("third");
        var thirdFieldGetter = givenClazz.<String>getMethod("getThird");
        var fourthField = (CtField<String>) givenClazz.getField("fourth");
        var fourthFieldGetter = givenClazz.<String>getMethod("getFourth");

        var accessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(firstField,  Set.of(firstField, firstFieldGetter)),
                new AccessibleField<>(secondField, Set.of(secondField, secondFieldGetter)),
                new AccessibleField<>(thirdField, Set.of(thirdField, thirdFieldGetter)),
                new AccessibleField<>(fourthField, Set.of(fourthField, fourthFieldGetter))
        );

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(givenClazz, accessibleFields);

        // then
        assertThat(actualResult).containsExactlyInAnyOrderElementsOf(accessibleFields);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findFieldsComparedInEqualsMethodReturnsAllIncludedFieldDeclaredInClass() {
        // given
        var model = buildModel("analyzer/objects/complex/ClassOmittingSingleFieldFromEquals.java");
        var givenClazz = findClassWithSimpleName(model, "ClassOmittingSingleFieldFromEquals");

        var included = (CtField<String>) givenClazz.getField("included");
        var notIncluded = (CtField<String>) givenClazz.getField("notIncluded");

        var accessibleFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>(included, included),
                new AccessibleField<>(notIncluded, notIncluded)
        );

        var expectedResult = Set.of(new AccessibleField<>(included, included));

        // when
        var actualResult = testSubject.findFieldsComparedInEqualsMethod(givenClazz, accessibleFields);

        // then
        assertThat(actualResult).containsExactlyElementsOf(expectedResult);
    }

}
