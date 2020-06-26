package de.adesso.objectfieldcoverage.core.processor;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;

import java.util.Set;

public class ChildExpressionFinder {

    public Set<CtExpression<?>> findChildExpressions(CtInvocation<?> targetMethodInvocation) {
        return Set.of();
    }


    private boolean getVariableContainingInvocationResult(CtInvocation<?> invocation, CtMethod<?> methodContainingInvocation) {
        return false;
    }

//    private static class ExpressionFilter extends TypeFilter<CtExpression<?>> {
//
//        private final CtExpression<?> expression;
//
//        public ExpressionFilter(CtExpression<?> expression) {
//            super(CtExpression.class);
//
//            this.expression = expression;
//        }
//
//        @Override
//        public boolean matches(CtExpression<?> element) {
//            if(!super.matches(element)) {
//                return false;
//            }
//
//
//        }
//
//    }

}
