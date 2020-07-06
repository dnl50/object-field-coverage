package de.adesso.objectfieldcoverage.core.analyzer.method;

import de.adesso.objectfieldcoverage.api.AccessibleField;
import de.adesso.objectfieldcoverage.api.EqualsMethodAnalyzer;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract base class extending {@link EqualsMethodAnalyzer} for implementations which analyze
 * a handwritten/generated equals method. Every implementation focuses on a single way of comparing
 * objects.
 */
@Slf4j
@NoArgsConstructor
public abstract class CtMethodEqualsMethodAnalyzer extends EqualsMethodAnalyzer {

    /**
     * The {@link TypeFactory} used to get a {@link CtTypeReference} for the required types for.
     */
    private static final TypeFactory TYPE_FACTORY = new TypeFactory();

    /**
     * A {@link CtTypeReference} to the {@link Object} class.
     */
    private static final CtTypeReference<Object> OBJECT_TYPE_REF = TYPE_FACTORY.OBJECT;

    /**
     * A {@link CtTypeReference} to {@code boolean} primitive type.
     */
    private static final CtTypeReference<Boolean> BOOLEAN_PRIM_TYPE_REF = TYPE_FACTORY.BOOLEAN_PRIMITIVE;

    /**
     * The simple name of the {@link Object#equals(Object)} method.
     */
    private static final String EQUALS_METHOD_SIMPLE_NAME = "equals";

    /**
     * The {@link CtExpression}s returned by this method are used in
     * {@link #findFieldsComparedInEqualsMethodInternal(CtTypeReference, Set)} to filter out {@link AccessibleField}s which
     * are not compared in the {@code equalsMethod}.
     *
     * @param equalsMethod
     *          The equals method which should be analyzed, not {@code null}.
     *
     * @return
     *          The {@link CtExpression}s which are compared in the given {@code equalsMethod}.
     */
    protected abstract Set<CtExpression<?>> findExpressionsComparedInEqualsMethod(CtMethod<Boolean> equalsMethod);

    /**
     *
      * @param clazzRef
     *          The type reference to check, not {@code null}. Must be a real sub-class of {@link Object}.
     *
     * @return
     *          {@code true}, if the {@link Object#equals(Object)} method of given {@code clazz} is overridden
     *          in that class. {@code false} is returned otherwise.
     */
    @Override
    public boolean overridesEquals(CtTypeReference<?> clazzRef) {
        return this.getEqualsMethod(clazzRef) != null;
    }

    /**
     * This method does not check if any invocation of the equals method is actually reachable.
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to check, not {@code null}. The {@link #overridesEquals(CtTypeReference)}
     *          method must return {@code true} for the given {@code clazz}.
     *
     * @return
     *          {@code true}, if the {@link #equals(Object)} method of the super class is invoked and its
     *          result is either directly returned with a {@code return} statement or is used inside another
     *          expression or statement. {@code false} is returned otherwise.
     */
    @Override
    protected boolean callsSuperInternal(CtTypeReference<?> clazzRef) {
        var equalsMethod = getEqualsMethod(clazzRef);
        var superEqualsMethodInvocations = getSuperEqualsMethodInvocationsIn(equalsMethod);

        if(superEqualsMethodInvocations.isEmpty()) {
            return false;
        }

        var localVariables = equalsMethod.getElements(new TypeFilter<>(CtLocalVariable.class));

        var assignmentWithSuperEqualsInvocationRhsPresent = localVariables.stream()
                .map(CtLocalVariable::getAssignment)
                .anyMatch(superEqualsMethodInvocations::contains);

        if(assignmentWithSuperEqualsInvocationRhsPresent) {
            log.debug("Result of super.equals method invocation in equals method of '{}' is used as the " +
                    "right hand side in an assignment!", clazzRef.getQualifiedName());
            return true;
        }

        var returnStatements = equalsMethod.getElements(new TypeFilter<>(CtReturn.class));

        var returnExpressionOfReturnStatement = returnStatements.stream()
                .map(CtReturn::getReturnedExpression)
                .filter(Objects::nonNull)
                .anyMatch(superEqualsMethodInvocations::contains);

        if(returnExpressionOfReturnStatement) {
            log.debug("Result of super.equals method invocation in equals method of '{}' is returned!",
                    clazzRef.getQualifiedName());
            return true;
        }

        return false;
    }

    /**
     *
     * @param clazzRefOverridingEquals
     *          The reference of the type which overrides the equals method declared in {@link Object#equals(Object)},
     *          not {@code null}. The {@link #overridesEquals(CtTypeReference)} method must return {@code true} for the
     *          this type reference.
     *
     * @param accessibleFields
     *          A set containing the <i>accessible</i> fields which are declared in the {@code clazzRefOverridingEquals} itself
     *          and all superclasses of the {@code clazz}, not {@code null}. The fields are <i>accessible</i>
     *          from the given {@code clazz}.
     *
     * @return
     *          A set containing the accessible fields of which at least on {@link AccessibleField#getAccessGrantingElements()
     *          access granting element} was compared in the equals method of the given {@code clazzOverridingEquals}.
     *          {@link AccessibleField}s not matching that criteria are omitted.
     */
    @Override
    protected Set<AccessibleField<?>> findFieldsComparedInEqualsMethodInternal(CtTypeReference<?> clazzRefOverridingEquals, Set<AccessibleField<?>> accessibleFields) {
        var comparedExpressions = findExpressionsComparedInEqualsMethod(getEqualsMethod(clazzRefOverridingEquals));

        Set<CtExecutable<?>> comparedExecutables = comparedExpressions.stream()
                .filter(arg -> arg instanceof CtInvocation)
                .map(arg -> (CtInvocation<?>) arg)
                .map(CtInvocation::getExecutable)
                .map(CtExecutableReference::getDeclaration)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<CtField<?>> comparedFields = comparedExpressions.stream()
                .filter(arg -> arg instanceof CtFieldRead)
                .map(arg -> (CtFieldRead<?>) arg)
                .map(CtFieldRead::getVariable)
                .map(CtFieldReference::getFieldDeclaration)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var typedElementsSet = new HashSet<CtTypedElement<?>>(comparedExecutables.size() + comparedFields.size());
        typedElementsSet.addAll(comparedExecutables);
        typedElementsSet.addAll(comparedFields);

        return accessibleFields.stream()
                .filter(accessibleField -> {
                    for(var accessGrantingElement : accessibleField.getAccessGrantingElements()) {
                        if(typedElementsSet.contains(accessGrantingElement)) {
                            log.debug("Accessible field {} compared in equals method of '{}' through typed element '{}'",
                                    accessibleField, clazzRefOverridingEquals.getQualifiedName(), accessGrantingElement);

                            return true;
                        }
                    }

                    log.debug("Accessible field {} not compared in equals method of '{}'!", accessibleField,
                            clazzRefOverridingEquals.getQualifiedName());

                    return false;
                })
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to get the {@link CtMethod} representation of the {@link Object#equals(Object)}
     *          method from, not {@code null}.
     *
     * @return
     *          The {@link CtMethod} representation of the {@link Object#equals(Object)} method or {@code null}
     *          if the equals method is not overridden in the given {@code clazzRef}.
     */
    protected CtMethod<Boolean> getEqualsMethod(CtTypeReference<?> clazzRef) {
        var clazzType = clazzRef.getTypeDeclaration();

        if(clazzType == null) {
            log.warn("CtType instance of '{}' not found!", clazzRef.getQualifiedName());
            return null;
        }

        return clazzType.getMethod(BOOLEAN_PRIM_TYPE_REF, EQUALS_METHOD_SIMPLE_NAME, OBJECT_TYPE_REF);
    }

    /**
     *
     * @param equalsMethod
     *          The {@link CtMethod} representation of the {@link Object#equals(Object)} method to get
     *          the invocations of the {@link Object#equals(Object)} method in, not {@code null}.
     *
     * @return
     *          A list containing all invocations of the {@link Object#equals(Object)} method inside
     *          the given {@code equalsMethod} whose target is a {@link CtSuperAccess}.
     */
    private List<CtInvocation<Boolean>> getSuperEqualsMethodInvocationsIn(CtMethod<Boolean> equalsMethod) {
        return equalsMethod.getElements(new SuperEqualsMethodInvocationFilter());
    }

    /**
     * {@link TypeFilter} which matches {@link CtInvocation}s of the {@link Object#equals(Object)} method
     * on the super class.
     */
    private static class SuperEqualsMethodInvocationFilter extends TypeFilter<CtInvocation<Boolean>> {

        public SuperEqualsMethodInvocationFilter() {
            super(CtInvocation.class);
        }

        /**
         *
         * @param invocation
         *          The invocation which should be matched, not {@code null}.
         *
         * @return
         *          {@code true}, when the given invocation's target is a {@link CtSuperAccess} and the executable's
         *          simple name is equal to {@value EQUALS_METHOD_SIMPLE_NAME}, is not static, returns a primitive
         *          boolean and requires a single argument of type {@link Object}.
         */
        @Override
        public boolean matches(CtInvocation<Boolean> invocation) {
            if(!super.matches(invocation)) {
                return false;
            }

            var executableRef = invocation.getExecutable();

            return (invocation.getTarget() instanceof CtSuperAccess)
                    && EQUALS_METHOD_SIMPLE_NAME.equals(executableRef.getSimpleName())
                    && !executableRef.isStatic()
                    && BOOLEAN_PRIM_TYPE_REF.equals(executableRef.getType())
                    && List.of(OBJECT_TYPE_REF).equals(executableRef.getParameters());
        }

    }

}
