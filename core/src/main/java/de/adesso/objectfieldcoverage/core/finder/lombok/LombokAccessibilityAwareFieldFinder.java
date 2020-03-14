package de.adesso.objectfieldcoverage.core.finder.lombok;


import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;

import java.util.Objects;

/**
 * {@link AccessibilityAwareFieldFinder} finding fields which are either directly annotated with Lombok's
 * {@link Getter} annotation or their declaring type is annotated with {@link Getter} or {@link Data}.
 */
public class LombokAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     *
     * @param testClazz
     *          The test class whose methods could potentially access the given {@code type}'s
     *          fields, not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given field has a generated getter which is accessible
     *          by the given {@code testClazz}' methods. {@code false} is returned otherwise.
     */
    @Override
    protected boolean isFieldAccessible(CtClass<?> testClazz, CtField<?> field) {
        return this.hasAccessibleGeneratedGetter(testClazz, field);
    }

    /**
     * Returns {@code true} if one of the following conditions is fulfilled for the generated getter:
     * <ul>
     *     <li>the getter is declared <i>public</i></li>
     *     <li>the getter is declared <i>protected</i> and the {@code testClazz} is in the same
     *     package as or a sub-class of the {@code field}'s declaring type</li>
     *     <li>the getter is declared <i>package private</i> and the {@code testClazz} is in the same
     *     package as or an inner class of the {@code field}'s declaring type</li>
     *     <li>the getter is declared <i>private</i> and the {@code testClazz} is an inner class
     *     of the {@code field}'s declaring type</li>
     * </ul>
     *
     * See §6.6.1 of the Java Language Specification for more details on why this is implemented
     * this way.
     *
     * <b>Note:</b> {@link AccessLevel#MODULE} and {@link AccessLevel#PACKAGE} are regarded as
     * equal.
     *
     * @param testClazz
     *          The test class whose methods could potentially access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if lombok generates a getter which is accessible from the given
     *          {@code testClazz}' methods. {@code false} is returned otherwise.
     */
    private boolean hasAccessibleGeneratedGetter(CtClass<?> testClazz, CtField<?> field) {
        if(!isFieldOrDeclaringClassAnnotatedWithGetter(field)) {
            return false;
        }

        var accessLevel = getAccessLevelOfGeneratedGetter(field);

        switch (accessLevel) {
            case PUBLIC:
                return true;
            case PROTECTED:
                return isInSamePackageAsDeclaringType(field, testClazz) || isRealSubClassOfDeclaringClass(field, testClazz);
            case PACKAGE: case MODULE:
                return isInSamePackageAsDeclaringType(field, testClazz) || isInnerClassOfDeclaringType(field, testClazz);
            case PRIVATE:
                return isInnerClassOfDeclaringType(field, testClazz);
            default:
                return false;
        }
    }

    /**
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code field} is annotated with {@link Getter}
     *          or the fields declaring type is annotated with {@link Getter} or {@link Data}. {@code false}
     *          is returned otherwise.
     */
    private boolean isFieldOrDeclaringClassAnnotatedWithGetter(CtField<?> field) {
        if(Objects.nonNull(field.getAnnotation(Getter.class))) {
            return true;
        }

        var declaringType = field.getDeclaringType();
        return Objects.nonNull(declaringType.getAnnotation(Getter.class)) ||
                Objects.nonNull(declaringType.getAnnotation(Data.class));
    }

    /**
     * See Lombok's <a href="https://projectlombok.org/features/GetterSetter">Getter</a> and
     * <a href="https://projectlombok.org/features/Data">Data</a> documentation for more details.
     *
     * @param field
     *          The field to get the access level of the generated getter for, not
     *          {@code null}.
     *
     * @return
     *          The access level specified in the Getter annotation of the field (if present),
     *          the access level specified in the Getter annotation of the declaring type (if present)
     *          or {@link AccessLevel#PUBLIC} otherwise, since the declaring type must be annotated
     *          with {@link Data} then and the access level can't be specified. Exactly in that order.
     */
    private AccessLevel getAccessLevelOfGeneratedGetter(CtField<?> field) {
        var fieldGetterAnnotation = field.getAnnotation(Getter.class);

        if(Objects.nonNull(fieldGetterAnnotation)) {
            return fieldGetterAnnotation.value();
        }

        var declaringTypeGetterAnnotation = field.getDeclaringType()
                .getAnnotation(Getter.class);
        if(Objects.nonNull(declaringTypeGetterAnnotation)) {
            return declaringTypeGetterAnnotation.value();
        }

        return AccessLevel.PUBLIC;
    }

}
