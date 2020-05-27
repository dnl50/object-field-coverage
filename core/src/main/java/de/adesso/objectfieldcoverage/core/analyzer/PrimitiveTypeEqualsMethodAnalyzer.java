package de.adesso.objectfieldcoverage.core.analyzer;

import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link CtMethodEqualsMethodAnalyzer} implementation for generated/handwritten {@link Object#equals(Object)}
 * method implementations which use reference comparison for primitive types.
 */
@Slf4j
public class PrimitiveTypeEqualsMethodAnalyzer extends CtMethodEqualsMethodAnalyzer {

    /**
     *
     * @param equalsMethod
     *          The equals method which should be analyzed, not {@code null}.
     *
     * @return
     *          A set containing the left hand operands of each {@link CtBinaryOperator} comparing
     *          two primitive type expressions.
     */
    @Override
    protected Set<CtExpression<?>> findExpressionsComparedInEqualsMethod(CtMethod<Boolean> equalsMethod) {
        var primitiveTypeEqualsBinaryOperations = equalsMethod.getElements(new PrimitiveTypeEqualsBinaryOperatorFilter());

        if(primitiveTypeEqualsBinaryOperations.isEmpty()) {
            return Set.of();
        }

        Set<CtExpression<?>> leftHandOperations = primitiveTypeEqualsBinaryOperations.stream()
                .map(CtBinaryOperator::getLeftHandOperand)
                .collect(Collectors.toSet());

        log.info("Equals method of '{}' contains {} binary equals comparisons!",
                equalsMethod.getDeclaringType().getQualifiedName(), primitiveTypeEqualsBinaryOperations.size());

        return leftHandOperations;
    }

    /**
     * {@link TypeFilter} which filters for {@link CtBinaryOperator}s with their being {@link CtBinaryOperator#getKind()
     * kind} set to {@link BinaryOperatorKind#EQ} and their right and left hand operation being of primitive type.
     */
    private static class PrimitiveTypeEqualsBinaryOperatorFilter extends TypeFilter<CtBinaryOperator<?>> {

        public PrimitiveTypeEqualsBinaryOperatorFilter() {
            super(CtBinaryOperator.class);
        }

        /**
         *
         * @param binaryOperator
         *          The binary operator which should be matched, not {@code null}.
         *
         * @return
         *          {@code true}, if the given {@code binaryOperator}'s kind is set to {@link BinaryOperatorKind#EQ}
         *          and its right and left hand operations return primitive types. {@code false} is returned
         *          otherwise.
         */
        @Override
        public boolean matches(CtBinaryOperator<?> binaryOperator) {
            if(!super.matches(binaryOperator)) {
                return false;
            }

            return binaryOperator.getKind() == BinaryOperatorKind.EQ &&
                    binaryOperator.getLeftHandOperand().getType().isPrimitive() &&
                    binaryOperator.getRightHandOperand().getType().isPrimitive();
        }
    }

}
