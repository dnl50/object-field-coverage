package de.adesso.objectfieldcoverage.core.processor;

import lombok.extern.slf4j.Slf4j;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.reference.CtArrayTypeReferenceImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Slf4j
public class TargetMethodFinder {

    /**
     * The simplified regex for a Java <i>Identifier</i> as specified by §3.8 of the Java Language specification. Does match
     * identifiers that contain <i>Keywords</i> (§3.9), <i>BooleanLiterals</i> (§3.10.3) and <i>NullLiterals</i>
     * (§3.10.7) though because a valid Java compiler prevents these cases anyway.
     */
    private static final String IDENTIFIER_REGEX = "(([a-zA-Z]|\\$|_)+([a-zA-Z0-9]|\\$|_)*)";

    /**
     * The regex for a Java <i>PackageOrTypeName</i> as specified by §6.5 of the Java Language Specification. References
     * the simplified {@link #IDENTIFIER_REGEX Identifer} regex.
     */
    private static final String JAVA_PACKAGE_OR_TYPE_NAME_REGEX = "(" + IDENTIFIER_REGEX + "\\.)*" + IDENTIFIER_REGEX;

    /**
     * The regex for a Java <i>UnannPrimitiveType</i> as specified by §8.3 of the Java Language Specification.
     */
    private static final String UNANN_PRIMITIVE_TYPE_REGEX = "(boolean|byte|short|int|long|char|float|double)";

    /**
     * The simplified regex for a Java <i>UnannClassType</i> as specified by §8.3 of the Java Language Specification
     * omitting type arguments for generics and annotations. References the simplified regex of a
     * {@link #JAVA_PACKAGE_OR_TYPE_NAME_REGEX PackageOrTypeName}.
     */
    private static final String UNANN_CLASS_TYPE_REGEX = JAVA_PACKAGE_OR_TYPE_NAME_REGEX;

    /**
     * The regex for a Java <i>UnannInterfaceType</i> as specified by §8.3 of the Java Language Specification.
     * References the simplified regex for a {@link #UNANN_CLASS_TYPE_REGEX UnannClassType}.
     */
    private static final String UNANN_INTERFACE_TYPE_REGEX = UNANN_CLASS_TYPE_REGEX;

    /**
     * The regex for a Java <i>UnannClassOrInterfaceType</i> as specified by §8.3 of the Java Language Specification.
     * References the simplified regex for a {@link #UNANN_CLASS_TYPE_REGEX UnannClassType} and a
     * {@link #UNANN_INTERFACE_TYPE_REGEX UnannInterfaceType}.
     */
    private static final String UNANN_CLASS_OR_INTERFACE_TYPE = "(" + UNANN_CLASS_TYPE_REGEX + "|" + UNANN_INTERFACE_TYPE_REGEX + ")";

    /**
     * The simplified regex for a Java <i>UnannArrayType</i> as specified by §8.3 of the Java Language Specification.
     */
    private static final String UNANN_ARRAY_TYPE_REGEX = "(" + UNANN_PRIMITIVE_TYPE_REGEX + "|" + UNANN_CLASS_OR_INTERFACE_TYPE + ")" + "(\\[\\])+";

    /**
     * The simplified regex for a Java <i>UnannReferenceType</i> as specified by §8.3 of the Java Language Specification.
     * Omits the <i>UnannTypeVariable</i> since it is included by the simplified representation of a
     * {@link #UNANN_CLASS_OR_INTERFACE_TYPE UnannClassOrInterfaceType}.
     */
    private static final String UNANN_REFERENCE_TYPE_REGEX = "(" + UNANN_CLASS_OR_INTERFACE_TYPE + "|" + UNANN_ARRAY_TYPE_REGEX + ")";

    /**
     * The regex for a Java <i>UnannType</i> as specified by §8.3 of the Java Language Specification. References the simplified
     * regex of a {@link #UNANN_PRIMITIVE_TYPE_REGEX UnannPrimitiveType} and a {@link #UNANN_REFERENCE_TYPE_REGEX UnannReferenceTypeRegex}
     */
    private static final String UNANN_TYPE_REGEX = "(" + UNANN_PRIMITIVE_TYPE_REGEX + "|" + UNANN_REFERENCE_TYPE_REGEX + ")";

    /**
     * The simplified regex for a Java <i>FormalParameter</i> as specified by §8.4.1 of the Java Language Specification.
     */
    private static final String METHOD_FORMAL_PARAMETER_REGEX = UNANN_TYPE_REGEX;

    /**
     * The regex for a Java <i>FormalParameterList</i> as specified by §8.4.1 of the Java Language Specification. References
     * the simplified regex for a {@link #METHOD_FORMAL_PARAMETER_REGEX FormalParameter}.
     */
    private static final String METHOD_FORMAL_PARAMETER_LIST_REGEX = "((" + METHOD_FORMAL_PARAMETER_REGEX + ",)*" + METHOD_FORMAL_PARAMETER_REGEX + ")";

    /**
     * The complete method identifier regex to validate a given method identifier with. Accepts JavaDoc-like method
     * {@code package.class#memberMethod} strings.
     */
    private static final String METHOD_IDENTIFIER_REGEX = JAVA_PACKAGE_OR_TYPE_NAME_REGEX + "#" + IDENTIFIER_REGEX + "\\(" + METHOD_FORMAL_PARAMETER_LIST_REGEX + "?\\)";

    /**
     * The predicate used to test a given method identifier. Build using the {@link #METHOD_IDENTIFIER_REGEX}.
     */
    private static final Predicate<String> METHOD_IDENTIFIER_MATCH_PREDICATE = Pattern.compile(METHOD_IDENTIFIER_REGEX).asMatchPredicate();

    public Optional<CtMethod<?>> findTargetMethod(String methodIdentifier, CtModel model) {
        Objects.requireNonNull(methodIdentifier, "methodIdentifier cannot be null!");
        Objects.requireNonNull(model, "model cannot be null!");

        if(!matchesMethodIdentifierPattern(methodIdentifier)) {
            throw new IllegalArgumentException(String.format("Given method identifier '%s' is not a valid identifier!",
                    methodIdentifier));
        }

        return findTargetClassInModel(methodIdentifier, model)
                .flatMap(targetClass -> findTargetMethodOnTargetClass(methodIdentifier, targetClass));
    }

    /**
     * Uses the {@link #METHOD_IDENTIFIER_MATCH_PREDICATE} to test a given {@code methodIdentifier}.
     *
     * @param methodIdentifier
     *          The method identifier to verify, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code methodIdentifier} matches the
     *          {@link #METHOD_IDENTIFIER_REGEX}. {@code false} is returned otherwise.
     */
    private boolean matchesMethodIdentifierPattern(String methodIdentifier) {
        if(METHOD_IDENTIFIER_MATCH_PREDICATE.test(methodIdentifier)) {
            return true;
        }

        log.warn("Method identifier '{}' is not a valid identifier!", methodIdentifier);
        return false;
    }

    /**
     *
     * @param methodIdentifier
     *          The method identifier to extract the qualified class name from, not {@code null}. Must
     *          be a valid method identifier.
     *
     * @param model
     *          The Spoon model to find the class in, not {@code null}.
     *
     * @return
     *          An optional containing the class identified by its qualified name extracted from
     *          the given {@code methodIdentifier} or an empty optional if no such class is
     *          present in the given {@code model}.
     */
    private Optional<CtClass<?>> findTargetClassInModel(String methodIdentifier, CtModel model) {
        var qualifiedClassName = extractClassName(methodIdentifier);

        return model.getElements(new TypeFilter<>(CtClass.class)).stream()
                .filter(ctClass -> ctClass.getQualifiedName().equals(qualifiedClassName))
                .findFirst()
                .map(ctClass -> (CtClass<?>) ctClass);
    }

    /**
     *
     * @param methodIdentifier
     *          The method identifier to extract the target method name and parameter types from, not {@code null}.
     *          Must be a valid method identifier.
     *
     * @param targetClass
     *          The target class to find the method on, not {@code null}.
     *
     * @return
     *          An optional containing the target method in case a method on the given {@code targetClass}
     *          is present which has an equal name and the same parameter types or an empty optional
     *          if no such method is present.
     */
    private Optional<CtMethod<?>> findTargetMethodOnTargetClass(String methodIdentifier, CtClass<?> targetClass) {
        var methodName = extractMethodName(methodIdentifier);
        var formalParameters = extractFormalParameters(methodIdentifier);

        //TODO: use parameters as well
        return Optional.ofNullable(targetClass.getMethod(methodName));
    }

    /**
     * Extracts the formal method parameters from a given method identifier. The identifier must match
     * the {@link #METHOD_IDENTIFIER_REGEX}.
     *
     * @param methodIdentifier
     *          The method identifier to get the formal method parameters from, not {@code null}. Must
     *          be a valid method identifier.
     *
     * @return
     *          A list containing all formal method parameters.
     */
    private List<String> extractFormalParameters(String methodIdentifier) {
        var formalParameterListWithClosingBracket = methodIdentifier.split("#")[1]
                .split("\\(")[1];

        var formalParameterList = formalParameterListWithClosingBracket.substring(0,
                formalParameterListWithClosingBracket.length() - 1);

        return Arrays.asList(formalParameterList.split(","));
    }

    /**
     *
     * @param formalMethodParameter
     *          A formal method parameter as returned by {@link #extractFormalParameters(String)}, not
     *          {@code null}.
     *
     * @return
     *          {@code true}, iff the given {@code formalMethodParameter} ends with array dimension
     *          brackets (<i>[]</i>).
     */
    private boolean isArrayType(String formalMethodParameter) {
        return formalMethodParameter.endsWith("[]");
    }

    private CtArrayTypeReference<?> buildArrayTypeReference(CtModel model) {
        return null;
    }

    private CtTypeReference<?> buildTypeReference(CtModel model) {
        return null;
    }

    /**
     * Extracts the method name from a given method identifier. The identifier must match
     * the {@link #METHOD_IDENTIFIER_REGEX}.
     *
     * @param methodIdentifier
     *          The method identifier to get the method name from, not {@code null}. Must
     *          be a valid method identifier.
     *
     * @return
     *          The method name.
     */
    private String extractMethodName(String methodIdentifier) {
        return methodIdentifier.split("#")[1]
                .split("\\(")[0];
    }

    /**
     * Extracts the method name from a given method identifier. The identifier must match
     * the {@link #METHOD_IDENTIFIER_REGEX}.
     *
     * @param methodIdentifier
     *          The method identifier to get the class name from, not {@code null}. Must
     *          be a valid method identifier.
     *
     * @return
     *          The class name.
     */
    private String extractClassName(String methodIdentifier) {
        return methodIdentifier.split("#")[0];
    }

}
