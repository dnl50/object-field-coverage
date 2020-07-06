package de.adesso.objectfieldcoverage.core.finder.executable;

import de.adesso.objectfieldcoverage.annotation.TestTarget;
import de.adesso.objectfieldcoverage.annotation.TestTargets;
import de.adesso.objectfieldcoverage.api.Order;
import de.adesso.objectfieldcoverage.api.TargetExecutableFinder;
import de.adesso.objectfieldcoverage.api.assertion.primitive.PrimitiveTypeUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.ConstructorFactory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@link TargetExecutableFinder} implementation using the <i>method identifier</i> specified in a {@link TestTarget}
 * and {@link TestTargets} annotation to find the targeted executables.
 * <br/>
 * Annotated with the {@link Order} annotation with the lowest possible ordering value so other
 * {@link TargetExecutableFinder} implementations may generate the {@link TestTarget} annotation before the
 * invocation of {@code this} one.
 *
 * @see TestTarget
 * @see TestTargets
 */
@Slf4j
@Order(Order.LOWEST)
@NoArgsConstructor
public class AnnotationBasedTargetExecutableFinder implements TargetExecutableFinder {

    /**
     * The prefix of a fully qualified type in the {@code java} package.
     */
    private static final String JAVA_FULLY_QUALIFIED_PREFIX = "java.";

    /**
     * The fully qualified package name of the {@code java.lang} package.
     */
    private static final String JAVA_LANG_PACKAGE_QUALIFIED_NAME = "java.lang";

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
    private static final String METHOD_FORMAL_PARAMETER_LIST_REGEX = "(" + METHOD_FORMAL_PARAMETER_REGEX + "(," + METHOD_FORMAL_PARAMETER_REGEX + ")*)";

    /**
     * The complete method identifier regex to validate a given method identifier with. Accepts JavaDoc-like method
     * {@code package.class#memberMethod} strings.
     */
    private static final String METHOD_IDENTIFIER_REGEX = JAVA_PACKAGE_OR_TYPE_NAME_REGEX + "#" + IDENTIFIER_REGEX + "\\(" + METHOD_FORMAL_PARAMETER_LIST_REGEX + "?\\)";

    /**
     * The predicate used to test a given method identifier. Built using the {@link #METHOD_IDENTIFIER_REGEX}.
     */
    private static final Predicate<String> METHOD_IDENTIFIER_MATCH_PREDICATE = Pattern.compile(METHOD_IDENTIFIER_REGEX).asMatchPredicate();

    /**
     * The predicate used to test if a given formal parameter is a primitive type parameter. Built using the {@link #UNANN_PRIMITIVE_TYPE_REGEX}.
     */
    private static final Predicate<String> UNANN_PRIMITIVE_TYPE_MATCH_PREDICATE = Pattern.compile(UNANN_PRIMITIVE_TYPE_REGEX).asMatchPredicate();

    /**
     * The type factory used internally to create the type references to identify the target methods.
     */
    private static final TypeFactory typeFactory = new TypeFactory();

    /**
     *
     * @param testMethod
     *          The test method for which the target executables should be found, not {@code null}.
     *
     * @param helperMethods
     *          All helper methods which are invoked inside the given {@code testMethod}. Not used by this
     *          implementation.
     *
     * @return
     *          A list containing all {@link CtExecutable}s which have been specified by the <i>method identifier</i>s
     *          of the {@link TestTarget} and {@link TestTargets} annotation. An empty set is returned if the
     *          given {@code testMethod} is not annotated with either annotation.
     */
    @Override
    public Set<CtExecutable<?>> findTargetExecutables(CtMethod<?> testMethod, List<CtMethod<?>> helperMethods) {
        Objects.requireNonNull(testMethod, "The test method cannot be null!");

        var testTargetAnnotation = testMethod.getAnnotation(TestTarget.class);
        var testTargetsAnnotation = testMethod.getAnnotation(TestTargets.class);

        if(testTargetAnnotation == null && testTargetsAnnotation == null) {
            log.info("Test method '{}' is neither annotated with @TestTarget nor @TestTargets!",
                    testMethod.getSignature());
            return Set.of();
        }

        var methodIdentifiers = new HashSet<String>();

        if(testTargetAnnotation != null) {
            methodIdentifiers.add(testTargetAnnotation.value());
        }

        if(testTargetsAnnotation != null) {
            var specifiedTestTargetAnnotations = testTargetsAnnotation.value();

            if(specifiedTestTargetAnnotations.length == 0) {
                log.warn("Test method '{}' is annotated with @TestTargets, but no @TestTarget values are specified!",
                        testMethod.getSignature());
            } else {
                for(var specifiedTestTargetAnnotation : specifiedTestTargetAnnotations) {
                    methodIdentifiers.add(specifiedTestTargetAnnotation.value());
                }
            }
        }

        var underlyingModel = testMethod.getFactory().getModel();

        return methodIdentifiers.stream()
                .map(methodIdentifier -> findTargetExecutable(methodIdentifier, underlyingModel))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    /**
     * <b>Note:</b> Constructors are viewed as a special kind of method.
     *
     * @param methodIdentifier
     *          The method identifier by which the target method is identified, not {@code null}. Must be
     *          a valid method identifier. See {@link de.adesso.objectfieldcoverage.annotation.TestTarget}
     *          for a detailed explanation.
     *
     * @param model
     *          The model which contains the target method which should be retrieved, not {@code null}.
     *
     * @return
     *          An optional containing the target executable in case it is present in the given model or
     *          an empty optional in case the target type is not present or the target type does
     *          not have a method that matches the specified signature.
     *
     * @throws IllegalArgumentException
     *          When the given {@code methodIdentifier} is not valid according to
     *          {@link #matchesMethodIdentifierPattern(String)}.
     */
    private static Optional<CtExecutable<?>> findTargetExecutable(String methodIdentifier, CtModel model) {
        Objects.requireNonNull(methodIdentifier, "The method identifier cannot be null!");
        Objects.requireNonNull(model, "The model cannot be null!");

        var trimmedIdentifier = methodIdentifier.replaceAll("\\h*", "");

        if(!matchesMethodIdentifierPattern(trimmedIdentifier)) {
            log.warn("Method identifier '{}' is not a valid method identifier!", methodIdentifier);
            throw new IllegalArgumentException(String.format("Method identifier '%s' is not a valid identifier!",
                    methodIdentifier));
        }

        return findTargetClassInModel(trimmedIdentifier, model)
                .flatMap(targetClass -> findTargetExecutableOnTargetClass(trimmedIdentifier, targetClass, model));
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
    private static boolean matchesMethodIdentifierPattern(String methodIdentifier) {
        return METHOD_IDENTIFIER_MATCH_PREDICATE.test(methodIdentifier);
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
    private static Optional<CtClass<?>> findTargetClassInModel(String methodIdentifier, CtModel model) {
        var qualifiedClassName = extractClassName(methodIdentifier);

        return model.getElements(new TypeFilter<CtClass<?>>(CtClass.class)).stream()
                .filter(ctClass -> ctClass.getQualifiedName().equals(qualifiedClassName))
                .findFirst();
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
     * @param model
     *          The model to get the type reference for the method parameters of, not {@code null}. Must contain
     *          a reference type for each parameter specified in the given {@code methodIdentifier}.
     *
     * @return
     *          An optional containing the target executable in case a method or constructor on the given
     *          {@code targetClass} is present which has an equal name and the same parameter types or an
     *          empty optional if no such method or constructor is present.
     */
    private static Optional<? extends CtExecutable<?>> findTargetExecutableOnTargetClass(String methodIdentifier, CtClass<?> targetClass, CtModel model) {
        var methodName = extractMethodName(methodIdentifier);
        var formalParameters = extractFormalParameters(methodIdentifier);

        var typeReferencesForParameters = formalParameters.stream()
                .map(formalParameter -> isArrayType(formalParameter)
                        ? buildArrayTypeReference(formalParameter, model)
                        : buildTypeReference(formalParameter, model))
                .toArray(CtTypeReference[]::new);

        if(isConstructorCandidate(methodName, targetClass)) {
            var constructorOptional = findConstructor(typeReferencesForParameters, targetClass);

            if(constructorOptional.isPresent()) {
                return constructorOptional;
            } else {
                log.warn("Target method name of method '{}' on type '{}' indicates that this method is a constructor " +
                        "but no constructor with matching parameters was found! Fallback to regular method!", methodName,
                        targetClass.getQualifiedName());
            }
        }

        return Optional.ofNullable(targetClass.getMethod(methodName, typeReferencesForParameters));
    }

    /**
     *
     * @param methodName
     *          The simple name of the method specified in the method identifier, not blank.
     *
     * @param targetClass
     *          The class which the method identifier references, not {@code null}.
     *
     * @return
     *          {@code true}, if the simple name of the given {@code targetClass} is equal to the
     *          given {@code methodName}. {@code false} is returned otherwise.
     */
    private static boolean isConstructorCandidate(String methodName, CtClass<?> targetClass) {
        var simpleClassName = targetClass.getSimpleName();
        return methodName.equals(simpleClassName);
    }

    /**
     * When the given {@code typeReferencesForParameters} array is empty, a default constructor is searched
     * on the given target class. If no default constructor is explicitly declared on the given
     * {@code targetClass} and no other constructor is present, a new default constructor will be generated.
     *
     * @param typeReferencesForParameters
     *          The {@link CtTypeReference}s for the parameters of the constructor, not {@code null}.
     *
     * @param targetClass
     *          The target class to find a constructor with the given type references for, not {@code null}.
     *
     * @param <T>
     *          The type of the class.
     *
     * @return
     *          An optional containing a constructor whose parameters match the given {@link CtTypeReference}s
     *          or an empty optional in case no such constructor is present.
     */
    private static <T> Optional<CtConstructor<T>> findConstructor(CtTypeReference<?>[] typeReferencesForParameters, CtClass<T> targetClass) {
        if(typeReferencesForParameters.length == 0 && targetClass.getConstructors().isEmpty()) {
            return Optional.of(createImplicitDefaultConstructor(targetClass));
        }

        return Optional.ofNullable(targetClass.getConstructor(typeReferencesForParameters));
    }

    /**
     * Creates a new default constructor on the given {@code targetClass} in case no other constructor
     * is present as defined by §8.8.9 of the Java Language Specification.
     *
     * @param targetClass
     *          The {@link CtClass} to create a default constructor for, not {@code null}.
     *
     * @param <T>
     *          The type of the target class.
     *
     * @throws IllegalStateException
     *          When the given {@code targetClass} already has at least one constructor declared.
     */
    private static <T> CtConstructor<T> createImplicitDefaultConstructor(CtClass<T> targetClass) {
        if(targetClass.getConstructors().isEmpty()) {
            var targetClassFactory = targetClass.getFactory();

            var implicitConstructor = new ConstructorFactory(targetClassFactory)
                    .createDefault(targetClass);
            implicitConstructor.setImplicit(true);

            return implicitConstructor;
        } else {
            log.warn("No default constructor can be generated for '{}', because other constructors are present!",
                    targetClass.getQualifiedName());
            throw new IllegalStateException("Cannot generate implicit default constructor! ");
        }
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
    private static List<String> extractFormalParameters(String methodIdentifier) {
        var formalParameterList = StringUtils.substringBetween(methodIdentifier, "(", ")");
        return Arrays.stream(formalParameterList.split(","))
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
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
    private static boolean isArrayType(String formalMethodParameter) {
        return formalMethodParameter.endsWith("[]");
    }

    /**
     *
     * @param formalMethodParameter
     *          The formal method parameter to build an array type reference from, not {@code null}. Must
     *          be a valid {@link #UNANN_ARRAY_TYPE_REGEX UnannArrayType}.
     *
     * @param model
     *          The model to get the type reference of, not {@code null}. Must contain
     *          a reference type for the given {@code formalMethodParameter}.
     *
     * @return
     *          The array type reference with its dimension set accordingly, not {@code null}.
     */
    private static CtArrayTypeReference<?> buildArrayTypeReference(String formalMethodParameter, CtModel model) {
        var arrayDimension = StringUtils.countMatches(formalMethodParameter, "[]");
        var formalParameterWithoutDimensions = StringUtils.substringBefore(formalMethodParameter, "[");

        var typeReference = buildTypeReference(formalParameterWithoutDimensions, model);
        return typeFactory.createArrayReference(typeReference, arrayDimension);
    }

    /**
     * When the given {@code formalMethodParameter} is not fully qualified (does not contain a dot) a
     * <i>java.lang.</i> and it is not a primitive type (e.g. <i>boolean</i>), a prefix is added, so classes
     * from the java.lang package don't need to be fully qualified.
     * <p/>
     * When the {@code formalMethodParameter} is located in the <i>java</i> package (after eventually prefixing it
     * when it was not fully qualified), a type reference is created using the actual <i>class</i>. Otherwise
     * the given {@code model} is taken into account.
     *
     * @param formalMethodParameter
     *          The formal method parameter, not {@code null}.
     *
     * @param model
     *          The model to get the type reference of, not {@code null}. Must contain
     *          a reference type for the given {@code formalMethodParameter}.
     *
     * @return
     *          The type reference for the given {@code formalMethodParameter}.
     */
    private static CtTypeReference<?> buildTypeReference(String formalMethodParameter, CtModel model) {
        if(UNANN_PRIMITIVE_TYPE_MATCH_PREDICATE.test(formalMethodParameter)) {
            return PrimitiveTypeUtils.getPrimitiveTypeReference(formalMethodParameter);
        }

        var isFullyQualified = formalMethodParameter.contains(".");
        var fullyQualifiedClassName = isFullyQualified ? formalMethodParameter : String.format("%s.%s", JAVA_LANG_PACKAGE_QUALIFIED_NAME, formalMethodParameter);

        if(fullyQualifiedClassName.startsWith(JAVA_FULLY_QUALIFIED_PREFIX)) {
            return buildJavaReferenceType(fullyQualifiedClassName);
        }

        return model.getAllTypes().stream()
                .filter(type -> fullyQualifiedClassName.equals(type.getQualifiedName()))
                .findFirst()
                .map(CtType::getReference)
                .orElseThrow(() -> new IllegalStateException(String.format("The model does not contain the type '%s'!",
                        fullyQualifiedClassName)));
    }

    /**
     *
     * @param fullyQualifiedClassName
     *          The fully qualified class name of a class located in the <i>java</i> package, not
     *          {@code null}.
     *
     * @return
     *          A type reference for the class identified by the given {@code fullyQualifiedClassName}.
     *
     * @throws IllegalStateException
     *          In case the class was not found using {@code this} class loader.
     */
    private static CtTypeReference<?> buildJavaReferenceType(String fullyQualifiedClassName) {
        try {
            return typeFactory.createReference(Class.forName(fullyQualifiedClassName));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(String.format("Class '%s' was not found!", fullyQualifiedClassName), e);
        }
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
    private static String extractMethodName(String methodIdentifier) {
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
    private static String extractClassName(String methodIdentifier) {
        return methodIdentifier.split("#")[0];
    }

}
