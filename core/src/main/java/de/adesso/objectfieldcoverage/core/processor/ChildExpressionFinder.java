package de.adesso.objectfieldcoverage.core.processor;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Set;

public class ChildExpressionFinder {

    public Set<CtExpression<?>> findChildExpressions(CtInvocation<?> targetMethodInvocation) {
        return Set.of();
    }

    private boolean getVariableContainingInvocationResult(CtInvocation<?> invocation, CtMethod<?> methodContainingInvocation) {
        var variablesWithDefaultExpression = invocation.getElements(new TypeFilter<>(CtElement.class));
        return false;
    }

}
