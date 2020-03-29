package de.adesso.objectfieldcoverage.core.processor.evaluation;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EvaluationTreeBuilder {

    /**
     * The field finders which are used to find all accessible fields in the
     * asserted types.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to a set of accessible
     * fields. Result cache to increase performance.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtClass<?>>, Set<CtField<?>>> analyzedTypes = new HashMap<>();

    /**
     * A map which contains entries mapping a (asserted type, test class) pair to the resulting
     * evaluation instance. A result cache to increase performance.
     */
    private final HashMap<Pair<CtTypeReference<?>, CtType<?>>, AssertionEvaluationInformation> typeInformation = new HashMap<>();

    /**
     * A map which contains the evaluation information instances for primitive types. Does not need to be cleared when
     * all other result caches are cleared, since it can only contain up to 8 entries which will be used quite often.
     */
    private final HashMap<CtTypeReference<?>, AssertionEvaluationInformation> primitiveTypeInformation = new HashMap<>(8);

    public AssertionEvaluationInformation buildEvaluationInformation(CtTypeReference<?> assertedTypeRef, CtClass<?> testClazz) {
        Objects.requireNonNull(assertedTypeRef, "assertedTypeRef cannot be null!");
        Objects.requireNonNull(testClazz, "testClazz cannot be null!");

        if(assertedTypeRef.isPrimitive()) {
            var primitiveType = PrimitiveTypeUtils.getPrimitiveTypeReference(assertedTypeRef.getSimpleName());
            primitiveTypeInformation.putIfAbsent(primitiveType, new AssertionEvaluationInformation(primitiveType));
            return primitiveTypeInformation.get(primitiveType);
        }

        //TODO: implement
        throw new UnsupportedOperationException();
    }

    /**
     * Passes the given {@code assertedType} and {@code testClazz} to each {@link AccessibilityAwareFieldFinder}
     * and combines the result in a single set.
     *
     * @param assertedType
     *          The type to get the fields of which are accessible from the given {@code testClazz},
     *          not {@code null}.
     *
     * @param testClazz
     *          The test class from which the fields should be accessible from, not {@code null}.
     *
     * @return
     *          A set containing the accessible fields of the given {@code assertedType}.
     */
    private Set<CtField<?>> findAccessibleFields(CtType<?> assertedType, CtClass<?> testClazz) {
        return fieldFinders.stream()
                .map(fieldFinder -> fieldFinder.findAccessibleFields(testClazz, assertedType))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Clears the internal caches used to increase the performance.
     */
    public void clearCache() {
        analyzedTypes.clear();
        typeInformation.clear();
    }

}
