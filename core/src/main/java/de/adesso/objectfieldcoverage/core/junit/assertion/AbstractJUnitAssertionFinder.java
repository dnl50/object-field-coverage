package de.adesso.objectfieldcoverage.core.junit.assertion;

import de.adesso.objectfieldcoverage.api.AssertionFinder;
import de.adesso.objectfieldcoverage.api.assertion.AbstractAssertion;
import de.adesso.objectfieldcoverage.api.filter.QualifiedNameMethodInvocationTypeFilter;
import de.adesso.objectfieldcoverage.core.junit.assertion.handler.JunitAssertionInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AbstractJUnitAssertionFinder implements AssertionFinder {

    /**
     * The fully qualified name of JUnit 5's Assertions utility class.
     */
    private static final String JUNIT_5_ASSERTIONS_QUALIFIED_NAME = "org.junit.jupiter.api.Assertions";

    /**
     * The fully qualified name of JUnit 4's Assert utility class.
     */
    private static final String JUNIT_4_ASSERT_QUALIFIED_NAME = "org.junit.Assert";

    private final List<JunitAssertionInvocationHandler> junitAssertionInvocationHandlers;

    /**
     * No-arg constructor as required by the {@link AssertionFinder} interface.
     */
    public AbstractJUnitAssertionFinder() {
        this.junitAssertionInvocationHandlers = List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractAssertion<?>> findAssertions(CtMethod<?> testMethod, List<CtMethod<?>> invokedHelperMethods) {
        var testAndHelperMethods = new ArrayList<>(invokedHelperMethods);
        testAndHelperMethods.add(testMethod);
        var staticJunitAssertInvocationPairs = findStaticJunitAssertInvocations(testAndHelperMethods);

        var allAssertions = new ArrayList<AbstractAssertion<?>>();
        for(var invocationPair : staticJunitAssertInvocationPairs) {
            var assertions = junitAssertionInvocationHandlers.stream()
                    .filter(handler -> handler.supports(invocationPair.getLeft(), invocationPair.getRight()))
                    .map(handler -> handler.getAssertion(invocationPair.getLeft(), testMethod, invocationPair.getRight()))
                    .collect(Collectors.toList());

            allAssertions.addAll(assertions);
        }

        return allAssertions;
    }

    /**
     *
     * @param methods
     *          The {@link CtMethod}s to find invocations of static methods declared in {@value JUNIT_5_ASSERTIONS_QUALIFIED_NAME}
     *          and {@value JUNIT_4_ASSERT_QUALIFIED_NAME}, not {@code null}.
     *
     * @return
     *          A set containing pairs of invocations of static methods declared in {@value JUNIT_5_ASSERTIONS_QUALIFIED_NAME}
     *          and {@value JUNIT_4_ASSERT_QUALIFIED_NAME} and the their corresponding JUnit version.
     */
    private Set<Pair<CtInvocation<?>, JUnitVersion>> findStaticJunitAssertInvocations(List<CtMethod<?>> methods) {
        var junit4assertInvocationFilter = new QualifiedNameMethodInvocationTypeFilter(JUNIT_4_ASSERT_QUALIFIED_NAME);
        var junit5assertionInvocationFilter = new QualifiedNameMethodInvocationTypeFilter(JUNIT_5_ASSERTIONS_QUALIFIED_NAME);

        Stream<Pair<CtInvocation<?>, JUnitVersion>> junit4assertInvocations = methods.stream()
                .map(method -> method.getElements(junit4assertInvocationFilter))
                .flatMap(Collection::stream)
                .map(invocation -> Pair.of(invocation, JUnitVersion.FOUR));

        Stream<Pair<CtInvocation<?>, JUnitVersion>> junit5assertionInvocations = methods.stream()
                .map(method -> method.getElements(junit5assertionInvocationFilter))
                .flatMap(Collection::stream)
                .map(invocation -> Pair.of(invocation, JUnitVersion.FIVE));

        var staticInvocationPairs = Stream.concat(junit4assertInvocations, junit5assertionInvocations)
                .filter(invocationVersionPair -> invocationVersionPair.getLeft().getExecutable().isStatic())
                .collect(Collectors.toSet());

        if(log.isDebugEnabled()) {
            log.debug("Methods {} contain {} invocations of static JUnit assert methods!",
                    methods.stream().map(CtExecutable::getSignature).collect(Collectors.toList()),
                    staticInvocationPairs.size());
        }

        return staticInvocationPairs;
    }

}
