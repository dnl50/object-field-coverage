package de.adesso.objectfieldcoverage.core.processor;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

import static de.adesso.objectfieldcoverage.core.util.VariableUtils.findVariablesWithDefaultExpression;

public class ChildExpressionFinder {

    public Set<CtExpression<?>> findChildExpressions(CtInvocation<?> targetMethodInvocation) {
        return Set.of();
    }

    private boolean getVariableContainingInvocationResult(CtInvocation<?> invocation, CtMethod<?> methodContainingInvocation) {
        var localVariableContainingInvocationResult = findVariablesWithDefaultExpression(CtLocalVariable.class, invocation, methodContainingInvocation);
        return false;
    }

    private Set<CtExpression<?>> findExpressionsReferencingInvocation(CtInvocation<?> invocation, CtMethod<?> methodContainingInvocation) {
        return Set.of();
    }

}
