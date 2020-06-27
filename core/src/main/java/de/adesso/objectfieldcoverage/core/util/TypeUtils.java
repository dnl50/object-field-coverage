package de.adesso.objectfieldcoverage.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeInformation;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class providing static utility methods for {@link CtType} instances.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtils {

    /**
     * A simple regex to check whether the absolute path of a Java class' source file is
     * located in a directory which contains test source files.
     */
    private static final String JAVA_TEST_CLASS_PATH_REGEX = "^.*src([/\\\\])test([/\\\\])java([/\\\\]).*\\.java$";

    /**
     * The fully qualified name of the {@link Object} class.
     */
    private static final String OBJECT_FULLY_QUALIFIED_NAME = "java.lang.Object";

    /**
     * The fully qualified name of the {@link Enum} class.
     */
    private static final String ENUM_FULLY_QUALIFIED_NAME = "java.lang.Enum";

    /**
     * The first element of the returned list is the given {@code clazzRef} itself, then its superclass, then
     * the superclass of the superclass and so on.
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to get the <i>explicit</i> superclass references of, not {@code null}.
     *
     * @return
     *          An <b>unmodifiable</b> list containing all <i>explicit</i> superclass references of the given
     *          {@code clazzRef}, excluding {@link Object} and {@link Enum}. The given {@code clazzRef} is included in
     *          the returned list.
     *
     * @see #findExplicitSuperClasses(CtTypeReference)
     */
    public static List<CtTypeReference<?>> findExplicitSuperClassesIncludingClass(CtTypeReference<?> clazzRef) {
        var explicitSuperClasses = findExplicitSuperClasses(clazzRef);
        var resultList = new ArrayList<CtTypeReference<?>>(explicitSuperClasses.size() + 1);

        resultList.add(clazzRef);
        resultList.addAll(explicitSuperClasses);

        return List.copyOf(resultList);
    }

    /**
     * The first element of the returned list is the superclass of the given {@code clazzRef} itself, then its superclass
     * and so on.
     *
     * @param clazzRef
     *          The {@link CtTypeReference} to get the <i>explicit</i> superclass references of, not {@code null}.
     *
     * @return
     *          An <b>unmodifiable</b> list containing all <i>explicit</i>> superclass references of the given
     *          {@code clazzRef}, excluding {@link Object} and {@link Enum}. The given {@code clazzRef} is not included
     *          in the returned list.
     *
     * @see #findExplicitSuperClassesIncludingClass(CtTypeReference)
     */
    public static List<CtTypeReference<?>> findExplicitSuperClasses(CtTypeReference<?> clazzRef) {
        Objects.requireNonNull(clazzRef, "The type reference cannot be null!");

        if(!clazzRef.isClass()) {
            throw new IllegalArgumentException("The given type reference is not a class reference!");
        }

        var currentSuperClassRef = clazzRef.getSuperclass();
        var superClassRefs = new ArrayList<CtTypeReference<?>>();

        while(currentSuperClassRef != null) {
            var qualifiedName = currentSuperClassRef.getQualifiedName();

            if(OBJECT_FULLY_QUALIFIED_NAME.equals(qualifiedName) || ENUM_FULLY_QUALIFIED_NAME.equals(qualifiedName)) {
                break;
            }

            superClassRefs.add(currentSuperClassRef);
            currentSuperClassRef = currentSuperClassRef.getSuperclass();
        }

        return List.copyOf(superClassRefs);
    }

    /**
     *
     * @param typeRef
     *          The type reference to get all super interfaces of, not {@code null}.
     *
     * @return
     *          A set containing all type erased super interfaces of the given {@code typeRef}, including the super
     *          interfaces of the super interfaces of the type ref.
     */
    public static Set<CtTypeReference<?>> findAllSuperInterfaces(CtTypeReference<?> typeRef) {
        Objects.requireNonNull(typeRef, "The type reference cannot be null!");

        var superInterfaces = new HashSet<>(findSuperInterfacesWithTypeErasure(typeRef));
        var newlyDiscoveredSuperInterfaces = new LinkedList<>(superInterfaces);

        while(!newlyDiscoveredSuperInterfaces.isEmpty()) {
            var currentSuperInterface = newlyDiscoveredSuperInterfaces.removeFirst();
            var currentSuperInterfaces = findSuperInterfacesWithTypeErasure(currentSuperInterface);

            currentSuperInterfaces.stream()
                    .filter(Predicate.not(superInterfaces::contains))
                    .forEach(newlyDiscoveredSuperInterfaces::addLast);

            superInterfaces.addAll(currentSuperInterfaces);
        }

        return superInterfaces;
    }

    /**
     *
     * @param clazz
     *          The {@link CtClass} to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code clazz}' source file is present and located in an absolute path
     *          matching the regex {@value JAVA_TEST_CLASS_PATH_REGEX}. {@code false} is returned otherwise.
     */
    public static boolean isPotentialTestClass(CtClass<?> clazz) {
        var clazzFile = clazz.getPosition()
                .getFile();

        if(clazzFile != null) {
            return clazzFile.getAbsolutePath()
                     .matches(JAVA_TEST_CLASS_PATH_REGEX);
        }

        return false;
    }


    /**
     *
     * @param typeRef
     *          The type reference to get the super interfaces of, not {@code null}.
     *
     * @return
     *          The super interfaces of the given {@code typeRef} as their type erasure.
     */
    private static Set<CtTypeReference<?>> findSuperInterfacesWithTypeErasure(CtTypeReference<?> typeRef) {
        return typeRef.getSuperInterfaces().stream()
                .map(CtTypeInformation::getTypeErasure)
                .collect(Collectors.toSet());
    }

}
