package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypedElement;

import java.util.Collection;
import java.util.Set;

/**
 * {@link AccessibilityAwareFieldFinder} implementation searching for fields which are accessible
 * directly through field access.
 * <p/>
 * <b>Note:</b> Java modules added in Java 9 are not taken into account.
 */
@Slf4j
public class DirectAccessAccessibilityAwareFieldFinder extends AccessibilityAwareFieldFinder {

    /**
     * Checks if the field is accessible through direct field access according to the Java
     * Language Specification §6.6.1 and §6.6.2.
     *
     * @param testClazz
     *          The test class whose methods could potentially access the given {@code type}'s
     *          fields, not {@code null}.
     *
     * @param field
     *          The field to check, not {@code null}.
     *
     * @return
     *          {@code true}, iff one of the following conditions is fulfilled (see §6.6.1 of the Java Language
     *          Specification for more details):
     *          <ul>
     *              <li>the {@code testClazz} is exactly the same class as the {@code field}'s declaring class</li>
     *              <li>the field is declared <i>public</i></li>
     *              <li>the field is declared <i>protected</i> and one of the following conditions is fulfilled</li>
     *              <ul>
     *                  <li>the {@code testClazz} is in the same package as the {@code field}'s declaring class</li>
     *                  <li>the {@code testClazz} is a subclass of the {@code field}'s declaring class (see §6.6.2 of the JLS)</li>
     *                  <li>the {@code testClazz} is an inner class of the {@code field}'s declaring class</li>
     *              </ul>
     *              <li>the field is declared <i>package private</i> and one of the following conditions is fulfilled</li>
     *              <ul>
     *                  <li>the {@code testClazz} is in the same package as the {@code field}'s declaring class</li>
     *                  <li>the {@code testClazz} is an inner class of the {@code field}'s declaring class</li>
     *              </ul>
     *              <li>the field is declared <i>private</i> and one of the following conditions is fulfilled</li>
     *              <ul>
     *                  <li>the {@code testClazz} is an inner class of the {@code field}'s declaring class</li>
     *              </ul>
     *          </ul>
     */
    @Override
    protected boolean isFieldAccessible(CtClass<?> testClazz, CtField<?> field) {
        if(isPublicField(field) || field.getDeclaringType().equals(testClazz) ) {
            return true;
        } else if(isProtectedField(field)) {
            return isInSamePackageAsDeclaringType(field, testClazz) || isRealSubClassOfDeclaringClass(field, testClazz);
        } else if(isPackagePrivateField(field) && isInSamePackageAsDeclaringType(field, testClazz)) {
            return true;
        }

        return isInnerClassOfDeclaringType(field, testClazz);
    }

    /**
     *
     * @param testClazz
     *          The test class whose methods can access the given {@code field},
     *          not {@code null}.
     *
     * @param field
     *          The field which can be accessed by inside the given {@code testClazz},
     *          not {@code null}.
     *
     * @param <T>
     *          The type of the field.
     *
     * @return
     *          A set containing the given {@code field} as its only element.
     */
    @Override
    protected <T> Collection<CtTypedElement<T>> findAccessGrantingElements(CtClass<?> testClazz, CtField<T> field) {
        return Set.of(field);
    }

}
