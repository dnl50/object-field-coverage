package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaBeansAccessibilityAwareFieldFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private JavaBeansAccessibilityAwareFieldFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new JavaBeansAccessibilityAwareFieldFinder();
    }

    @Test
    void findAccessibleFieldsReturnsExpectedFieldsInUserClass() throws Exception {
        // given
        var model = buildModel("finder/getter/User.java");
        var userClass = findClassWithName(model, "User");

        var expectedFields = List.of(
                userClass.getField("id"),
                userClass.getField("name"),
                userClass.getField("admin"),
                userClass.getField("locked"),
                userClass.getField("staticString")
        );

        // when
        var actualFields = testSubject.findAccessibleFields(userClass, userClass);

        // then
        assertThat(actualFields).containsExactlyInAnyOrderElementsOf(expectedFields);
    }

}
