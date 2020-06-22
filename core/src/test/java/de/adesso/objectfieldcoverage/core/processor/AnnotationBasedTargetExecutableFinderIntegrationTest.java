package de.adesso.objectfieldcoverage.core.processor;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import de.adesso.objectfieldcoverage.core.finder.executable.AnnotationBasedTargetExecutableFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.factory.TypeFactory;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnotationBasedTargetExecutableFinderIntegrationTest extends AbstractSpoonIntegrationTest {

    private AnnotationBasedTargetExecutableFinder testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new AnnotationBasedTargetExecutableFinder();
    }

    @Test
    void findTargetExecutablesGeneratesDefaultConstructorWhenNoOtherConstructorPresent() {
        // given
        var model = buildModel("processor/Building.java", "processor/BuildingTest.java");
        var buildingTestClass = findClassWithSimpleName(model, "BuildingTest");
        var givenTestMethod = buildingTestClass.getMethod("testNoArgConstructor");

        // when
        var actualTargetExecutables = testSubject.findTargetExecutables(givenTestMethod, List.of());

        // then
        var expectedTargetExecutable =  findClassWithSimpleName(model, "Building").getConstructor();

        assertThat(actualTargetExecutables).containsExactly(expectedTargetExecutable);
    }

    @Test
    void findTargetExecutablesConstructorWithPrimitiveParameter() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("constructorWithPrimitiveParameter");

        var expectedConstructor = melonClass.getConstructor(new TypeFactory().INTEGER_PRIMITIVE);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedConstructor);
    }

    @Test
    void findTargetExecutableWithNoParameters() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("incrementSeedsNoParameter");

        var expectedMethod = melonClass.getMethod("incrementSeeds");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithPrimitiveParameter() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("constructorWithPrimitiveParameter");

        var expectedConstructor = melonClass.getConstructor(new TypeFactory().INTEGER_PRIMITIVE);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedConstructor);
    }

    @Test
    void findTargetExecutableWithQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("incrementSeedsQualifiedJavaLangParameter");

        var expectedMethod = melonClass.getMethod("incrementSeeds",
                new TypeFactory().STRING);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithNonQualifiedJavaLangParameter() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("incrementSeedsNonQualifiedJavaLangParameter");

        var expectedMethod = melonClass.getMethod("incrementSeeds",
                new TypeFactory().STRING);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedAndNonQualifiedJavaLangParameters() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var testMethod = findClassWithSimpleName(model, "MelonTest")
                .getMethod("incrementSeedsQualifiedAndNonQualifiedJavaLangParameters");

        var stringTypeRef = new TypeFactory().STRING;
        var expectedMethod = melonClass.getMethod("incrementSeeds",
                stringTypeRef, stringTypeRef);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedModelClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("saveMelonQualifiedModelClass");

        var melonTypeRef = melonClass.getReference();
        var expectedMethod = melonServiceClass.getMethod("saveMelon", melonTypeRef);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedModelClassArray() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonClass = findClassWithSimpleName(model, "Melon");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("deleteMelonsArrayParameter");

        var melonTypeRef = new TypeFactory().createArrayReference(melonClass.getReference(), 1);
        var expectedMethod = melonServiceClass.getMethod("deleteMelons", melonTypeRef);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithQualifiedGenericJavaUtilClass() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("deleteMelonsListParameter");

        var listTypeRef = new TypeFactory().LIST;
        var expectedMethod = melonServiceClass.getMethod("deleteMelons", listTypeRef);

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithUnboundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("unboundGenericMethod");

        var expectedMethod = findMethodWithSimpleName(melonServiceClass,"unboundGenericMethod");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutableWithBoundGenericArgument() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("boundGenericMethod");

        var expectedMethod = findMethodWithSimpleName(melonServiceClass,"boundGenericMethod");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactly(expectedMethod);
    }

    @Test
    void findTargetExecutablesContainsMultipleElementsWhenMultipleTestTargetAnnotationsPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var melonServiceClass = findClassWithSimpleName(model, "MelonService");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("multipleMethods");

        var expectedMethods = Set.<CtExecutable<?>>of(
                findMethodWithSimpleName(melonServiceClass,"boundGenericMethod"),
                findMethodWithSimpleName(melonServiceClass,"unboundGenericMethod")
        );

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).containsExactlyInAnyOrderElementsOf(expectedMethods);
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenNoImplicitDefaultConstructorIsPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java",
                "processor/MelonServiceTest.java");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("nonExistentDefaultConstructor");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).isEmpty();
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenClassNotInModel() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java",
                "processor/MelonServiceTest.java");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("nonExistentClass");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).isEmpty();
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenMethodWithNameNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java",
                "processor/MelonServiceTest.java");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("nonExistentMethod");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).isEmpty();
    }

    @Test
    void findTargetExecutablesReturnsEmptyListWhenMethodWithParameterNotPresent() {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonTest.java",
                "processor/MelonServiceTest.java");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("methodWithParameterTypeNotPresent");

        // when
        var actualConstructor = testSubject.findTargetExecutables(testMethod, List.of());

        // then
        assertThat(actualConstructor).isEmpty();
    }

    @Test
    void findTargetExecutableThrowsExceptionWhenModelDoesNotContainQualifiedParameterType()  {
        // given
        var model = buildModel("processor/Melon.java", "processor/MelonService.java",
                "processor/MelonServiceTest.java");
        var testMethod = findClassWithSimpleName(model, "MelonServiceTest")
                .getMethod("parameterTypeNotPartOfModel");
        var qualifiedNameOfNonPresentType = "de.unknown.Test";

        // when / then
        assertThatThrownBy(() -> testSubject.findTargetExecutables(testMethod, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The model does not contain the type '%s'!", qualifiedNameOfNonPresentType);
    }

}
