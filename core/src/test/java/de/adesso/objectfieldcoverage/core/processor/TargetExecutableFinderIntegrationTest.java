package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TargetExecutableFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private TargetExecutableFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TargetExecutableFinder();
    }

    @Test
    void findTargetExecutableGeneratesDefaultConstructorWhenNoOtherConstructorPresent() {
        // given
        var model = buildModel("processor/Building.java");
        var givenMethodIdentifier = "de.adesso.test.Building#Building()";

        // when
        var actualConstructor = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        var softly = new SoftAssertions();

        softly.assertThat(actualConstructor).isPresent();
        softly.assertThat(actualConstructor.get().getParameters()).isEmpty();
        softly.assertThat(actualConstructor.get().isImplicit()).isTrue();

        softly.assertAll();
    }

    @Test
    void findTargetExecutableConstructorWithPrimitiveParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#Melon(int)";

        var melonType = findClassWithName(model, "Melon");
        var expectedConstructor = melonType.getConstructors().stream()
                .findFirst()
                .get();

        // when
        var actualConstructor = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualConstructor).contains(expectedConstructor);
    }

    @Test
    void findTargetExecutableWithNoParameters() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds()";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(0);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithPrimitiveParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(int)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(1);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(java.lang.String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(2);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithNonQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(2);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedAndNonQualifiedJavaLangParameters() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#incrementSeeds(String, java.lang.String)";

        var melonType = findClassWithName(model, "Melon");
        var expectedMethod = melonType.getMethodsByName("incrementSeeds").get(3);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedModelClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#saveMelon(de.adesso.test.Melon)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("saveMelon").get(0);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedModelClassArray() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(de.adesso.test.Melon[])";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("deleteMelons").get(1);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedGenericJavaUtilClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(java.util.List)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("deleteMelons").get(0);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithUnboundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#unboundGenericMethod(Object)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("unboundGenericMethod").get(0);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableWithBoundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#boundGenericMethod(Number)";

        var melonServiceType = findClassWithName(model, "MelonService");
        var expectedMethod = melonServiceType.getMethodsByName("boundGenericMethod").get(0);

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).contains(expectedMethod);
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenNoImplicitDefaultConstructorIsPresent() {
        // given
        var model = buildModel("processor/Melon.java");
        var givenMethodIdentifier = "de.adesso.test.Melon#Melon()";

        // when
        var actualConstructor = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualConstructor).isEmpty();
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenClassNotInModel() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.NotPresent#notPresent()";

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenMethodWithNameNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#notPresent()";

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetExecutableReturnsEmptyOptionalWhenMethodWithParameterNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var givenMethodIdentifier = "de.adesso.test.MelonService#deleteMelons(String)";

        // when
        var actualMethod = testSubject.findTargetExecutable(givenMethodIdentifier, model);

        // then
        assertThat(actualMethod).isEmpty();
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenModelDoesNotContainQualifiedParameterType()  {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java");
        var parameterType = "de.adesso.test.Test";
        var givenMethodIdentifier = String.format("de.adesso.test.MelonService#deleteMelons(%s)", parameterType);

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutable(givenMethodIdentifier, model))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The model does not contain the type '%s'!", parameterType);
    }

}
