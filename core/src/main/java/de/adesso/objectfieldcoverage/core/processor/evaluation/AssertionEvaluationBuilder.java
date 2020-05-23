package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for building {@link AssertionEvaluationInformation} instances for the evaluation of
 * {@link de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion}s.
 */
@RequiredArgsConstructor
public class AssertionEvaluationBuilder {

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to a set of accessible
     * fields.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtType<?>>, Set<AccessibleField<?>>> accessibleFields = new HashMap<>();

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to the resulting
     * evaluation instance.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtType<?>>, AssertionEvaluationInformation> typeInformation = new HashMap<>();

    /**
     * A map which contains the evaluation information instances for primitive types. Does not need to be cleared when
     * all other result caches are cleared, since it can only contain up to 16 entries (8 primitive types + their 8
     * wrapper classes). Initialized lazily.
     */
    private final HashMap<CtTypeReference<?>, AssertionEvaluationInformation> primitiveTypeInformation = new HashMap<>(16);

    public AssertionEvaluationInformation buildEvaluationInformation(AbstractAssertion<?> assertion) {
        Objects.requireNonNull(assertion, "The given abstract assertion cannot be null!");

        var assertedTypeRef = assertion.getAssertedExpression()
                .getType();

        if(assertedTypeRef.isPrimitive()) {
            var primitiveType = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedTypeRef.getSimpleName());
            primitiveTypeInformation.putIfAbsent(primitiveType, new AssertionEvaluationInformation(primitiveType));
            return primitiveTypeInformation.get(primitiveType);
        }

        throw new UnsupportedOperationException();
    }

}
