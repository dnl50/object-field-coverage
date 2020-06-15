package de.adesso.objectfieldcoverage.core.processor.evaluation.graph;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.analyzer.IterativeEqualsMethodAnalyzer;
import de.adesso.objectfieldcoverage.core.finder.AggregatingAccessibilityAwareFieldFinder;
import de.adesso.objectfieldcoverage.core.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

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
public class ComparedInEqualsMethodBiPredicate implements BiPredicate<AccessibleField<?>, CtType<?>> {

    /**
     * A regex matching the {@code java} package and any sub package. It is assumed that the package name which
     * this regex matches is a valid package name.
     */
    private static final String JAVA_PACKAGE_REGEX = "^java(\\..*)?$";

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
     * @param originType
     *          The {@link CtType} which was fed as the second argument into the
     *          {@link AccessibilityAwareFieldFinder#findAccessibleFields(CtType, CtType)} method which lead
     *          to the given {@code accessibleField} to be returned, not {@code null}. The given {@code accessibleField}
     *          must therefore be a member of this type.
     *
     * @return
     *          {@code true}, if the given {@code originType} is declared in the {@code java} package or the given
     *          {@code accessibleField}'s {@link AccessibleField#getActualField() underlying field} is compared in the
     *          {@link Object#equals(Object)} method of the given {@code originType}. {@code false} is returned otherwise.
     */
    @Override
    public boolean test(AccessibleField<?> accessibleField, CtType<?> originType) {
        if(!(originType instanceof CtClass)) {
            log.warn("CtType '{}', from which the accessible field '{}' originates from, is not CtClass!",
                    originType.getQualifiedName(), accessibleField);
            return false;
        }

        var originClass = (CtClass<?>) originType;

        if(isInJavaPackage(originClass)) {
            return true;
        }

        var superClassesIncludingClass = TypeUtil.findExplicitSuperClassesIncludingClass(originClass);
        var aggregatingFieldFinder = new AggregatingAccessibilityAwareFieldFinder(fieldFinders);
        Map<CtType<?>, Set<AccessibleField<?>>> accessibleFieldsInSuperTypes = superClassesIncludingClass.stream()
                .collect(Collectors.toMap(Function.identity(), t -> Set.copyOf(aggregatingFieldFinder.findAccessibleFields(t, t))));

        var accessibleFields = new IterativeEqualsMethodAnalyzer(equalsMethodAnalyzers)
                    .findAccessibleFieldsUsedInEquals(originClass, Set.of(accessibleField), accessibleFieldsInSuperTypes);

        return !accessibleFields.isEmpty();
    }

    /**
     *
     * @param type
     *          The {@link CtType} to check the package of, not {@code null}.
     *
     * @return
     *          {@code true}, if the package the given {@code type} is declared in the {@code java} package or any
     *          sub package. {@code false} is returned otherwise.
     */
    private boolean isInJavaPackage(CtType<?> type) {
        return type.getPackage()
                .getQualifiedName()
                .matches(JAVA_PACKAGE_REGEX);
    }

}
