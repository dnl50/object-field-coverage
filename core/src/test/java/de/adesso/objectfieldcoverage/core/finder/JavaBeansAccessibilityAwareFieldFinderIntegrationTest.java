package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        var expectedFields = List.of(
                new AccessibleField(userClass.getField("id"), findMethodWithSimpleName(userClass, "getId")),
                new AccessibleField(userClass.getField("name"), findMethodWithSimpleName(userClass, "getName")),
                new AccessibleField(userClass.getField("admin"), findMethodWithSimpleName(userClass, "isAdmin")),
                new AccessibleField(userClass.getField("locked"), findMethodWithSimpleName(userClass, "isLocked")),
                new AccessibleField(userClass.getField("staticString"), findMethodWithSimpleName(userClass, "getStaticString"))
        );

        // when
        var actualFields = testSubject.findAccessibleFields(userClass, userClass);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualFields).hasSize(5);
        softly.assertThat(actualFields).contains(expectedFields.get(0));
        softly.assertThat(actualFields).contains(expectedFields.get(1));
        softly.assertThat(actualFields).contains(expectedFields.get(2));
        softly.assertThat(actualFields).contains(expectedFields.get(3));
        softly.assertThat(actualFields).contains(expectedFields.get(4));

        softly.assertAll();
    }

}
