package de.adesso.objectfieldcoverage.core.finder;

import de.adesso.objectfieldcoverage.api.AccessibilityAwareFieldFinder;
import lombok.extern.slf4j.Slf4j;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@link AccessibilityAwareFieldFinder} implementation searching for fields which are accessible
 * directly through field access.
 * <p/>
 * <b>Note:</b> Java modules added in Java 9 are not taken into account.
 */
@Slf4j
public class DirectAccessAccessibilityAwareFieldFinder implements AccessibilityAwareFieldFinder {

    /**
     *
     * @param testClazz
     *          The test class which contains test methods which access the given
     *          {@code type}'s fields, not {@code null}.
     *
     * @param type
     *          The {@link CtClass} for which the accessible fields should be found, not
     *          {@code null}.
     *
     * @return
     *          A list containing the {@link CtField}s which are directly accessible
     *          through field access by the given {@code testClazz}' methods, not {@code null}.
     */
    @Override
    public List<CtField<?>> findAccessibleFields(CtClass<?> testClazz, CtType<?> type) {
        Objects.requireNonNull(testClazz, "testClazz cannot be null!");
        Objects.requireNonNull(type, "type cannot be null!");

        if(type.isInterface()) {
            log.info("Given type is an interface! Returning an empty list... (Type: {})", type);
            return List.of();
        }

        return type.getAllFields().stream()
                .map(CtFieldReference::getFieldDeclaration)
                .filter(field -> isAccessible(field, testClazz))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the field is accessible through direct field access according to the Java
     * Language Specification $6.6.1 and $6.6.2.
     *
     * @param field
     *          The field which should be checked, not {@code null}.
     *
     * @param testClazz
     *          The class which
     *
     * @return
     *          {@code true}, iff one of the following conditions is fulfilled (see $6.6.1 of the Java Language
     *          Specification for more details):
     *          <ul>
     *              <li>the {@code testClazz} is exactly the same class as the {@code field}'s declaring class</li>
     *              <li>the field is declared <i>public</i></li>
     *              <li>the field is declared <i>protected</i> and one of the following conditions is fulfilled</li>
     *              <ul>
     *                  <li>the {@code testClazz} is in the same package as the {@code field}'s declaring class</li>
     *                  <li>the {@code testClazz} is a subclass of the {@code field}'s declaring class (see $6.6.2 of the JLS)</li>
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
    private boolean isAccessible(CtField<?> field, CtClass<?> testClazz) {
        if(isPublicField(field) || field.getDeclaringType().equals(testClazz) ) {
            return true;
        } else if(isProtectedField(field)) {
            return isInSamePackageAsDeclaringClass(field, testClazz) || isSubClassOfDeclaringClass(field, testClazz);
        } else if(isPackagePrivateField(field) && isInSamePackageAsDeclaringClass(field, testClazz)) {
            return true;
        }

        return isInnerClassOfDeclaringClass(field, testClazz);
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
    private boolean isPublicField(CtField<?> field) {
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
    private boolean isProtectedField(CtField<?> field) {
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
    private boolean isPackagePrivateField(CtField<?> field) {
        return !field.isPublic() && !field.isProtected() && !field.isPrivate();
    }

    /**
     *
     * @param field
     *          The field to get the declaring class of, not {@code null}.
     *
     * @param testClazz
     *          The test class which contains the test methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code testClazz} is a <i>real</i> subclass of the declaring
     *          class of the given {@code field}. {@code false} is returned otherwise.
     */
    private boolean isSubClassOfDeclaringClass(CtField<?> field, CtClass<?> testClazz) {
        var fieldDeclaringType = field.getDeclaringType();

        var currentSuperClassReferenceType = testClazz.getSuperclass();

        while(Objects.nonNull(currentSuperClassReferenceType)) {
            var currentSuperClassType = currentSuperClassReferenceType.getTypeDeclaration();
            if(fieldDeclaringType.equals(currentSuperClassType)) {
                return true;
            }

            currentSuperClassReferenceType = currentSuperClassReferenceType.getSuperclass();
        }

        return false;
    }

    /**
     *
     * @param field
     *          The field to get the declaring class of, not {@code null}.
     *
     * @param testClazz
     *          The test class which contains the test methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the declaring class of the given {@code field} is in the
     *          same package as the given {@code testClazz}. {@code false} is returned otherwise.
     */
    private boolean isInSamePackageAsDeclaringClass(CtField<?> field, CtClass<?> testClazz) {
        var declaringTypePackage = field.getDeclaringType()
                .getPackage();
        var testClazzPackage = testClazz.getPackage();

        return declaringTypePackage.equals(testClazzPackage);
    }

    /**
     * Walks up the declaring type chain of the given {@code testClazz} until it either
     * reaches the given {@code field}'s declaring or the declaring type is {@code null}.
     *
     * @param field
     *          The field to get the declaring class of, not {@code null}.
     *
     * @param testClazz
     *          The test class which contains the test methods which could potentially
     *          access the given {@code field}, not {@code null}.
     *
     * @return
     *          {@code true}, if the given {@code testClass} is a non-static inner class of the given
     *          {@code field}'s declaring class. {@code false} is returned otherwise.
     */
    private boolean isInnerClassOfDeclaringClass(CtField<?> field, CtClass<?> testClazz) {
        var fieldDeclaringType = field.getDeclaringType();

        var testClassDeclaringType = testClazz.getDeclaringType();
        while(Objects.nonNull(testClassDeclaringType)) {
            if(fieldDeclaringType.equals(testClassDeclaringType)) {
                return true;
            }

            testClassDeclaringType = testClassDeclaringType.getDeclaringType();
        }

        return false;
    }

}
