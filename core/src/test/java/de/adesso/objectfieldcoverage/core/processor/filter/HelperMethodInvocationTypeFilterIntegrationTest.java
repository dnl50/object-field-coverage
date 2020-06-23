package de.adesso.objectfieldcoverage.core.processor.filter;

import de.adesso.objectfieldcoverage.core.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.Test;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelperMethodInvocationTypeFilterIntegrationTest extends AbstractSpoonIntegrationTest {

    @Test
    void matchesReturnsExpectedHelperMethods() {
        // given
        var model = buildModel("processor/filter/Test.java", "processor/filter/AbstractTest.java",
                "processor/filter/Other.java", "processor/filter/OtherInterface.java");
        var testClass = findClassWithSimpleName(model, "Test");

        var testMethod = findMethodWithSimpleName(testClass, "test");
        var testSubject = new HelperMethodInvocationTypeFilter(testMethod);

        var allInvocations = testMethod.getElements(new TypeFilter<>(CtInvocation.class));
        var expectedInvocations = List.<CtInvocation<?>>of(
                allInvocations.get(0),
                allInvocations.get(5)
        );

        // when
        var actualHelperMethodInvocations = testMethod.getElements(testSubject);

        // then
        assertThat(actualHelperMethodInvocations).containsExactlyElementsOf(expectedInvocations);
    }

}
