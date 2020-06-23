package de.adesso.objectfieldcoverage.core.processor.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link Predicate} implementation to check if a given {@link CtExecutable} is invoked in a given
 * set of {@link CtMethod}s.
 */
@Slf4j
@RequiredArgsConstructor
public class InvokedExecutableFilter implements Predicate<CtExecutable<?>> {

    /**
     * The set containing the methods to find an invocation of a given executable in.
     */
    private final Set<CtMethod<?>> methodsToFindInvocationsIn;

    /**
     * A set containing all executable references which are invoked inside the given methods.
     */
    private Set<CtExecutableReference<?>> invokedExecutables;

    /**
     *
     * @param executable
     *          The executable to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code executable} is invoked in at least one of the
     *          registered methods. {@code false} is returned otherwise.
     */
    @Override
    public boolean test(CtExecutable<?> executable) {
        if(invokedExecutables == null) {
            initInvokedExecutables();
        }

        return invokedExecutables.contains(executable.getReference());
    }

    /**
     * Sets the {@link #invokedExecutables} to a set containing all executables which
     * are invoked inside the given methods.
     */
    private void initInvokedExecutables() {
        this.invokedExecutables = methodsToFindInvocationsIn.stream()
                .map(method -> method.getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class)))
                .flatMap(Collection::stream)
                .map(CtAbstractInvocation::getExecutable)
                .collect(Collectors.toSet());

        log.debug("Found a total of {} invocations in the following methods: {}", invokedExecutables.size(),
                methodsToFindInvocationsIn);
    }

}
