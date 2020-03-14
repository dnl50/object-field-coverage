package de.adesso.objectfieldcoverage.core.junit;

import de.adesso.objectfieldcoverage.api.TestMethodFinder;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A abstract {@link TestMethodFinder} implementation whose {@link #findTestMethods(CtClass)} method
 * is filters for JUnit test methods, including those methods that are declared in static inner-classes and
 * super-classes and super-interfaces. See section 2.2 of the JUnit Jupiter user guide for more details (the
 * same rules apply to JUnit 4).
 */
public abstract class AbstractJUnitTestMethodFinder implements TestMethodFinder {

    /**
     * Combines the methods directly declared on the given {@code testClazz}, any super-class/super-interface
     * and non-abstract static inner-classes into a single stream and uses the test method predicate returned
     * by the implementation to filter out methods which are no test methods.
     *
     * @param testClazz
     *          The {@link CtClass} of which the JUnit test methods should be returned, not {@code null}.
     *
     * @return
     *          A list of all JUnit test methods which are either declared directly on the given
     *          {@code testClazz}, a super-class/super-interface or a non-abstract static inner-class. Returns an
     *          empty list in case the given {@code testClazz} is declared <i>abstract</i>.
     */
    @Override
    public List<CtMethod<?>> findTestMethods(CtClass<?> testClazz) {
        Objects.requireNonNull(testClazz, "testClazz cannot be null!");

        if(testClazz.isAbstract()) {
            return List.of();
        }

        var innerClassMethodStream = testClazz.getElements(new TypeFilter<CtClass<?>>(CtClass.class)).stream()
                .filter(Predicate.not(CtClass::isAbstract))
                .filter(CtClass::isStatic)
                .map(CtClass::getAllMethods)
                .flatMap(Set::stream)
                .distinct();
        var directDeclaredMethodStream = testClazz.getAllMethods().stream();

        return Stream.concat(innerClassMethodStream, directDeclaredMethodStream)
                .filter(testMethodPredicate())
                .collect(Collectors.toList());
    }

    /**
     *
     * @return
     *          A {@link Predicate} that matches a given {@link CtMethod} in case it is
     *          a JUnit test method. Cannot be {@code null}.
     */
    protected abstract Predicate<CtMethod<?>> testMethodPredicate();

}
