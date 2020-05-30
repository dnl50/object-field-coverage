package de.adesso.objectfieldcoverage.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class providing static utility methods for {@link CtType} instances.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeUtil {

    /**
     * The fully qualified name of the {@link Object} class.
     */
    private static final String OBJECT_FULLY_QUALIFIED_NAME = "java.lang.Object";

    /**
     * The fully qualified name of the {@link Enum} class.
     */
    private static final String ENUM_FULLY_QUALIFIED_NAME = "java.lang.Enum";

    /**
     * The first element of the returned list is the given {@code clazz} itself, then its superclass, then
     * the superclass of the superclass and so on.
     *
     * @param clazz
     *          The {@link CtClass} to get the <i>explicit</i> superclasses of, not {@code null}.
     *
     * @return
     *          An <b>unmodifiable</b> list containing all real superclasses of the given {@code type},
     *          excluding {@link Object} and {@link Enum}. The given {@code clazz} is included in
     *          the returned list.
     *
     * @see #findExplicitSuperClasses(CtType)
     */
    public static List<CtClass<?>> findExplicitSuperClassesIncludingClass(CtClass<?> clazz) {
        var explicitSuperClasses = findExplicitSuperClasses(clazz);
        var resultList = new ArrayList<CtClass<?>>(explicitSuperClasses.size() + 1);

        resultList.add(clazz);
        resultList.addAll(explicitSuperClasses);

        return List.copyOf(resultList);
    }

    /**
     * The first element of the returned list is the superclass of the given {@code clazz} itself, then its superclass
     * and so on.
     *
     * @param type
     *          The {@link CtType} to get the <i>explicit</i> superclasses of, not {@code null}.
     *
     * @return
     *          An <b>unmodifiable</b> list containing all real superclasses of the given {@code type},
     *          excluding {@link Object} and {@link Enum}. The given {@code type} is not included
     *          in the returned list.
     *
     * @see #findExplicitSuperClassesIncludingClass(CtClass)
     */
    public static List<CtClass<?>> findExplicitSuperClasses(CtType<?> type) {
        Objects.requireNonNull(type, "The given type cannot be null!");

        var currentSuperClass = findSuperClass(type);
        var superClasses = new ArrayList<CtClass<?>>();

        while(currentSuperClass != null) {
            var qualifiedName = currentSuperClass.getQualifiedName();

            if(OBJECT_FULLY_QUALIFIED_NAME.equals(qualifiedName) || ENUM_FULLY_QUALIFIED_NAME.equals(qualifiedName)) {
                break;
            }

            superClasses.add(currentSuperClass);
            currentSuperClass = findSuperClass(currentSuperClass);
        }

        return List.copyOf(superClasses);
    }

    /**
     *
     * @param type
     *          The type of which the superclass should be retrieved, not {@code null}.
     *
     * @return
     *          The superclass of the given {@code type} or {@code null} if there is no parent
     *          class present in the underlying model.
     */
    private static CtClass<?> findSuperClass(CtType<?> type) {
        return type.getSuperclass() != null ? (CtClass<?>) type.getSuperclass().getTypeDeclaration() : null;
    }

}
