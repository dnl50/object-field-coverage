package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TargetMethodFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private TargetMethodFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TargetMethodFinder();
    }

    @Test
    void findTargetMethodWithNoParameters() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds()";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(0);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithPrimitiveParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(int)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(1);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(java.lang.String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(2);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithNonQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(2);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithQualifiedAndNonQualifiedJavaLangParameters() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(String, java.lang.String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(3);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithQualifiedModelClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#saveMelon(de.adesso.test.Melon)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("saveMelon").get(0);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithQualifiedModelClassArray() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(de.adesso.test.Melon[])";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("deleteMelons").get(1);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithQualifiedGenericJavaUtilClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(java.util.List)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("deleteMelons").get(0);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithUnboundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#unboundGenericMethod(Object)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("unboundGenericMethod").get(0);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodWithBoundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#boundGenericMethod(Number)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("boundGenericMethod").get(0);

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenClassNotInModel() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.NotPresent#notPresent()";

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodWithNameNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#notPresent()";

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetMethodReturnsEmptyOptionalWhenMethodWithParameterNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(String)";

        // when
        var actualMethod = testSubject.findTargetMethod(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetMethodThrowsExceptionWhenModelDoesNotContainQualifiedParameterType()  {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var parameterType = "de.adesso.test.Test";
        var givenMethodIdentifier = String.format("de.adesso.test.MelonService#deleteMelons(%s)", parameterType);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetMethod(givenMethodIdentifier, model))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The model does not contain the type '%s'!", parameterType);
    }

}
