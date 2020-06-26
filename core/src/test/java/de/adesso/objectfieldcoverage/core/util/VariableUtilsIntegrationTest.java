package de.adesso.objectfieldcoverage.core.util;

import de.adesso.objectfieldcoverage.test.AbstractSpoonIntegrationTest;
import org.junit.jupiter.api.Test;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class VariableUtilsIntegrationTest extends AbstractSpoonIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    void findVariablesWithDefaultExpressionReturnsAllVariablesWithoutDefaultExpressionWhenNull() {
        // given
        var model = buildModel("util/VariableUtilTest.java");
        var testClass = findClassWithSimpleName(model, "VariableUtilTest");

        var expectedVariables = testClass.getElements(new TypeFilter<>(CtVariable.class)).stream()
                .filter(variable -> variable.getSimpleName().startsWith("noDefExpr"))
                .collect(Collectors.toList());

        // when
        var actualVariables = VariableUtils.findVariablesWithDefaultExpression(CtVariable.class, null, testClass);

        // then
        assertThat(actualVariables).containsExactlyElementsOf(expectedVariables);
    }

}
