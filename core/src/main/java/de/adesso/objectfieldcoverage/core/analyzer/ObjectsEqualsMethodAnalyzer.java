package de.adesso.objectfieldcoverage.core.analyzer;

import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link CtMethodEqualsMethodAnalyzer} implementation for generated/handwritten {@link Object#equals(Object)}
 * method implementations which use the {@link java.util.Objects#equals(Object, Object)} helper method internally.
 */
@Slf4j
public class ObjectsEqualsMethodAnalyzer extends CtMethodEqualsMethodAnalyzer {

    /**
     *
     * @param equalsMethod
     *          The equals method which should be analyzed, not {@code null}.
     *
     * @return
     *          A pair of the {@link CtInvocation}s and {@link CtFieldRead}s which are compared
     *          in the {@link Objects#equals(Object, Object)} method invocations which are made inside
     *          the given {@code equalsMethod}. Only the first argument of each invocation is taken into account.
     */
    @Override
    protected Set<CtExpression<?>> findExpressionsComparedInEqualsMethod(CtMethod<Boolean> equalsMethod) {
        var objectsEqualsInvocations = equalsMethod.getElements(new ObjectsEqualsInvocationFilter());

        if(objectsEqualsInvocations.isEmpty()) {
            return Set.of();
        }

        Set<CtExpression<?>> objectsEqualsInvocationArgs = objectsEqualsInvocations.stream()
                .map(CtInvocation::getArguments)
                .map(argumentLists -> argumentLists.get(0))
                .collect(Collectors.toSet());

        log.info("Equals method of '{}' contains {} Objects#equals(Object, Object) invocations!",
                equalsMethod.getDeclaringType().getQualifiedName(), objectsEqualsInvocations.size());

        return objectsEqualsInvocationArgs;
    }

    /**
     * A {@link TypeFilter} filtering for {@link CtInvocation}s of the {@link Objects#equals(Object, Object)}
     * method.
     */
    private static class ObjectsEqualsInvocationFilter extends TypeFilter<CtInvocation<Boolean>> {

        /**
         * The {@link TypeFactory} used to get a {@link CtTypeReference} for the required types for.
         */
        private static final TypeFactory TYPE_FACTORY = new TypeFactory();

        /**
         * A {@link CtTypeReference} to the {@link Objects} class which contains the {@link Objects#equals(Object, Object)}
         * method.
         */
        private static final CtTypeReference<Objects> OBJECTS_TYPE_REF = TYPE_FACTORY.createReference(Objects.class);

        /**
         * A the simple name of the static {@link Objects#equals(Object, Object)} method.
         */
        private static final String OBJECTS_EQUALS_SIMPLE_NAME = "equals";

        public ObjectsEqualsInvocationFilter() {
            super(CtInvocation.class);
        }

        /**
         *
         * @param invocation
         *          The invocation which should be matched, not {@code null}.
         *
         * @return
         *          {@code true}, if the invocation's executable is declared in the {@link Objects} class,
         *          its simple name is equal to {@value OBJECTS_EQUALS_SIMPLE_NAME}, is static and
         *          expects two parameters of type {@link Object}.
         */
        @Override
        public boolean matches(CtInvocation<Boolean> invocation) {
            if(!super.matches(invocation)) {
                return false;
            }

            var executableRef = invocation.getExecutable();

            return OBJECTS_TYPE_REF.equals(executableRef.getDeclaringType())
                    && OBJECTS_EQUALS_SIMPLE_NAME.equals(executableRef.getSimpleName())
                    && executableRef.isStatic()
                    && executableRef.getParameters().size() == 2;
        }

    }

}
