package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

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

        var expectedFields = List.<AccessibleField<?>>of(
                new AccessibleField<>((CtField<String>) userClass.getField("id"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getId")),
                new AccessibleField<>((CtField<String>) userClass.getField("name"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getName")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("admin"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isAdmin")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("locked"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isLocked")),
                new AccessibleField<>((CtField<String>) userClass.getField("protectedName"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getProtectedName"))
        );

        // when
        var actualFields = (List<AccessibleField<?>>) testSubject.findAccessibleFields(userClass, userClass);

        // then
        assertThat(actualFields).containsExactlyElementsOf(expectedFields);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void findAccessibleFieldsReturnsExpectedFieldsInUserClassOmittingProtected() {
        // given
        var model = buildModel("finder/getter/User.java", "finder/getter/OtherClassInSubPackage.java");
        var userClass = findClassWithSimpleName(model, "User");
        var classInSubPackage = findClassWithSimpleName(model,"OtherClassInSubPackage");

        var expectedFields = List.<AccessibleField<?>>of(
                new AccessibleField<>((CtField<String>) userClass.getField("id"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getId")),
                new AccessibleField<>((CtField<String>) userClass.getField("name"), (CtMethod<String>) findMethodWithSimpleName(userClass, "getName")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("admin"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isAdmin")),
                new AccessibleField<>((CtField<Boolean>) userClass.getField("locked"), (CtMethod<Boolean>) findMethodWithSimpleName(userClass, "isLocked"))
        );

        // when
        var actualFields = (List<AccessibleField<?>>) testSubject.findAccessibleFields(classInSubPackage, userClass);

        // then
        assertThat(actualFields).containsExactlyElementsOf(expectedFields);
    }

}
