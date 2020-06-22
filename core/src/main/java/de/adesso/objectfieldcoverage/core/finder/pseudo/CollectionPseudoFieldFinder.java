package de.adesso.objectfieldcoverage.core.finder.pseudo;

import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoClassGenerator;
import de.adesso.objectfieldcoverage.core.finder.pseudo.generator.PseudoFieldGenerator;
import de.adesso.objectfieldcoverage.core.util.TypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link PseudoFieldFinder} implementation for {@link java.util.Map}s and (un-)ordered {@link java.util.Collection}s.
 */
@Slf4j
public class CollectionPseudoFieldFinder extends PseudoFieldFinder {

    /**
     * The types (classes and interfaces) which indicate an ordered collection. Array types are included
     * implicitly as they are regarded as ordered collections.
     */
    private static final Set<Class<?>> ORDERED_TYPES = Set.of(
            List.class, Queue.class, SortedSet.class, LinkedHashSet.class
    );

    /**
     * The types (classes and interfaces) which indicate an unordered collection.
     */
    private static final Set<Class<?>> UNORDERED_TYPES = Set.of(
            Collection.class, Set.class, Map.class
    );

    /**
     * A map containing entries which map a Java {@link Class} to its corresponding Spoon
     * {@link CtTypeReference}.
     */
    private final Map<Class<?>, CtTypeReference<?>> typeRefCache;

    /**
     * The fully qualified name of the {@code java.util} package. Pseudo classes for maps ands collections are part of this
     * package since it is assumed that all pseudo fields are compared in the {@code equals} method of the type.
     */
    private static final String JAVA_UTIL_PACKAGE = "java.util";

    /**
     * The {@link TypeFactory} which is used to create type references with.
     */
    private final TypeFactory typeFactory;

    public CollectionPseudoFieldFinder(PseudoClassGenerator pseudoClassGenerator, PseudoFieldGenerator pseudoFieldGenerator) {
        super(pseudoClassGenerator, pseudoFieldGenerator);

        this.typeFactory = new TypeFactory();
        this.typeRefCache = new HashMap<>();
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} which should be checked for pseudo fields, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typeRef} represents an ordered or an unordered
     *          collection/map. {@code false} is returned otherwise.
     */
    @Override
    public boolean containsPseudoFields(CtTypeReference<?> typeRef) {
        return isOrdered(typeRef) || isUnordered(typeRef);
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} to get the simple names and {@link CtTypeReference}s of the pseudo fields for,
     *          not {@code null}.
     *
     * @return
     *          A set containing ({@code size}, {@code boolean}), ({@code elements}, {@code boolean}) pairs and an
     *          additional ({@code order}, {@code boolean}) pair in case the given {@code typeRef}
     *          {@link #isOrdered(CtTypeReference) is ordered}.
     */
    @Override
    protected Set<Pair<String, CtTypeReference<?>>> fieldNamesAndTypes(CtTypeReference<?> typeRef) {
        var pairs = new HashSet<Pair<String, CtTypeReference<?>>>();

        pairs.add(Pair.of("size", typeFactory.BOOLEAN_PRIMITIVE));
        pairs.add(Pair.of("elements", typeFactory.BOOLEAN_PRIMITIVE));

        if(isOrdered(typeRef)) {
            pairs.add(Pair.of("order", typeFactory.BOOLEAN_PRIMITIVE));
        }

        return pairs;
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type to get the pseudo class prefix for, not {@code null}.
     *
     * @return
     *          The simple name of the given {@code typeRef}.
     */
    @Override
    protected String getPseudoClassPrefix(CtTypeReference<?> typeRef) {
        return typeRef.getSimpleName();
    }

    /**
     *
     * @param typeRef
     *          The {@link CtTypeReference} of the type the pseudo fields should be generated for, not {@code null}.
     *
     * @return
     *          {@value JAVA_UTIL_PACKAGE}
     */
    @Override
    protected String getPackageQualifiedName(CtTypeReference<?> typeRef) {
        return JAVA_UTIL_PACKAGE;
    }

    /**
     *
     * @param typeRef
     *          The type reference of the type to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typeRef} is an array type reference or it is equal to a type
     *          or extends/implements a type in the {@link #ORDERED_TYPES} set. {@code false} is returned
     *          otherwise.
     */
    private boolean isOrdered(CtTypeReference<?> typeRef) {
        if(typeRef.isArray()) {
            return true;
        }

        var orderedInterfaceTypes = ORDERED_TYPES.stream()
                .filter(Class::isInterface)
                .toArray(Class[]::new);
        var orderedClassTypes = ORDERED_TYPES.stream()
                .filter(Predicate.not(Class::isInterface))
                .toArray(Class[]::new);

        return isOrImplementsInterface(typeRef, orderedInterfaceTypes)
                || isOrExtendsClass(typeRef, orderedClassTypes);
    }

    /**
     *
     * @param typeRef
     *          The type reference of the type to check, not {@code null}.
     *
     * @return
     *          {@code true}, if it is not {@link #isOrdered(CtTypeReference) ordered} and is equal to a type
     *          or extends/implements a type in the {@link #UNORDERED_TYPES} set. {@code false} is returned
     *          otherwise.
     */
    private boolean isUnordered(CtTypeReference<?> typeRef) {
        if(isOrdered(typeRef)) {
            return false;
        }

        var unorderedInterfaceTypes = UNORDERED_TYPES.stream()
                .filter(Class::isInterface)
                .toArray(Class[]::new);
        var unorderedClassTypes = UNORDERED_TYPES.stream()
                .filter(Predicate.not(Class::isInterface))
                .toArray(Class[]::new);

        return isOrImplementsInterface(typeRef, unorderedInterfaceTypes)
                || isOrExtendsClass(typeRef, unorderedClassTypes);
    }

    /**
     *
     * @param typeRef
     *          The type reference which might be equal to or might implement the given at at least one of the given
     *          {@code interfaceTypes}, not {@code null}.
     *
     * @param interfaceTypes
     *          The interfaces, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typeRef} is equal to or implements at least one of the given
     *          {@code interfaceTypes}. {@code false} is returned otherwise.
     *
     * @implNote The order of the given {@code interfaceTypes} is taken into account.
     */
    private boolean isOrImplementsInterface(CtTypeReference<?> typeRef, Class<?>... interfaceTypes) {
        for(var interfaceType : interfaceTypes) {
            var interfaceTypeRef = getTypeReferenceFor(interfaceType);
            if(interfaceTypeRef.equals(typeRef.getTypeErasure()) || TypeUtils.findAllSuperInterfaces(typeRef).contains(interfaceTypeRef)) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param classTypes
     *          The {@link Class}es which might be equal to or are extended by the given {@code typeRef},
     *          {@code null}.
     *
     * @param typeRef
     *          The type reference which might be equal to or extends the given {@code classType}, not
     *          {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code typeRef} is equal to or extends at least one type in the given
     *          {@code classTypes}. {@code false} is returned otherwise.
     *
     * @implNote The order of the given {@code interfaceTypes} is not taken into account.
     */
    private boolean isOrExtendsClass(CtTypeReference<?> typeRef, Class<?>... classTypes) {
        var classTypeRefs = Arrays.stream(classTypes)
                .map(this::getTypeReferenceFor)
                .collect(Collectors.toList());

        if(classTypeRefs.contains(typeRef.getTypeErasure())) {
            return true;
        }

        var nonFinalTypeRefs = classTypeRefs.stream()
                .filter(classTypeRef -> !classTypeRef.getModifiers().contains(ModifierKind.FINAL))
                .collect(Collectors.toList());

        var objectTypeRef = getTypeReferenceFor(Object.class);
        var currentSuperClassRef = typeRef.getSuperclass();

        while(currentSuperClassRef != null && !objectTypeRef.equals(typeRef)) {
            if(nonFinalTypeRefs.contains(currentSuperClassRef.getTypeErasure())) {
                return true;
            }

            currentSuperClassRef = currentSuperClassRef.getSuperclass();
        }

        return false;
    }

    /**
     *
     * @param clazz
     *          The Java {@link Class} instance to get the corresponding Spoon {@link CtTypeReference} for, not
     *          {@code null}.
     *
     * @param <T>
     *          The type of the class.
     *
     * @return
     *          The corresponding type erased {@link CtTypeReference}.
     */
    @SuppressWarnings("unchecked")
    private <T> CtTypeReference<T> getTypeReferenceFor(Class<T> clazz) {
        return (CtTypeReference<T>) typeRefCache.computeIfAbsent(clazz, typeFactory::createReference);
    }

}
