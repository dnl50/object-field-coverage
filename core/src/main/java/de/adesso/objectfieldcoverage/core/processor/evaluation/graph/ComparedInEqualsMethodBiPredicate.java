package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.analyzer.IterativeEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.finder.AccessibilityAwareFieldFinderChain;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link BiPredicate} implementation which can be used to build a subgraph of a {@link de.adesso.objectfieldcoverage.api.evaluation.graph.AccessibleFieldGraph}
 * which only includes {@link AccessibleField}s which are compared in the {@link Object#equals(Object)} method
 * of corresponding {@link CtType}.
 */
@Slf4j
@RequiredArgsConstructor
public class ComparedInEqualsMethodBiPredicate implements BiPredicate<AccessibleField<?>, CtTypeReference<?>> {

    /**
     * The {@link EqualsMethodAnalyzer}s which are used to filter out {@link AccessibleField}s which
     * are not compared in the equals method.
    */
    private final List<EqualsMethodAnalyzer> equalsMethodAnalyzers;

    /**
     * The {@link AccessibilityAwareFieldFinder}s which are used to find all {@link AccessibleField}s
     * which are accessible in the superclass hierarchy of a given {@link CtType}.
     */
    private final List<AccessibilityAwareFieldFinder> fieldFinders;

    /**
     * @implNote The given {@link CtType} is expected to be a {@link CtClass} instance.
     *
     * @param accessibleField
     *          The {@link AccessibleField} instance to test, not {@code null}.
     *
     * @param originTypeRef
     *          The {@link CtTypeReference} which was fed as the second argument into the
     *          {@link AccessibilityAwareFieldFinder#findAccessibleFields(CtType, CtTypeReference)} method which lead
     *          to the given {@code accessibleField} to be returned, not {@code null}. The given {@code accessibleField}
     *          must therefore be a member of this type.
     *
     * @return
     *          {@code true}, if the given {@code originTypeRef} is declared in the {@code java} package or the given
     *          {@code accessibleField}'s {@link AccessibleField#getActualField() underlying field} is compared in the
     *          {@link Object#equals(Object)} method of the given {@code originTypeRef}. {@code false} is returned otherwise.
     */
    @Override
    public boolean test(AccessibleField<?> accessibleField, CtTypeReference<?> originTypeRef) {
        if(!(originTypeRef.isClass())) {
            log.warn("CtType '{}', from which the accessible field '{}' originates from, is not CtClass!",
                    originTypeRef.getQualifiedName(), accessibleField);
            return false;
        }

        var superClassesIncludingClass = TypeUtils.findExplicitSuperClassesIncludingClass(originTypeRef);
        var aggregatingFieldFinderChain = new AccessibilityAwareFieldFinderChain(fieldFinders);
        Map<CtTypeReference<?>, Set<AccessibleField<?>>> accessibleFieldsInSuperTypes = superClassesIncludingClass.stream()
                .collect(Collectors.toMap(Function.identity(), c -> Set.copyOf(aggregatingFieldFinderChain.findAccessibleFields(c.getTypeDeclaration(), c))));

        var accessibleFields = new IterativeEqualsMethodAnalyzer(equalsMethodAnalyzers)
                    .findAccessibleFieldsUsedInEquals(originTypeRef, Set.of(accessibleField), accessibleFieldsInSuperTypes);

        return !accessibleFields.isEmpty();
    }

}
