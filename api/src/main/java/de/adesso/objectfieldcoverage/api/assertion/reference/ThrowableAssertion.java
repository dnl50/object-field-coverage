package de.adesso.objectfieldcoverage.api.assertion.reference;

import de.adesso.objectfieldcoverage.api.evaluation.AssertionEvaluationInformation;
import de.adesso.objectfieldcoverage.api.evaluation.graph.Path;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtMethod;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO: Test

/**
 * {@link ReferenceTypeAssertion} implementation specifically for assertions which cover throwables
 * thrown by an expression. The asserted expression must be the expression which actually throws the exception.
 *
 * @param <T>
 *          The return type of the asserted expression.
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ThrowableAssertion<T> extends ReferenceTypeAssertion<T> {

    /**
     * The simple name of the {@code type} pseudo field of a {@link Throwable} to check whether the
     * type of the thrown throwable is covered.
     */
    public static final String TYPE_PSEUDO_FIELD_NAME = "type";

    /**
     * The simple name of the {@code message} pseudo field of a {@link Throwable} to check whether the
     * {@link Throwable#getMessage() message} of the thrown throwable is covered.
     */
    public static final String MESSAGE_PSEUDO_FIELD_NAME= "message";

    /**
     * The simple name of the {@code cause} pseudo field of a {@link Throwable} to check whether the
     * {@link Throwable#getCause() cause} of the thrown throwable is covered.
     */
    public static final String CAUSE_PSEUDO_FIELD_NAME = "cause";

    /**
     * A flag indicating whether the {@code type} pseudo-field is covered.
     */
    private boolean coversType;

    /**
     * A flag indicating whether the {@code message} pseudo-field is covered.
     */
    private boolean coversMessage;

    /**
     * A flag indicating whether the {@code cause} pseudo-field is covered.
     */
    private boolean coversCause;

    public ThrowableAssertion(CtExpression<T> assertedExpression, CtMethod<?> originTestMethod) {
        super(assertedExpression, originTestMethod);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     *          An <b>unmodifiable</b> set containing the {@link Path}s to the pseudo fields of a throwable which
     *          are covered by {@code this} assertion.
     *
     * @throws IllegalStateException
     *          When the given {@code evaluationInformation} instance does not contain the expected pseudo fields
     *          for a throwable.
     */
    @Override
    public Set<Path> getCoveredPaths(AssertionEvaluationInformation evaluationInformation) {
        var allPaths = evaluationInformation.getAccessibleFieldsGraph()
                .getTransitiveReachabilityPaths();

        var simpleFieldNameToPathMap = allPaths.stream()
                .collect(Collectors.toMap(
                        path -> path.getLast().get().getAccessibleField().getActualField().getSimpleName(),
                        Function.identity()
                ));

        var coveredPaths = new HashSet<Path>(3);

        if(coversType) {
            coveredPaths.add(getPathEndingWithFieldFromMap(TYPE_PSEUDO_FIELD_NAME, simpleFieldNameToPathMap));
        }

        if(coversMessage) {
            coveredPaths.add(getPathEndingWithFieldFromMap(MESSAGE_PSEUDO_FIELD_NAME, simpleFieldNameToPathMap));
        }

        if(coversCause) {
            coveredPaths.add(getPathEndingWithFieldFromMap(CAUSE_PSEUDO_FIELD_NAME, simpleFieldNameToPathMap));
        }

        return coveredPaths;
    }

    /**
     *
     * @return
     *          {@code true}
     */
    @Override
    public boolean expressionRaisesThrowable() {
        return true;
    }

    /**
     *
     * @param simpleFieldName
     *          The name of the simple field to find a path for, not {@code null}.
     *
     * @param map
     *          A map mapping the simple name of the last node to a path which ends with a simple
     *          field with the same simple name, not {@code null}.
     *
     * @return
     *          The
     *
     * @throws IllegalStateException
     *          When the given map does not contain a mapping for the given field name.
     */
    private Path getPathEndingWithFieldFromMap(String simpleFieldName, Map<String, Path> map) {
        var pathOfField = map.get(simpleFieldName);

        if(pathOfField == null) {
            throw new IllegalStateException(String.format("Path ending with field '%s' not present!", simpleFieldName));
        }

        log.debug("ThrowableAssertion of '{}' covers '{}' field!", assertedExpression, simpleFieldName);

        return pathOfField;
    }

}
