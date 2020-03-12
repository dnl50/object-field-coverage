package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtFieldReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JavaBeansAccessibilityAwareFieldFinder implements AccessibilityAwareFieldFinder {

    @Override
    public List<CtField<?>> findAccessibleFields(CtClass<?> testClazz, CtType<?> type) {
        Objects.requireNonNull(testClazz, "testClazz cannot be null!");
        Objects.requireNonNull(type, "type cannot be null!");

        return type.getAllFields().stream()
                .map(CtFieldReference::getFieldDeclaration)
                .filter(ctField -> hasJavaBeansGetterMethod(ctField))
                .collect(Collectors.toList());
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
     *     public java.lang.String getUser();
     * </pre>
     *
     * @param field
     *          The {@link CtField} to find the Java Beans getter method for, not {@code null}.
     *
     * @return
     *          {@code true}, if if a Java Beans getter method for the given {@code field}
     *          is present in the declaring class, or {@code false} otherwise.
     */
    private boolean hasJavaBeansGetterMethod(CtField<?> field) {
        var getterPrefix = javaBeansGetterMethodPrefix(field);
        var capitalizedFieldSimpleName = StringUtils.capitalize(field.getSimpleName());
        var javaBeansGetterName = String.format("%s%s", getterPrefix, capitalizedFieldSimpleName);
        var fieldType = field.getType();

        var getterMethod = field.getDeclaringType()
                .getMethod(fieldType, javaBeansGetterName);

        return Objects.nonNull(getterMethod) && getterMethod.isPublic();
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
        var isOfBooleanType = "java.lang.Boolean".equals(fieldTypeName);

        return (isOfBooleanPrimitiveType || isOfBooleanType) ? "is" : "get";
    }

    /**
     *
     * @param field
     *          The {@link CtField}
     *
     * @param testClazz
     *
     * @return
     *          {@code true}, if at least one of the following conditions is met:
     *          <ul>
     *              <li>
     *                  the given {@code field} is declared as {@code public}
     *              </li>
     *              <li>
     *                  the given {@code field} is declared as {@code protected} and the package of
     *                  the class in which the {@code field} is declared is a prefix of the
     *                  given {@code testMethodPackage}
     *              </li>
     *              <li>
     *                  the given {@code field} is declared as {@code package-private} and the given
     *                  class in which the {@code field} is declared is in the same package
     *              </li>
     *          </ul>
     */
    private boolean isDirectlyAccessible(CtField<?> field, CtType<?> testClazz) {
        return false;
    }

    private boolean fieldIsDeclaredInInnerClass() {
        return false;
    }

}
