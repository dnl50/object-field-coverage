package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * {@link AccessibilityAwareFieldFinder} implementation which finds fields for which a getter method
 * is present on the declaring type that matches the signature of a Java Beans Getter Method. See section
 * 8.3 of the JavaBeans Spec Version 1.01 for more details.
 * <p/>
 * <b>Note:</b> This implementation also applies the same naming convention to <i>static</i> fields.
 */
@Slf4j
public class JavaBeansAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     *
     * @param accessingType
     *          The class whose methods could potentially access the given {@code type}'s
     *          fields, not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the a Java Beans Getter Method is present on the given {@code field}'s
     *          declaring type. {@code false} is returned otherwise.
     */
    @Override
    protected boolean isFieldAccessible(CtType<?> accessingType, CtField<?> field) {
        return this.findJavaBeansGetterMethod(field).isPresent();
    }

    /**
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
     *          A set containing the given {@code field}'s Java Beans Getter method which is present
     *          on the {@code field}'s declaring class as its only element.
     *
     * @throws IllegalStateException
     *          When no Java Beans Getter method is present for the {@code field} on the
     *          {@code field}'s declaring class.
     */
    @Override
    protected <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtType<?> accessingType, CtField<T> field) {
        var getterMethod = this.findJavaBeansGetterMethod(field)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No Java Beans Getter method present for field '%s'!", field.getSimpleName())
                ));

        return Set.of(getterMethod);
    }

    /**
     * Searches for a public, non-static, no-arg method on the declaring class of the given {@code field}. The name
     * of the method must match the Java Beans getter method pattern as describe in
     * section 8.3 of the JavaBeans Spec Version 1.01. The return type must match the {@code field}'s type.
     * <p/>
     * <b>Example:</b> If the {@link CtField#getSimpleName() simple name} of the given {@code field} is
     * <i>name</i> and the {@code field} is a {@code java.lang.String}, a method with the following
     * signature is searched on the {@code field}'s declaring class:
     * <pre>
     *     public java.lang.String getName();
     * </pre>
     *
     * @param field
     *          The {@link CtField} to find the Java Beans getter method for, not {@code null}.
     *
     * @return
     *          An optional containing the Java Beans getter method for the given {@code field} in case it is
     *          is present on the declaring class, or an empty optional otherwise. The getter method can be declared
     *          static when the given {@code field} is static as well.
     */
    private <T> Optional<CtMethod<T>> findJavaBeansGetterMethod(CtField<T> field) {
        var getterPrefix = javaBeansGetterMethodPrefix(field);
        var capitalizedFieldSimpleName = StringUtils.capitalize(field.getSimpleName());
        var javaBeansGetterName = String.format("%s%s", getterPrefix, capitalizedFieldSimpleName);
        var fieldType = field.getType();
        var getterCanBeStatic = field.isStatic();

        var getterMethod = field.getDeclaringType()
                .getMethod(fieldType, javaBeansGetterName);

        var getterMethodExists = Objects.nonNull(getterMethod);
        var getterMethodIsPublic = getterMethodExists && getterMethod.isPublic();
        var getterMethodIsStatic = getterMethodExists && getterMethod.isStatic();

        if(getterMethodExists && getterMethodIsPublic && (getterCanBeStatic || !getterMethodIsStatic)) {
            return Optional.of(getterMethod);
        }

        return Optional.empty();
    }

    /**
     *
     * @param field
     *          The {@link CtField} to get the Java Beans getter method prefix for,
     *          not {@code null}.
     *
     * @return
     *          <i>is</s> in case the given {@code field} is of primitive type {@code boolean}
     *          or reference type {@link Boolean}. <i>get</i> is returned otherwise.
    */
    private String javaBeansGetterMethodPrefix(CtField<?> field) {
        var fieldType = field.getType();
        var fieldTypeName = fieldType.getQualifiedName();
        var isOfBooleanPrimitiveType = (fieldType.isPrimitive() && "boolean".equals(fieldTypeName));
        var isOfBooleanReferenceType = "java.lang.Boolean".equals(fieldTypeName);

        return (isOfBooleanPrimitiveType || isOfBooleanReferenceType) ? "is" : "get";
    }

}
