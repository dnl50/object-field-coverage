package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JavaBeansAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private JavaBeansAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JavaBeansAccessibilityAwareFieldFinder();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsExpectedFieldsInUserClass() {
        // given
        var model = buildModel("finder/getter/User.java");
        var userClass = findClassWithSimpleName(model, "User");

        var expectedFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>((CtField<String>) userClass.getField("id"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getId")),
                new AccessibleField<>((CtField<String>) userClass.getField("name"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getName")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("admin"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isAdmin")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("locked"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isLocked")),
                new AccessibleField<>((CtField<String>) userClass.getField("protectedName"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getProtectedName"))
        );

        // when
        var actualFields = (List<AccessibleField<?>>) testSubject.findAccessibleFields(userClass, userClass.getReference());

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsExpectedFieldsInUserClassOmittingProtected() {
        // given
        var model = buildModel("finder/getter/User.java", "finder/getter/OtherClassInSubPackage.java");
        var userClass = findClassWithSimpleName(model, "User");
        var classInSubPackage = findClassWithSimpleName(model,"OtherClassInSubPackage");

        var expectedFields = Set.<AccessibleField<?>>of(
                new AccessibleField<>((CtField<String>) userClass.getField("id"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getId")),
                new AccessibleField<>((CtField<String>) userClass.getField("name"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getName")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("admin"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isAdmin")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("locked"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isLocked"))
        );

        // when
        var actualFields = (List<AccessibleField<?>>) testSubject.findAccessibleFields(classInSubPackage, userClass.getReference());

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAccessibleFieldsReturnsExpectedFieldsWithMethodAccessingGetterInSuperType() {
        // given
        var model = buildModel("finder/getter/Pair.java");
        var pairClass = findClassWithSimpleName(model, "Pair");
        var pairImplClass = findClassWithSimpleName(model, "PairImpl");

        var leftField = (CtField<Integer>) pairImplClass.getField("left");
        var getLeftMethod = (CtMethod<Integer>) findMethodWithSimpleName(pairImplClass, "getLeft");
        var getKeyMethod = (CtMethod<Integer>) findMethodWithSimpleName(pairClass, "getKey");

        var rightField = (CtField<Integer>) pairImplClass.getField("right");
        var getRightMethod = (CtMethod<Integer>) findMethodWithSimpleName(pairImplClass, "getRight");
        var getValueMethod = (CtMethod<Integer>) findMethodWithSimpleName(pairClass, "getValue");

        var expectedFields =  Set.<AccessibleField<?>>of(
                new AccessibleField<>(leftField, Set.of(getLeftMethod, getKeyMethod)),
                new AccessibleField<>(rightField, Set.of(getRightMethod, getValueMethod)
        ));

        // when
        var actualFields = testSubject.findAccessibleFields(pairImplClass, pairImplClass.getReference());

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}
