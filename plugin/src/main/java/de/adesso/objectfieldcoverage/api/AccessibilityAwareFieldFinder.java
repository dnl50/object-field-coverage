package de.adesso.objectfieldcoverage.api;

import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtFieldReference;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An abstract class used to find <i>accessible</i> fields of a given type. The meaning of <i>accessible</i>
 * depends on the implementation.
 */
public abstract class AccessibilityAwareFieldFinder {

    /**
     *
     * @param accessingType
     *          The class whose methods could potentially access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is accessible from the given {@code accessingType}.
     *          {@code false} is returned otherwise.
     */
    protected abstract boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field);

    /**
     * This method is required since it is important to know through which typed element (e.g. the field
     * itself or a getter method) a given {@link CtField} can be accessed in a given {@link CtType}.
     * </P>
     * <b>Example</b>: Further down the line in the analysis process it must be known that the
     * {@code getX()} method is an alias for the field {@code x}, so an assertion on the the invocation result of the
     * {@code getX()} method inside a test method is counted as an assertion on field {@code x}.
     *
     * @param accessingType
     *          The class whose methods can access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code accessingType},
     *          not {@code null}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          The typed elements which grant access to the given {@code field}. Must contain at least one
     *          element when {@link #isFieldAccessible(CtType, CtField)} returns {@code true}. Implementations
     *          might throw unchecked exceptions otherwise.
     */
    protected abstract <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field);

    /**
     *
     * @param accessingType
     *          The type whose methods could potentially access the given {@code type}'s
     *          fields, not {@code null}.
     *
     * @param type
     *          The type to get the accessible fields of, not {@code null}.
     *
     * @return
     *          A list of all fields which are accessible from the given {@code accessingType} combined with the
     *          typed element which grants access to the field. Includes fields which are directly declared in
     *          the given {@code type} or in any super-type. An empty list is returned in case the
     *          given {@code type} is an interface.
     */
    @SuppressWarnings("unchecked")
    public List<AccessibleField<?>> findAccessibleFields(CtType<?> accessingType, CtType<?> type) {
        Objects.requireNonNull(accessingType, "accessingType cannot be null!");
        Objects.requireNonNull(type, "type cannot be null!");

        if(type.isInterface()) {
            return List.of();
        }

        return type.getAllFields().stream()
                .map(CtFieldReference::getFieldDeclaration)
                .filter(field -> this.isFieldAccessible(accessingType, field))
                .map(field -> {
                    var accessGrantingElements = this.findAccessGrantingElements(accessingType, field);

                    @SuppressWarnings("rawtypes")
                    var accessibleField = (AccessibleField<?>) new AccessibleField(field, Set.copyOf(accessGrantingElements));
                    return accessibleField;
                })
                .collect(Collectors.toList());
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          The result of the invocation of the {@link CtField#isPublic()}
     *          method of the given {@code field}.
     */
    protected boolean isPublicField(CtField<?> field) {
        return field.isPublic();
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          The result of the invocation of the {@link CtField#isProtected()}
     *          method of the given {@code field}.
     */
    protected boolean isProtectedField(CtField<?> field) {
        return field.isProtected();
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if {@link CtField#isPublic()}, {@link CtField#isProtected()}
     *          and {@link CtField#isPrivate()} all return {@code false}. {@code false} is
     *          returned otherwise.
     */
    protected boolean isPackagePrivateField(CtField<?> field) {
        return !field.isPublic() && !field.isProtected() && !field.isPrivate();
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          The result of the invocation of the {@link CtField#isPrivate()}
     *          method of the given {@code field}.
     */
    protected boolean isPrivateField(CtField<?> field) {
        return field.isPrivate();
    }

    /**
     * Walks up the super-class chain of the given {@code type} until either
     * the given {@code field}'s declaring class is reached or the super-class
     * is {@code null}.
     *
     * @param field
     *          The field to get the declaring class of, not {@code null}.
     *
     * @param type
     *          The type which contains the methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} is a <i>real</i> subclass of the declaring
     *          class of the given {@code field}. {@code false} is returned otherwise.
     */
    protected boolean isRealSubClassOfDeclaringClass(CtField<?> field, CtType<?> type) {
        var fieldDeclaringType = field.getDeclaringType();

        var currentSuperClassReferenceType = type.getSuperclass();

        while(currentSuperClassReferenceType != null) {
            var currentSuperClassType = currentSuperClassReferenceType.getTypeDeclaration();
            if(fieldDeclaringType.equals(currentSuperClassType)) {
                return true;
            }

            currentSuperClassReferenceType = currentSuperClassReferenceType.getSuperclass();
        }

        return false;
    }

    /**
     * Compares the packages of the given {@code field}'s declaring type and the
     * given {@code type}.
     *
     * @param field
     *          The field to get the declaring type of, not {@code null}.
     *
     * @param type
     *          The type which contains the methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the declaring type of the given {@code field} is in the
     *          same package as the given {@code type}. {@code false} is returned otherwise.
     */
    protected boolean isInSamePackageAsDeclaringType(CtField<?> field, CtType<?> type) {
        var declaringTypePackage = field.getDeclaringType()
                .getPackage();
        var typePackage = type.getPackage();

        return declaringTypePackage.equals(typePackage);
    }

    /**
     * Walks up the declaring type chain of the given {@code type} until it either
     * reaches the given {@code field}'s declaring type or the declaring type is {@code null}.
     *
     * @param field
     *          The field to get the declaring class of, not {@code null}.
     *
     * @param type
     *          The type which contains the methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code type} is a inner type of the given
     *          {@code field}'s declaring type. {@code false} is returned otherwise.
     */
    protected boolean isInnerClassOfDeclaringType(CtField<?> field, CtType<?> type) {
        var fieldDeclaringType = field.getDeclaringType();

        var classDeclaringType = type.getDeclaringType();
        while(Objects.nonNull(classDeclaringType)) {
            if(fieldDeclaringType.equals(classDeclaringType)) {
                return true;
            }

            classDeclaringType = classDeclaringType.getDeclaringType();
        }

        return false;
    }

}
